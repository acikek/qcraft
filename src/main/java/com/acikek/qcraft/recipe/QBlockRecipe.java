package com.acikek.qcraft.recipe;

import com.acikek.qcraft.QCraft;
import com.acikek.qcraft.block.qblock.QBlock;
import com.acikek.qcraft.block.qblock.QBlockItem;
import com.acikek.qcraft.item.QBlockEssence;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class QBlockRecipe extends SpecialCraftingRecipe {

    public static SpecialRecipeSerializer<QBlockRecipe> SERIALIZER;

    public QBlockRecipe(Identifier id) {
        super(id);
    }

    public static void applyFaces(ItemStack stack, List<String> faces) {
        for (int i = 0; i < faces.size(); i++) {
            String faceName = QBlock.Face.values()[i].name();
            stack.getOrCreateSubNbt("faces").putString(faceName, faces.get(i));
        }
    }

    @Override
    public ItemStack craft(CraftingInventory inventory) {
        QBlockEssence essence = (QBlockEssence) inventory.getStack(QBlock.Face.CENTER).getItem();
        List<String> faces = new ArrayList<>();
        for (QBlock.Face face : QBlock.Face.values()) {
            Item item = inventory.getStack(face.slot).getItem();
            faces.add(Registry.ITEM.getId(item).toString());
        }
        ItemStack stack = new ItemStack(essence.getQBlock());
        applyFaces(stack, faces);
        return stack;
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= 9;
    }

    @Override
    public boolean matches(CraftingInventory inventory, World world) {
        if (!(inventory.getStack(QBlock.Face.CENTER).getItem() instanceof QBlockEssence)) {
            return false;
        }
        for (int i : QBlock.Face.EMPTY_SLOTS) {
            if (!inventory.getStack(i).isEmpty()) {
                return false;
            }
        }
        Item faceItem = null;
        boolean oneDifferent = false;
        for (QBlock.Face face : QBlock.Face.values()) {
            ItemStack stack = inventory.getStack(face.slot);
            Item item = stack.getItem();
            if (!stack.isEmpty() && (!(item instanceof BlockItem) || item instanceof QBlockItem)) {
                return false;
            }
            if (faceItem == null) {
                faceItem = item;
            }
            else if (item != faceItem) {
                oneDifferent = true;
            }
        }
        return oneDifferent;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    public static void register() {
        SERIALIZER = Registry.register(Registry.RECIPE_SERIALIZER, QCraft.id("crafting_special_qblock"), new SpecialRecipeSerializer<>(QBlockRecipe::new));
    }
}
