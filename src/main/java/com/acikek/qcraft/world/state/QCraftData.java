package com.acikek.qcraft.world.state;

import com.acikek.qcraft.QCraft;
import com.acikek.qcraft.advancement.Criteria;
import com.acikek.qcraft.block.qblock.QBlock;
import com.acikek.qcraft.block.qblock.QBlockItem;
import com.acikek.qcraft.world.state.frequency.FrequencyMap;
import com.acikek.qcraft.world.state.frequency.Frequential;
import com.acikek.qcraft.world.state.frequency.LocationList;
import com.acikek.qcraft.world.state.location.QBlockLocation;
import com.acikek.qcraft.world.state.location.QuantumComputerLocation;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class QCraftData extends PersistentState {

    public static final String KEY = QCraft.ID + "_" + "data";
    public static final String DATA = "qblock_locations";

    public boolean settingBlock = false;

    public LocationList<QBlockLocation> qBlockLocations = new LocationList<>();
    public LocationList<QuantumComputerLocation> quantumComputerLocations = new LocationList<>();

    public FrequencyMap<QBlockLocation, QBlockLocation.Pair> qBlockFrequencies = new FrequencyMap<>();
    public FrequencyMap<QuantumComputerLocation, QuantumComputerLocation.Pair> quantumComputerFrequencies = new FrequencyMap<>();

    public QCraftData() {
    }

    /**
     * Gets or creates {@link QCraftData} state from the specified world.
     *
     * @param world The {@link ServerWorld} to get the state from.
     * @return The {@link QCraftData} instance.
     */
    public static QCraftData get(World world, boolean filter) {
        QCraftData data = ((ServerWorld) world).getPersistentStateManager().getOrCreate(QCraftData::fromNbt, QCraftData::new, KEY);
        if (filter) {
            data.filterLocations(world);
            data.qBlockFrequencies.filter(data.qBlockLocations);
        }
        return data;
    }

    public static QCraftData fromNbt(NbtCompound nbt) {
        QCraftData data = new QCraftData();
        List<QBlockLocation> locations = Frequential.parse(QBlockLocation.LIST_CODEC, nbt, DATA);
        if (!locations.isEmpty()) {
            data.qBlockLocations.locations.addAll(locations);
            QCraft.LOGGER.info("Loaded " + data.qBlockLocations.locations.size() + " qBlocks");
            data.qBlockFrequencies.fill(locations, QBlockLocation.Pair::new);
            QCraft.LOGGER.info("Loaded " + data.qBlockFrequencies.frequencies.size() + " frequencies");
        }
        return data;
    }

    /**
     * Filters the {@link QBlockLocation}s based on whether their current block state is possible.
     */
    public void filterLocations(World world) {
        int size = qBlockLocations.locations.size();
        qBlockLocations.locations.removeIf(location -> location.isStateImpossible(world.getBlockState(location.pos)));
        if (qBlockLocations.locations.size() < size) {
            QCraft.LOGGER.error("Removed " + (size - qBlockLocations.locations.size()) + " invalid qBlocks");
        }
    }

    /**
     * @return The {@link QBlockLocation}s that are in a loaded chunk.
     */
    public List<QBlockLocation> getLoadedLocations(ServerWorld world) {
        return qBlockLocations.locations.stream()
                .filter(location -> world.getChunkManager().isChunkLoaded(location.pos.getX() / 16, location.pos.getZ() / 16))
                .collect(Collectors.toList());
    }

    /**
     * @param loaded The locations that are in loaded chunks.
     * @return The {@link QBlockLocation}s that are within a close distance of the player.
     * @see QCraftData#getLoadedLocations(ServerWorld)
     */
    public List<QBlockLocation> getLocalLocations(List<QBlockLocation> loaded, PlayerEntity player) {
        return loaded.stream()
                .filter(location -> location.pos.isWithinDistance(player.getEyePos(), 160))
                .collect(Collectors.toList());
    }

    /**
     * Constructs and adds a {@link QBlockLocation} to this state's locations.
     *
     * @return The added location.
     */
    public QBlockLocation addBlock(QBlock.Type type, BlockPos blockPos, ItemStack stack) {
        if (qBlockLocations.has(blockPos)) {
            return null;
        }
        String[] faces = QBlockItem.getFaces(stack);
        if (faces == null) {
            return null;
        }
        NbtCompound stackNbt = stack.getOrCreateNbt();
        Optional<UUID> frequency = stackNbt.containsUuid("frequency")
                ? Optional.of(stackNbt.getUuid("frequency"))
                : Optional.empty();
        QBlockLocation result = new QBlockLocation(blockPos, frequency, type, List.of(faces), false);
        qBlockLocations.locations.add(result);
        frequency.ifPresent(f -> qBlockFrequencies.add(f, result, QBlockLocation.Pair::new));
        markDirty();
        return result;
    }

    public void reset() {
        qBlockLocations.locations.clear();
        qBlockFrequencies.frequencies.clear();
    }

    /**
     * A wrapper for {@link World#setBlockState(BlockPos, BlockState)}.<br>
     * This sets {@link QCraftData#settingBlock} to true so that {@link com.acikek.qcraft.mixin.WorldMixin} functions properly.
     */
    public void setBlockState(World world, BlockPos pos, BlockState state) {
        settingBlock = true;
        world.setBlockState(pos, state);
    }

    public void setFaceBlock(QBlockLocation location, World world, QBlock.Face face) {
        BlockState state = location.getFaceBlock(face).getDefaultState();
        setBlockState(world, location.pos, state);
    }

    public void pseudoObserve(QBlockLocation location, World world, PlayerEntity player) {
        setFaceBlock(location, world, location.pickFace(player, world));
    }

    public void observe(QBlockLocation location, World world, PlayerEntity player) {
        QBlock.Face face = location.pickFace(player, world);
        observe(location, world, face, player, QBlock.Observation.PLAYER, false);
        qBlockFrequencies.ifPresent(location, pair -> {
            QBlockLocation other = pair.getOther(location);
            if (other != null) {
                observe(other, world, face, player, QBlock.Observation.PLAYER, true);
            }
        });
    }

    public void observe(QBlockLocation location, World world, QBlock.Face face, PlayerEntity player, QBlock.Observation type, boolean entangled) {
        setFaceBlock(location, world, face);
        if (!entangled) {
            location.observed = true;
        }
        if (player != null) {
            Criteria.QUANTUM_OBSERVATION.trigger((ServerPlayerEntity) player, location.pos, type, location.type, entangled);
        }
    }

    public void unobserve(QBlockLocation location, World world, boolean checkFrequency) {
        BlockState state = location.type.resolveInert().getDefaultState();
        setBlockState(world, location.pos, state);
        location.observed = false;
        if (checkFrequency) {
            qBlockFrequencies.ifPresent(location, pair -> {
                QBlockLocation other = pair.getOther(location);
                if (other != null && !other.observed) {
                    unobserve(other, world, false);
                }
            });
        }
    }

    public boolean getOtherNotObserved(QBlockLocation location) {
        AtomicBoolean otherObserved = new AtomicBoolean(false);
        qBlockFrequencies.ifPresent(location, pair -> otherObserved.set(pair.getOtherObserved(location)));
        return !otherObserved.get();
    }

    public boolean canBeUnobserved(QBlockLocation location, Vec3d center) {
        return getOtherNotObserved(location) && !location.pos.isWithinDistance(center, 2.0);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        Frequential.encode(QBlockLocation.LIST_CODEC, qBlockLocations.locations, nbt, DATA);
        return nbt;
    }
}
