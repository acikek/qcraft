package com.acikek.qcraft.world;

import com.acikek.qcraft.QCraft;
import com.acikek.qcraft.blocks.qblock.InertQBlock;
import com.acikek.qcraft.blocks.qblock.QBlock;
import com.acikek.qcraft.blocks.qblock.QBlockRecipe;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

import java.util.*;
import java.util.stream.Collectors;

public class QBlockData extends PersistentState {

    public static Codec<List<QBlockLocation>> CODEC = Codec.list(QBlockLocation.CODEC);
    public static final String DATA = "qblocklocations";
    public static final String KEY = QCraft.ID + "_" + DATA;

    public List<QBlockLocation> locations = new ArrayList<>();
    public boolean settingBlock = false;

    public QBlockData() {
    }

    /**
     * Gets or creates {@link QBlockData} state from the specified world.
     * @param world The {@link ServerWorld} to get the state from.
     * @return The {@link QBlockData} instance.
     */
    public static QBlockData get(World world) {
        QBlockData data = ((ServerWorld) world).getPersistentStateManager().getOrCreate(QBlockData::fromNbt, QBlockData::new, KEY);
        data.filterBlocks(world);
        return data;
    }

    public static QBlockData fromNbt(NbtCompound nbt) {
        QBlockData blockData = new QBlockData();
        List<QBlockLocation> locations = CODEC.parse(NbtOps.INSTANCE, nbt.getList(DATA, NbtElement.COMPOUND_TYPE))
                .result()
                .orElse(Collections.emptyList());
        if (!locations.isEmpty()) {
            blockData.locations.addAll(locations);
            QCraft.LOGGER.info("Loaded " + blockData.locations.size() + " qBlocks");
        }
        return blockData;
    }

    /**
     * Filters the {@link QBlockLocation}s based on whether their current block state is possible.
     */
    public void filterBlocks(World world) {
        int size = locations.size();
        locations.removeIf(location -> location.isStateImpossible(world.getBlockState(location.pos)));
        if (locations.size() < size) {
            QCraft.LOGGER.error("Removed " + (size - locations.size()) + " invalid qBlocks");
        }
    }

    /**
     * @return The {@link QBlockLocation}s that are in a loaded chunk.
     */
    public List<QBlockLocation> getLoadedLocations(ServerWorld world) {
        return locations.stream()
                .filter(location -> world.getChunkManager().isChunkLoaded(location.pos.getX() / 16, location.pos.getZ() / 16))
                .collect(Collectors.toList());
    }

    /**
     * @param loaded The locations that are in loaded chunks.
     * @return The {@link QBlockLocation}s that are within a close distance of the player.
     * @see QBlockData#getLoadedLocations(ServerWorld)
     */
    public List<QBlockLocation> getLocalLocations(List<QBlockLocation> loaded, PlayerEntity player) {
        return loaded.stream()
                .filter(location -> location.pos.isWithinDistance(player.getEyePos(), 160))
                .collect(Collectors.toList());
    }

    /**
     * Adds a {@link QBlockLocation} to this state's locations.
     * @param stack The stack to read faces data from.
     * @return The added location.
     */
    public QBlockLocation addBlock(QBlock.Type type, BlockPos blockPos, ItemStack stack) {
        String[] faces = QBlock.getFaces(stack);
        if (faces == null || getBlock(blockPos).isPresent()) {
            return null;
        }
        QBlockLocation result = new QBlockLocation(type, blockPos, List.of(faces), false);
        locations.add(result);
        markDirty();
        return result;
    }

    /**
     * @return The location at the specified block position, if present.
     */
    public Optional<QBlockLocation> getBlock(BlockPos blockPos) {
        return locations.stream()
                .filter(loc -> loc.pos.asLong() == blockPos.asLong())
                .findFirst();
    }

    /**
     * Removes the block at the specified block position, if present.
     */
    public void removeBlock(BlockPos blockPos) {
        getBlock(blockPos).ifPresent(this::removeBlock);
    }

    /**
     * Removes the specified block location.
     * @see QBlockData#removeBlock(BlockPos)
     */
    public void removeBlock(QBlockLocation location) {
        if (locations.remove(location)) {
            markDirty();
        }
    }

    public boolean hasBlock(BlockPos blockPos) {
        return getBlock(blockPos).isPresent();
    }

    /**
     * A wrapper for {@link World#setBlockState(BlockPos, BlockState)}.<br>
     * This sets {@link QBlockData#settingBlock} to true so that {@link com.acikek.qcraft.mixin.WorldMixin} functions properly.
     */
    public void setBlockState(World world, BlockPos pos, BlockState state) {
        settingBlock = true;
        world.setBlockState(pos, state);
    }

    public void setFaceBlock(World world, QBlockLocation location, QBlock.Face face) {
        BlockState state = location.getFaceBlock(face).getDefaultState();
        setBlockState(world, location.pos, state);
    }

    public void observe(QBlockLocation location, World world, PlayerEntity player) {
        Vec3d dists = location.getBetween(player.getEyePos());
        switch (location.type) {
            case OBSERVER_DEPENDENT -> setFaceBlock(world, location, location.getClosestFace(dists));
            case QUANTUM -> setFaceBlock(world, location, location.getClosestAxis(dists).getRandomFace(world.random));
        }
        location.observed = true;
    }

    public void unobserve(QBlockLocation location, World world) {
        BlockState state = location.type.resolveInert().getDefaultState();
        setBlockState(world, location.pos, state);
        location.observed = false;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        CODEC.encodeStart(NbtOps.INSTANCE, locations)
                .result()
                .ifPresent(tag -> nbt.put(DATA, tag));
        return nbt;
    }

    public static class QBlockLocation {

        public static Codec<QBlockLocation> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        QBlock.Type.CODEC.fieldOf("type").forGetter(l -> l.type),
                        BlockPos.CODEC.fieldOf("pos").forGetter(l -> l.pos),
                        Codec.list(Codec.STRING).fieldOf("faces").forGetter(l -> l.faces),
                        Codec.BOOL.fieldOf("observed").forGetter(l -> l.observed)
                )
                        .apply(instance, QBlockLocation::new)
        );

        public QBlock.Type type;
        public BlockPos pos;
        public List<String> faces;
        public boolean observed;

        /**
         * Constructs a {@link QBlockLocation}.<br>
         * To add a location to a {@link QBlockData} instance, see {@link QBlockData#addBlock(QBlock.Type, BlockPos, ItemStack)}.
         * @param type The qBlock's type. This determines its observation behavior.
         * @param pos The block position of the location.
         * @param faces The block face IDs. To read these from an item, see {@link QBlock#getFaces(ItemStack)}.
         * @param observed Whether this location is currently observed.
         */
        public QBlockLocation(QBlock.Type type, BlockPos pos, List<String> faces, boolean observed) {
            this.type = type;
            this.pos = pos;
            this.faces = faces;
            this.observed = observed;
        }

        public Block getFaceBlock(int index) {
            return Registry.BLOCK.get(Identifier.tryParse(faces.get(index)));
        }

        /**
         * @return The block at the specified face.
         * @see QBlockLocation#getFaceBlock(QBlock.Face)
         */
        public Block getFaceBlock(QBlock.Face face) {
            return getFaceBlock(face.index);
        }

        /**
         * @return The stack, based on block type, with the faces applied to its NBT.
         * @see QBlockRecipe#applyFaces(ItemStack, List)
         */
        public ItemStack getItemStack() {
            ItemStack stack = new ItemStack(type.resolveBlock());
            return QBlockRecipe.applyFaces(stack, faces);
        }

        /**
         * @return The vector between the block position and a player's eye position.
         */
        public Vec3d getBetween(Vec3d eyePos) {
            return eyePos.subtract(Vec3d.ofCenter(pos));
        }

        /**
         * @return The closest axis to the player.
         * @see QBlockLocation#getBetween(Vec3d)
         */
        public QBlock.Axis getClosestAxis(Vec3d dists) {
            double absX = Math.abs(dists.x);
            double absY = Math.abs(dists.y);
            double absZ = Math.abs(dists.z);
            if (absX > absY && absX > absZ) {
                return QBlock.Axis.X;
            }
            else if (absY > absX && absY > absZ) {
                return QBlock.Axis.Y;
            }
            else {
                return QBlock.Axis.Z;
            }
        }

        /**
         * @return The closest block face to the player.
         * @see QBlockLocation#getBetween(Vec3d)
         */
        public QBlock.Face getClosestFace(Vec3d dists) {
            return switch (getClosestAxis(dists)) {
                case X -> dists.x > 0 ? QBlock.Face.EAST : QBlock.Face.WEST;
                case Y -> dists.y > 0 ? QBlock.Face.UP : QBlock.Face.DOWN;
                case Z -> dists.z > 0 ? QBlock.Face.SOUTH : QBlock.Face.NORTH;
            };
        }

        /**
         * @return Whether the specified block state is impossible given the valid block faces.
         */
        public boolean isStateImpossible(BlockState state) {
            Block block = state.getBlock();
            if (block instanceof InertQBlock) {
                return false;
            }
            String id = Registry.BLOCK.getId(block).toString();
            return !faces.contains(id);
        }

        /**
         * @return Whether the location can be observed (a player's central position is within a certain distance).
         */
        public boolean canBeUnobserved(Vec3d center) {
            return !pos.isWithinDistance(center, 2.0);
        }
    }
}
