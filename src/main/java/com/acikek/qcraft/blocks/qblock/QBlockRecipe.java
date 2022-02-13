package com.acikek.qcraft.blocks.qblock;

import com.acikek.qcraft.QCraft;
import com.acikek.qcraft.blocks.Blocks;
import com.acikek.qcraft.items.Essence;
import com.google.common.collect.Iterables;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.Arrays;

public class QBlockRecipe extends SpecialCraftingRecipe {

    public static SpecialRecipeSerializer<QBlockRecipe> SERIALIZER;

    public QBlockRecipe(Identifier id) {
        super(id);
    }

    public enum Face {

        UP(0, 0),
        NORTH(1, 1),
        WEST(3, 2),
        EAST(5, 3),
        DOWN(6, 4),
        SOUTH(7, 5);

        public int slot;
        public int index;

        Face(int slot, int index) {
            this.slot = slot;
            this.index = index;
        }

        public static int[] EMPTY_SLOTS = {
                2, 8
        };
    }

    @Override
    public ItemStack craft(CraftingInventory inventory) {
        ItemStack stack = new ItemStack(Blocks.QUANTUM_BLOCK);
        for (Face face : Face.values()) {
            Item slot = inventory.getStack(face.slot).getItem();
            stack.getOrCreateSubNbt("faces").putString(face.name(), Registry.ITEM.getId(slot).toString());
        }
        return stack;
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= 9;
    }

    @Override
    public boolean matches(CraftingInventory inventory, World world) {
        Item item = inventory.getStack(4).getItem();
        if (item instanceof Essence essence) {
            for (int i : Face.EMPTY_SLOTS) {
                if (!inventory.getStack(i).isEmpty()) {
                    return false;
                }
            }
            for (Face face : Face.values()) {
                ItemStack slot = inventory.getStack(face.slot);
                if (slot.isEmpty()) {
                    continue;
                }
                if (!(slot.getItem() instanceof BlockItem)) {
                    return false;
                }
            }
            return essence.essenceType == Essence.Type.SUPERPOSITION;
        }
        return false;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    public static void register() {
        SERIALIZER = Registry.register(Registry.RECIPE_SERIALIZER, new Identifier(QCraft.ID, "crafting_special_quantum_block"), new SpecialRecipeSerializer<>(QBlockRecipe::new));
    }
}
