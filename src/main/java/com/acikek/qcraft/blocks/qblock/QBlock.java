package com.acikek.qcraft.blocks.qblock;

import com.acikek.qcraft.blocks.Blocks;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

import java.util.Random;

public class QBlock extends InertQBlock {

    public enum Type {

        OBSERVER_DEPENDENT("observer_dependent_block"),
        QUANTUM("quantum_block");

        public static Codec<Type> CODEC = Codec.STRING.comapFlatMap(Type::validate, Type::getId);

        public static DataResult<Type> validate(String id) {
            return switch (id) {
                case "observer_dependent_block" -> DataResult.success(Type.OBSERVER_DEPENDENT);
                case "quantum_block" -> DataResult.success(Type.QUANTUM);
                default -> DataResult.error("Not a valid QBlock type: " + id);
            };
        }

        public QBlock resolveBlock() {
            return switch (this) {
                case OBSERVER_DEPENDENT -> Blocks.OBSERVER_DEPENDENT_BLOCK;
                case QUANTUM -> Blocks.QUANTUM_BLOCK;
            };
        }

        public InertQBlock resolveInert() {
            return switch(this) {
                case OBSERVER_DEPENDENT -> Blocks.INERT_OBSERVER_DEPENDENT_BLOCK;
                case QUANTUM -> Blocks.INERT_QUANTUM_BLOCK;
            };
        }

        public String id;

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

        public int slot;
        public int index;

        Face(int slot, int index) {
            this.slot = slot;
            this.index = index;
        }

        public static int CENTER = 4;

        public static int[] EMPTY_SLOTS = {
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
    }

    public Type type;

    public QBlock(Type type) {
        super();
        this.type = type;
    }

    public static String[] getFaces(ItemStack stack) {
        NbtCompound nbt = stack.getSubNbt("faces");
        if (nbt == null) {
            return null;
        }
        String[] faces = new String[6];
        for (int i = 0; i < faces.length; i++) {
            faces[i] = nbt.getString(Face.values()[i].name());
        }
        return faces;
    }
}
