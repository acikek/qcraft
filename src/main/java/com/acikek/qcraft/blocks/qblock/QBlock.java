package com.acikek.qcraft.blocks.qblock;

import com.acikek.qcraft.blocks.Blocks;
import com.acikek.qcraft.items.Essence;
import com.acikek.qcraft.items.Items;
import com.acikek.qcraft.world.QBlockData;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;

public class QBlock extends Block  {

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

    public Type type;

    public QBlock(Settings settings, Type type) {
        super(settings);
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

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient()) {
            Optional<QBlockData.QBlockLocation> block = QBlockData.get(world).getBlock(pos);
            block.ifPresent(qBlockLocation -> world.setBlockState(pos, qBlockLocation.getFaceBlock(world.random.nextInt(6)).getDefaultState()));
        }
        return ActionResult.SUCCESS;
        //return super.onUse(state, world, pos, player, hand, hit);
    }
}
