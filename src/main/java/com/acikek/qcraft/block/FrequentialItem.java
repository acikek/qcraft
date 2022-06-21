package com.acikek.qcraft.block;

import com.acikek.qcraft.block.qblock.QBlockItem;
import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (stack.getOrCreateNbt().containsUuid("frequency")) {
            String uuid = stack.getOrCreateNbt().getUuid("frequency").toString();
            String snippet = uuid.substring(0, uuid.indexOf('-')) + "...";
            MutableText frequency = Text.translatable("tooltip.qcraft.frequency")
                    .append(": ")
                    .setStyle(Style.EMPTY.withItalic(false))
                    .formatted(Formatting.GRAY);
            Text text = Text.literal(snippet)
                    .formatted(Formatting.DARK_AQUA);
            frequency.append(text);
            tooltip.add(frequency);
        }
    }
}
