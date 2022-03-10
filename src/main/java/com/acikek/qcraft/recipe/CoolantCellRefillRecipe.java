package com.acikek.qcraft.recipe;

import com.acikek.qcraft.QCraft;
import com.acikek.qcraft.item.Items;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class CoolantCellRefillRecipe extends SpecialCraftingRecipe {

    public static SpecialRecipeSerializer<CoolantCellRefillRecipe> SERIALIZER;

    public CoolantCellRefillRecipe(Identifier id) {
        super(id);
    }

    @Override
    public ItemStack craft(CraftingInventory inventory) {
        return new ItemStack(Items.COOLANT_CELL);
    }

    @Override
    public boolean matches(CraftingInventory inventory, World world) {
        if (inventory.count(net.minecraft.item.Items.BLUE_ICE) != 1 || inventory.count(Items.COOLANT_CELL) != 1) {
            return false;
        }
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isOf(Items.COOLANT_CELL)) {
                if (stack.getDamage() == 0) {
                    return false;
                }
            }
            else if (!stack.isEmpty() && !stack.isOf(net.minecraft.item.Items.BLUE_ICE)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    public static void register() {
        SERIALIZER = Registry.register(Registry.RECIPE_SERIALIZER, QCraft.id("crafting_special_coolant_cell_refill"), new SpecialRecipeSerializer<>(CoolantCellRefillRecipe::new));
    }
}
