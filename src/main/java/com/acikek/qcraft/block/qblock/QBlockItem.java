package com.acikek.qcraft.block.qblock;

import com.acikek.qcraft.block.FrequentialItem;
import com.acikek.qcraft.item.Goggles;
import com.acikek.qcraft.world.state.QBlockData;
import com.acikek.qcraft.world.state.location.QBlockLocation;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class QBlockItem extends FrequentialItem {

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
            QBlockData data = QBlockData.get(context.getWorld(), true);
            QBlockLocation added = data.add(getQBlock().type, context.getBlockPos(), context.getStack());
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

    public static boolean equals(ItemStack stack, ItemStack other) {
        if (stack.getItem() instanceof QBlockItem left && other.getItem() instanceof QBlockItem right) {
            return QBlock.getBlockFromItem(left).type == QBlock.getBlockFromItem(right).type
                    && Arrays.equals(getFaces(stack), getFaces(other));
        }
        return false;
    }

    public static MutableText formatFace(MutableText face, MutableText block) {
        block.setStyle(block.getStyle().withItalic(false)).formatted(Formatting.GRAY);
        MutableText faceText = new LiteralText(" (").append(face).append(")")
                .setStyle(Style.EMPTY.withItalic(false))
                .formatted(Formatting.DARK_GRAY);
        block.append(faceText);
        return block;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        String[] faces = getFaces(stack);
        if (faces == null) {
            return;
        }
        for (int i = 0; i < faces.length; i++) {
            MutableText face = QBlock.Face.values()[i].text;
            MutableText block = Registry.BLOCK.get(Identifier.tryParse(faces[i])).getName();
            tooltip.add(formatFace(face, block));
        }
    }
}
