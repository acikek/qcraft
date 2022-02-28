package com.acikek.qcraft.block.quantum_computer;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;

public class QuantumComputerItem extends BlockItem {

    public QuantumComputerItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    protected boolean place(ItemPlacementContext context, BlockState state) {
        if (super.place(context, state)) {

            return true;
        }
        return false;
    }
}
