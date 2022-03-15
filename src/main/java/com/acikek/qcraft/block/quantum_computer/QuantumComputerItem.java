package com.acikek.qcraft.block.quantum_computer;

import com.acikek.qcraft.block.FrequentialItem;
import com.acikek.qcraft.world.state.QuantumComputerData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;

public class QuantumComputerItem extends FrequentialItem {

    public QuantumComputerItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    protected boolean place(ItemPlacementContext context, BlockState state) {
        if (super.place(context, state)) {
            if (!context.getWorld().isClient()) {
                QuantumComputerData.get(context.getWorld()).add(context.getBlockPos(), context.getStack());
            }
            return true;
        }
        return false;
    }
}
