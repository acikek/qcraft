package com.acikek.qcraft.blocks.qblock;

import com.acikek.qcraft.QCraft;
import com.acikek.qcraft.items.QBlockEssence;
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

import java.util.List;

public class QBlockRecipe extends SpecialCraftingRecipe {

    public static SpecialRecipeSerializer<QBlockRecipe> SERIALIZER;

    public QBlockRecipe(Identifier id) {
        super(id);
    }

    public static ItemStack applyNbt(ItemStack stack, List<String> faces) {
        for (int i = 0; i < faces.size(); i++) {
            stack.getOrCreateSubNbt("faces").putString(QBlock.Face.values()[i].name(), faces.get(i));
        }
        return stack;
    }

    public static ItemStack applyNbt(ItemStack stack, CraftingInventory inventory) {
        for (QBlock.Face face : QBlock.Face.values()) {
            Item slot = inventory.getStack(face.slot).getItem();
            stack.getOrCreateSubNbt("faces").putString(face.name(), Registry.ITEM.getId(slot).toString());
        }
        return stack;
    }

    @Override
    public ItemStack craft(CraftingInventory inventory) {
        QBlockEssence essence = (QBlockEssence) inventory.getStack(QBlock.Face.CENTER).getItem();
        ItemStack stack = new ItemStack(essence.getQBlock());
        return applyNbt(stack, inventory);
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= 9;
    }

    @Override
    public boolean matches(CraftingInventory inventory, World world) {
        Item item = inventory.getStack(QBlock.Face.CENTER).getItem();
        if (item instanceof QBlockEssence) {
            for (int i : QBlock.Face.EMPTY_SLOTS) {
                if (!inventory.getStack(i).isEmpty()) {
                    return false;
                }
            }
            for (QBlock.Face face : QBlock.Face.values()) {
                ItemStack slot = inventory.getStack(face.slot);
                if (slot.isEmpty()) {
                    continue;
                }
                if (!(slot.getItem() instanceof BlockItem)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    public static void register() {
        SERIALIZER = Registry.register(Registry.RECIPE_SERIALIZER, new Identifier(QCraft.ID, "crafting_special_qblock"), new SpecialRecipeSerializer<>(QBlockRecipe::new));
    }
}
