package com.acikek.qcraft.world.state;

import com.acikek.qcraft.QCraft;
import com.acikek.qcraft.advancement.Criteria;
import com.acikek.qcraft.block.qblock.QBlock;
import com.acikek.qcraft.block.qblock.QBlockItem;
import com.acikek.qcraft.world.state.frequency.Frequential;
import com.acikek.qcraft.world.state.location.QBlockLocation;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class QBlockData extends LocationState<QBlockLocation, QBlockLocation.Pair> {

    public static final Keys keys = new Keys("qblock");

    public boolean settingBlock = false;

    public QBlockData() {
    }

    /**
     * Gets or creates {@link QBlockData} state from the specified world.
     *
     * @param world The {@link ServerWorld} to get the state from.
     * @return The {@link QBlockData} instance.
     */
    public static QBlockData get(World world, boolean filter) {
        QBlockData data = ((ServerWorld) world).getPersistentStateManager().getOrCreate(QBlockData::fromNbt, QBlockData::new, keys.id);
        if (filter) {
            data.filterLocations(world);
            data.frequencies.filter(data.locations);
        }
        return data;
    }

    public static QBlockData fromNbt(NbtCompound nbt) {
        QBlockData data = new QBlockData();
        List<QBlockLocation> locations = Frequential.parse(QBlockLocation.LIST_CODEC, nbt, keys.data);
        if (!locations.isEmpty()) {
            data.fill(locations, QBlockLocation.Pair::new);
        }
        return data;
    }

    /**
     * Filters the {@link QBlockLocation}s based on whether their current block state is possible.
     */
    public void filterLocations(World world) {
        int size = locations.list.size();
        locations.list.removeIf(location -> location.isStateImpossible(world.getBlockState(location.pos)));
        if (locations.list.size() < size) {
            QCraft.LOGGER.error("Removed " + (size - locations.list.size()) + " invalid qBlocks");
        }
    }

    /**
     * @return The {@link QBlockLocation}s that are in a loaded chunk.
     */
    public List<QBlockLocation> getLoadedLocations(ServerWorld world) {
        return locations.list.stream()
                .filter(location -> world.getChunkManager().isChunkLoaded(location.pos.getX() / 16, location.pos.getZ() / 16))
                .collect(Collectors.toList());
    }

    /**
     * @param loaded The locations that are in loaded chunks.
     * @return The {@link QBlockLocation}s that are within a close distance of the player.
     * @see QBlockData#getLoadedLocations(ServerWorld)
     */
    public List<QBlockLocation> getLocalLocations(ServerWorld world, List<QBlockLocation> loaded, PlayerEntity player) {
        return loaded.stream()
                .filter(location -> location.pos.isWithinDistance(player.getEyePos(), world.getServer().getPlayerManager().getSimulationDistance() * 16))
                .collect(Collectors.toList());
    }

    /**
     * Constructs and adds a {@link QBlockLocation} to this state's locations.
     *
     * @return The added location.
     */
    public QBlockLocation add(QBlock.Type type, BlockPos blockPos, ItemStack stack) {
        if (locations.has(blockPos)) {
            return null;
        }
        BlockState[] faces = QBlockItem.getFaces(stack);
        if (faces == null) {
            return null;
        }
        Optional<UUID> frequency = Frequential.getFrequency(stack);
        List<BlockState> states = new ArrayList<>(Arrays.asList(faces));
        QBlockLocation result = new QBlockLocation(blockPos, frequency, type, states, false);
        locations.list.add(result);
        frequency.ifPresent(f -> frequencies.add(f, result, QBlockLocation.Pair::new));
        markDirty();
        return result;
    }

    /**
     * A wrapper for {@link World#setBlockState(BlockPos, BlockState)}.<br>
     * This sets {@link QBlockData#settingBlock} to true so that {@link com.acikek.qcraft.mixin.WorldMixin} functions properly.
     */
    public void setBlockState(World world, BlockPos pos, BlockState state) {
        settingBlock = true;
        world.setBlockState(pos, state);
    }

    public void setFaceBlock(QBlockLocation location, World world, QBlock.Face face) {
        BlockState state = location.getFaceState(face);
        setBlockState(world, location.pos, state);
    }

    public void pseudoObserve(QBlockLocation location, World world, PlayerEntity player) {
        setFaceBlock(location, world, location.pickFace(player, world));
    }

    public void observe(QBlockLocation location, World world, PlayerEntity player) {
        QBlock.Face face = location.pickFace(player, world);
        observe(location, world, face, QBlock.Observation.PLAYER, player);
    }

    public void observe(QBlockLocation location, World world, QBlock.Face face, QBlock.Observation type, PlayerEntity player) {
        observe(location, world, face, type, player, false);
        frequencies.ifPresent(location, pair -> {
            QBlockLocation other = pair.getOther(location);
            if (other != null) {
                observe(other, world, face, type, player, true);
            }
        });
    }

    public void observe(QBlockLocation location, World world, QBlock.Face face,  QBlock.Observation type, PlayerEntity player, boolean entangled) {
        setFaceBlock(location, world, face);
        if (!entangled) {
            location.observed = true;
        }
        if (location.observedFace == null) {
            location.observedFace = face;
        }
        if (player != null) {
            Criteria.QUANTUM_OBSERVATION.trigger((ServerPlayerEntity) player, location.pos, type, location.type, entangled);
        }
    }

    public void unobserve(QBlockLocation location, World world, boolean checkFrequency) {
        if (location.observedFace != null) {
            BlockState state = world.getBlockState(location.pos);
            location.faces.set(location.observedFace.index, state);
            location.observedFace = null;
        }
        BlockState inert = location.type.resolveInert().getDefaultState();
        setBlockState(world, location.pos, inert);
        location.observed = false;
        if (checkFrequency) {
            frequencies.ifPresent(location, pair -> {
                QBlockLocation other = pair.getOther(location);
                if (other != null && !other.observed) {
                    unobserve(other, world, false);
                }
            });
        }
    }

    public boolean getOtherNotObserved(QBlockLocation location) {
        AtomicBoolean otherObserved = new AtomicBoolean(false);
        frequencies.ifPresent(location, pair -> otherObserved.set(pair.getOtherObserved(location)));
        return !otherObserved.get();
    }

    public boolean canBeUnobserved(QBlockLocation location, Vec3d center) {
        return getOtherNotObserved(location) && !location.pos.isWithinDistance(center, 2.0);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        Frequential.encode(QBlockLocation.LIST_CODEC, locations.list, nbt, keys.data);
        return nbt;
    }
}
