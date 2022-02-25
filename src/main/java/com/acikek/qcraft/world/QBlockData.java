package com.acikek.qcraft.world;

import com.acikek.qcraft.QCraft;
import com.acikek.qcraft.block.qblock.InertQBlock;
import com.acikek.qcraft.block.qblock.QBlock;
import com.acikek.qcraft.block.qblock.QBlockItem;
import com.acikek.qcraft.recipe.QBlockRecipe;
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
import net.minecraft.util.dynamic.DynamicSerializableUuid;
import net.minecraft.util.math.*;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class QBlockData extends PersistentState {

    public static final Codec<List<QBlockLocation>> CODEC = Codec.list(QBlockLocation.CODEC);
    public static final String DATA = "qblocklocations";
    public static final String KEY = QCraft.ID + "_" + DATA;

    public final List<QBlockLocation> locations = new ArrayList<>();
    public final Map<UUID, QBlockLocation.Pair> frequencies = new HashMap<>();
    public boolean settingBlock = false;
    public QBlockLocation removed = null;

    public QBlockData() {
    }

    /**
     * Gets or creates {@link QBlockData} state from the specified world.
     *
     * @param world The {@link ServerWorld} to get the state from.
     * @return The {@link QBlockData} instance.
     */
    public static QBlockData get(World world, boolean filter) {
        QBlockData data = ((ServerWorld) world).getPersistentStateManager().getOrCreate(QBlockData::fromNbt, QBlockData::new, KEY);
        if (filter) {
            data.filterLocations(world);
            data.filterFrequencies();
        }
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
            blockData.fillFrequencies();
            QCraft.LOGGER.info("Loaded " + blockData.frequencies.size() + " frequencies");
        }
        return blockData;
    }

    /**
     * Filters the {@link QBlockLocation}s based on whether their current block state is possible.
     */
    public void filterLocations(World world) {
        int size = locations.size();
        locations.removeIf(location -> location.isStateImpossible(world.getBlockState(location.pos)));
        if (locations.size() < size) {
            QCraft.LOGGER.error("Removed " + (size - locations.size()) + " invalid qBlocks");
        }
    }

    public void filterFrequencies() {
        int size = frequencies.size();
        for (Map.Entry<UUID, QBlockLocation.Pair> frequency : frequencies.entrySet()) {
            for (QBlockLocation location : frequency.getValue().getBoth()) {
                if (location != null && !locations.contains(location)) {
                    frequencies.remove(frequency.getKey());
                    break;
                }
            }
        }
        if (frequencies.size() < size) {
            QCraft.LOGGER.error("Removed " + (size - frequencies.size()) + " invalid frequencies");
        }
    }

    public void fillFrequencies() {
        for (QBlockLocation location : locations) {
            location.frequency.ifPresent(frequency -> {
                if (!frequencies.containsKey(frequency)) {
                    frequencies.put(frequency, new QBlockLocation.Pair(location));
                }
                else if (!frequencies.get(frequency).add(location)) {
                    QCraft.LOGGER.error("Failed to add " + location + " to frequency '" + frequency + "'");
                }
            });
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

    public void getFrequency(QBlockLocation location, Consumer<QBlockLocation.Pair> consumer) {
        location.frequency.ifPresent(frequency -> {
            if (frequencies.containsKey(frequency)) {
                consumer.accept(frequencies.get(frequency));
            }
            else {
                QCraft.LOGGER.error(location + " has an unmatched frequency '" + frequency + "'");
                location.frequency = Optional.empty();
            }
        });
    }

    /**
     * Constructs and adds a {@link QBlockLocation} to this state's locations.
     *
     * @return The added location.
     */
    public QBlockLocation addBlock(QBlock.Type type, BlockPos blockPos, ItemStack stack) {
        if (getBlock(blockPos).isPresent()) {
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
        QBlockLocation result = new QBlockLocation(type, blockPos, List.of(faces), false, frequency);
        locations.add(result);
        frequency.ifPresent(f -> {
            if (frequencies.containsKey(f)) {
                frequencies.get(f).add(result);
            }
            else {
                frequencies.put(f, new QBlockLocation.Pair(result));
            }
        });
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
     *
     * @see QBlockData#removeBlock(BlockPos)
     */
    public void removeBlock(QBlockLocation location) {
        if (locations.remove(location)) {
            removed = location;
            getFrequency(location, pair -> {
                if (pair.remove(location) && location.frequency.isPresent()) {
                    frequencies.remove(location.frequency.get());
                }
            });
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
        QBlock.Face face = location.type.pickFace(location, dists, world.random);
        observe(location, world, face);
        getFrequency(location, pair -> {
            QBlockLocation other = pair.getOther(location);
            if (other != null) {
                setFaceBlock(world, other, face);
            }
        });
    }

    public void observe(QBlockLocation location, World world, QBlock.Axis axis) {
        observe(location, world, axis.getRandomFace(world.random));
    }

    public void observe(QBlockLocation location, World world, QBlock.Face face) {
        setFaceBlock(world, location, face);
        location.observed = true;
    }

    public void unobserve(QBlockLocation location, World world, boolean checkFrequency) {
        BlockState state = location.type.resolveInert().getDefaultState();
        setBlockState(world, location.pos, state);
        location.observed = false;
        if (checkFrequency) {
            getFrequency(location, pair -> {
                QBlockLocation other = pair.getOther(location);
                if (other != null && !other.observed) {
                    unobserve(other, world, false);
                }
            });
        }
    }

    public boolean getOtherNotObserved(QBlockLocation location) {
        AtomicBoolean otherObserved = new AtomicBoolean(false);
        getFrequency(location, pair -> otherObserved.set(pair.getOtherObserved(location)));
        return !otherObserved.get();
    }

    public boolean canBeUnobserved(QBlockLocation location, Vec3d center) {
        return getOtherNotObserved(location) && !location.pos.isWithinDistance(center, 2.0);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        CODEC.encodeStart(NbtOps.INSTANCE, locations)
                .result()
                .ifPresent(tag -> nbt.put(DATA, tag));
        return nbt;
    }

    /**
     * Represents a placed qBlock in the world.
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static class QBlockLocation {

        public static final Codec<QBlockLocation> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                                QBlock.Type.CODEC.fieldOf("type").forGetter(l -> l.type),
                                BlockPos.CODEC.fieldOf("pos").forGetter(l -> l.pos),
                                Codec.list(Codec.STRING).fieldOf("faces").forGetter(l -> l.faces),
                                Codec.BOOL.fieldOf("observed").forGetter(l -> l.observed),
                                DynamicSerializableUuid.CODEC.optionalFieldOf("frequency").forGetter(l -> l.frequency)
                        )
                        .apply(instance, QBlockLocation::new)
        );

        public final QBlock.Type type;
        public final BlockPos pos;
        public final List<String> faces;
        public boolean observed;
        public Optional<UUID> frequency;

        /**
         * Constructs a {@link QBlockLocation}.<br>
         * To add a location to a {@link QBlockData} instance, see {@link QBlockData#addBlock(QBlock.Type, BlockPos, ItemStack)}.
         *
         * @param type      The qBlock's type. This determines its observation behavior.
         * @param pos       The block position of the location.
         * @param faces     The block face IDs. To read these from an item, see {@link QBlockItem#getFaces(ItemStack)}.
         * @param observed  Whether this location is currently observed.
         * @param frequency The String UUID of the location's entanglement frequency.
         */
        public QBlockLocation(QBlock.Type type, BlockPos pos, List<String> faces, boolean observed, Optional<UUID> frequency) {
            this.type = type;
            this.pos = pos;
            this.faces = faces;
            this.observed = observed;
            this.frequency = frequency;
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
            QBlockRecipe.applyFaces(stack, faces);
            frequency.ifPresent(f -> stack.getOrCreateNbt().putUuid("frequency", f));
            return stack;
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

        @Override
        public String toString() {
            return "qBlock (" + type + ", " + pos.toShortString() + ")";
        }

        /**
         * Represents a pair of entangled qBlocks.
         */
        public static class Pair {

            public QBlockLocation left;
            public QBlockLocation right = null;

            public Pair(QBlockLocation first) {
                left = first;
            }

            /**
             * Adds a location to the pair, assuming that the left value is already present.
             *
             * @return Whether the location was successfully added.
             */
            public boolean add(QBlockLocation second) {
                if (left != null && right == null) {
                    right = second;
                    return true;
                }
                return false;
            }

            /**
             * Removes a location from the pair, either left or right.
             *
             * @return Whether the pair is empty and should be removed.
             */
            public boolean remove(QBlockLocation location) {
                if (left == location) {
                    if (right != null) {
                        left = right;
                        right = null;
                    }
                    else {
                        left = null;
                        return true;
                    }
                }
                else if (right == location) {
                    right = null;
                }
                return false;
            }

            /**
             * @return The other location in the pair.
             */
            public QBlockLocation getOther(QBlockLocation location) {
                if (left == location) {
                    return right;
                }
                else if (right == location) {
                    return left;
                }
                return null;
            }

            public boolean getOtherObserved(QBlockLocation location) {
                QBlockLocation other = getOther(location);
                if (other != null) {
                    return other.observed;
                }
                return false;
            }

            /**
             * @return Both locations in the pair.
             */
            public QBlockLocation[] getBoth() {
                return new QBlockLocation[] {
                        left,
                        right
                };
            }
        }
    }
}
