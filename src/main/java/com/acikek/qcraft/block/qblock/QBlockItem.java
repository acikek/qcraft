package com.acikek.qcraft.block.qblock;

import com.acikek.qcraft.item.Goggles;
import com.acikek.qcraft.world.state.QCraftData;
import com.acikek.qcraft.world.state.location.QBlockLocation;
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

    public QBlock getQBlock() {
        return (QBlock) getBlock();
    }

    @Override
    protected boolean place(ItemPlacementContext context, BlockState state) {
        BlockState inertState = getQBlock().type.resolveInert().getDefaultState();
        boolean result = super.place(context, inertState);
        if (context.getWorld().isClient()) {
            return result;
        }
        if (result) {
            QCraftData data = QCraftData.get(context.getWorld(), true);
            QBlockLocation added = data.addBlock(getQBlock().type, context.getBlockPos(), context.getStack());
            if (added == null) {
                return false;
            }
            if (context.getPlayer() != null && !Goggles.isWearingGoggles(context.getPlayer(), Goggles.Type.ANTI_OBSERVATION)) {
                data.pseudoObserve(added, context.getWorld(), context.getPlayer());
            }
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
