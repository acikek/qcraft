package com.acikek.qcraft.blocks.qblock;

import com.acikek.qcraft.world.QBlockData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

import java.util.Arrays;

public class QBlockItem extends BlockItem {

    public QBlockItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    protected boolean place(ItemPlacementContext context, BlockState state) {
        boolean result = super.place(context, state);
        if (context.getWorld().isClient()) {
            return result;
        }
        if (result) {
            QBlockData data = QBlockData.get(context.getWorld(), true);
            QBlockData.QBlockLocation added = data.addBlock(((QBlock) getBlock()).type, context.getBlockPos(), context.getStack());
            if (added == null) {
                return false;
            }
            if (context.getPlayer() != null) {
                data.observe(added, context.getWorld(), context.getPlayer());
            }
            System.out.println(data.frequencies);
            return true;
        }
        return false;
    }

    public static String[] getFaces(ItemStack stack) {
        NbtCompound nbt = stack.getSubNbt("faces");
        if (nbt == null) {
            return null;
        }
        String[] faces = new String[6];
        for (int i = 0; i < faces.length; i++) {
            faces[i] = nbt.getString(QBlock.Face.values()[i].name());
        }
        return faces;
    }

    public static boolean checkStacks(ItemStack left, ItemStack right) {
        return !left.isEmpty() && !right.isEmpty()
                && QBlock.getBlockFromItem(left.getItem()).type == QBlock.getBlockFromItem(right.getItem()).type
                && Arrays.equals(getFaces(left), getFaces(right));
    }
}