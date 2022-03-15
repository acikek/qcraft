package com.acikek.qcraft.block;

import com.acikek.qcraft.block.qblock.QBlockItem;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;

public class FrequentialItem extends BlockItem {

    public FrequentialItem(Block block, Settings settings) {
        super(block, settings);
    }

    public static boolean isUnavailable(ItemStack stack) {
        return !(stack.getItem() instanceof FrequentialItem)
                || stack.getOrCreateNbt().containsUuid("frequency");
    }

    public static boolean checkStacks(ItemStack left, ItemStack right, boolean qBlock) {
        if (isUnavailable(left) || isUnavailable(right)) {
            return false;
        }
        if (qBlock) {
            return QBlockItem.equals(left, right);
        }
        return true;
    }
}
