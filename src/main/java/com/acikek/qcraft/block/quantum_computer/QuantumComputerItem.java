package com.acikek.qcraft.block.quantum_computer;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class QuantumComputerItem extends BlockItem {

    public QuantumComputerItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    protected boolean place(ItemPlacementContext context, BlockState state) {
        if (super.place(context, state)) {
            QuantumComputerBlockEntity entity = (QuantumComputerBlockEntity) context.getWorld().getBlockEntity(context.getBlockPos());
            if (entity != null) {
                NbtCompound nbt = context.getStack().getOrCreateNbt();
                if (nbt != null && nbt.containsUuid("frequency")) {
                    entity.frequency = nbt.getUuid("frequency");
                }
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
