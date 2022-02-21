package com.acikek.qcraft.blocks.recipe;

import com.acikek.qcraft.QCraft;
import com.acikek.qcraft.blocks.qblock.QBlockItem;
import com.acikek.qcraft.items.Essence;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.UUID;

public class EntangledPairRecipe extends SpecialCraftingRecipe {

    public static SpecialRecipeSerializer<EntangledPairRecipe> SERIALIZER;

    public EntangledPairRecipe(Identifier id) {
        super(id);
    }

    public static void applyFrequency(ItemStack stack, UUID uuid) {
        stack.getOrCreateNbt().putUuid("frequency", uuid);
    }

    @Override
    public ItemStack craft(CraftingInventory inventory) {
        ItemStack stack = inventory.getStack(Essence.findSlot(inventory) - 1).copy();
        applyFrequency(stack, UUID.randomUUID());
        stack.setCount(2);
        return stack;
    }

    @Override
    public boolean fits(int width, int height) {
        return width >= 3;
    }

    @Override
    public boolean matches(CraftingInventory inventory, World world) {
        int essenceSlot = Essence.findSlot(inventory);
        if (essenceSlot == -1 || ((Essence) inventory.getStack(essenceSlot).getItem()).essenceType != Essence.Type.ENTANGLEMENT) {
            return false;
        }
        return QBlockItem.checkStacks(inventory.getStack(essenceSlot - 1), inventory.getStack(essenceSlot + 1));
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    public static void register() {
        SERIALIZER = Registry.register(Registry.RECIPE_SERIALIZER, new Identifier(QCraft.ID, "crafting_special_entangled_pair"), new SpecialRecipeSerializer<>(EntangledPairRecipe::new));
    }
}
