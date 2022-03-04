package com.acikek.qcraft.block.qblock;

import com.acikek.qcraft.block.BlockItemProvider;
import com.acikek.qcraft.block.Blocks;
import com.acikek.qcraft.world.state.location.QBlockLocation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Random;
import java.util.function.BiFunction;

public class QBlock extends InertQBlock implements BlockItemProvider {

    public enum Type {

        OBSERVER_DEPENDENT("observer_dependent_block"),
        QUANTUM("quantum_block");

        public static final Codec<Type> CODEC = Codec.STRING.comapFlatMap(Type::validate, Type::getId);

        public static DataResult<Type> validate(String id) {
            return switch (id) {
                case "observer_dependent_block" -> DataResult.success(Type.OBSERVER_DEPENDENT);
                case "quantum_block" -> DataResult.success(Type.QUANTUM);
                default -> DataResult.error("Not a valid qBlock type: " + id);
            };
        }

        public QBlock resolveBlock() {
            return switch (this) {
                case OBSERVER_DEPENDENT -> Blocks.OBSERVER_DEPENDENT_BLOCK;
                case QUANTUM -> Blocks.QUANTUM_BLOCK;
            };
        }

        public InertQBlock resolveInert() {
            return switch (this) {
                case OBSERVER_DEPENDENT -> Blocks.INERT_OBSERVER_DEPENDENT_BLOCK;
                case QUANTUM -> Blocks.INERT_QUANTUM_BLOCK;
            };
        }

        public Face pickFace(QBlockLocation location, Vec3d dists, Random random) {
            return switch (this) {
                case OBSERVER_DEPENDENT -> location.getClosestFace(dists);
                case QUANTUM -> location.getClosestAxis(dists).getRandomFace(random);
            };
        }

        public Face pickFace(Direction direction, Random random) {
            return switch (this) {
                case OBSERVER_DEPENDENT -> Face.from(direction);
                case QUANTUM -> Axis.from(direction.getAxis()).getRandomFace(random);
            };
        }

        public final String id;

        Type(String id) {
            this.id = id;
        }

        public String getId() {
            return resolveBlock().type.id;
        }
    }

    public enum Face {

        UP(0, 0),
        NORTH(1, 1),
        WEST(3, 2),
        EAST(5, 3),
        DOWN(6, 4),
        SOUTH(7, 5);

        public final int slot;
        public final int index;

        Face(int slot, int index) {
            this.slot = slot;
            this.index = index;
        }

        public static Face from(Direction direction) {
            return switch (direction) {
                case UP -> UP;
                case NORTH -> NORTH;
                case WEST -> WEST;
                case EAST -> EAST;
                case DOWN -> DOWN;
                case SOUTH -> SOUTH;
            };
        }

        public static final int CENTER = 4;

        public static final int[] EMPTY_SLOTS = {
                2, 8
        };
    }

    public enum Axis {

        X,
        Y,
        Z;

        public Face getRandomFace(Random rng) {
            boolean value = rng.nextBoolean();
            return switch (this) {
                case X -> value ? Face.EAST : Face.WEST;
                case Y -> value ? Face.UP : Face.DOWN;
                case Z -> value ? Face.SOUTH : Face.NORTH;
            };
        }

        public static Axis from(Direction.Axis axis) {
            return switch (axis) {
                case X -> X;
                case Y -> Y;
                case Z -> Z;
            };
        }
    }

    public final Type type;

    public QBlock(Type type) {
        super();
        this.type = type;
    }

    public static QBlock getBlockFromItem(Item item) {
        return (QBlock) Block.getBlockFromItem(item);
    }

    @Override
    public BiFunction<Block, Item.Settings, BlockItem> getBlockItem() {
        return QBlockItem::new;
    }
}
