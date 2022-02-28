package com.acikek.qcraft.block;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;

import java.util.function.BiFunction;

public interface BlockItemProvider {
    BiFunction<Block, Item.Settings, BlockItem> getBlockItem();
}
