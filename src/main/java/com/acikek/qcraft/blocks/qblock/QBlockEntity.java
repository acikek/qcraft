package com.acikek.qcraft.blocks.qblock;

import com.acikek.qcraft.blocks.qblock.QBlock;
import com.acikek.qcraft.blocks.qblock.QBlockRecipe;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public class QBlockEntity extends BlockEntity {

    public static String DEFAULT_FACE = "minecraft:air";

    public String[] faces = new String[6];

    public QBlockEntity(BlockPos pos, BlockState state) {
        super(QBlock.QBLOCK_ENTITY, pos, state);
    }

    public void fillFaces(ItemStack stack) {
        NbtCompound nbt = stack.getSubNbt("faces");
        if (nbt == null) {
            return;
        }
        for (int i = 0; i < faces.length; i++) {
            faces[i] = nbt.getString(QBlockRecipe.Face.values()[i].name());
        }
    }

    @Override
    protected void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        for (int i = 0; i < QBlockRecipe.Face.values().length; i++) {
            tag.putString(QBlockRecipe.Face.values()[i].name(), faces[i] != null ? faces[i] : DEFAULT_FACE);
        }
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        for (int i = 0; i < QBlockRecipe.Face.values().length; i++) {
            faces[i] = tag.getString(QBlockRecipe.Face.values()[i].name());
        }
    }
}
