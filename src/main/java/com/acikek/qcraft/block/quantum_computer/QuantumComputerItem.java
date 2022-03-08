package com.acikek.qcraft.block.quantum_computer;

import com.acikek.qcraft.world.state.QuantumComputerData;
import com.acikek.qcraft.world.state.frequency.Frequential;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;

public class QuantumComputerItem extends BlockItem {

    public QuantumComputerItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    protected boolean place(ItemPlacementContext context, BlockState state) {
        if (super.place(context, state)) {
            QuantumComputerBlockEntity entity = (QuantumComputerBlockEntity) context.getWorld().getBlockEntity(context.getBlockPos());
            if (entity != null) {
                Frequential.getFrequency(context.getStack()).ifPresent(frequency -> {
                    entity.frequency = frequency;
                    entity.markDirty();
                });
            }
            if (!context.getWorld().isClient()) {
                QuantumComputerData.get(context.getWorld()).add(context.getBlockPos(), context.getStack());
            }
            return true;
        }
        return false;
    }

    public static boolean isAvailable(ItemStack stack) {
        return stack.getItem() instanceof QuantumComputerItem
                && !stack.getOrCreateNbt().containsUuid("frequency");
    }
}
