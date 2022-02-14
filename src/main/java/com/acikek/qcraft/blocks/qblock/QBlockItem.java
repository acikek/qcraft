package com.acikek.qcraft.blocks.qblock;

import com.acikek.qcraft.world.QBlockData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;

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
            QBlock qBlock = (QBlock) getBlock();
            return QBlockData.get(context.getWorld()).addBlock(qBlock.type, context.getBlockPos(), context.getWorld(), context.getStack());
        }
        return false;
    }
}