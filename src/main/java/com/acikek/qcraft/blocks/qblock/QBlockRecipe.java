package com.acikek.qcraft.blocks.qblock;

import com.acikek.qcraft.QCraft;
import com.acikek.qcraft.items.QBlockEssence;
import net.minecraft.block.Block;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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

    public static ItemStack applyFaces(ItemStack stack, List<String> faces) {
        NbtCompound display = new NbtCompound();
        NbtList lore = new NbtList();
        for (int i = 0; i < faces.size(); i++) {
            stack.getOrCreateSubNbt("faces").putString(QBlock.Face.values()[i].name(), faces.get(i));
            Block block = Registry.BLOCK.get(Identifier.tryParse(faces.get(i)));
            lore.addElement(i, NbtString.of(Text.Serializer.toJson(block.getName().formatted(Formatting.RESET, Formatting.GRAY))));
        }
        display.put("Lore", lore);
        if (stack.getNbt() != null) {
            stack.getNbt().put("display", display);
        }
        return stack;
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
        return applyFaces(stack, faces);
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
            boolean atLeastOne = false;
            for (QBlock.Face face : QBlock.Face.values()) {
                ItemStack slot = inventory.getStack(face.slot);
                if (slot.isEmpty()) {
                    continue;
                }
                if (!(slot.getItem() instanceof BlockItem) || slot.getItem() instanceof QBlockItem) {
                    return false;
                }
                if (!atLeastOne) {
                    atLeastOne = true;
                }
            }
            return atLeastOne;
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
