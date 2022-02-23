package com.acikek.qcraft.recipe;

import com.acikek.qcraft.QCraft;
import com.acikek.qcraft.block.qblock.QBlock;
import com.acikek.qcraft.block.qblock.QBlockItem;
import com.acikek.qcraft.item.Essence;
import com.acikek.qcraft.item.QBlockEssence;
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
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
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

    public static MutableText formatFace(String faceName, MutableText text) {
        text.setStyle(text.getStyle().withItalic(false)).formatted(Formatting.GRAY);
        MutableText faceText = new LiteralText(" (" + faceName + ")")
                .setStyle(Style.EMPTY.withItalic(false))
                .formatted(Formatting.DARK_GRAY);
        text.append(faceText);
        return text;
    }

    public static void applyFaces(ItemStack stack, List<String> faces) {
        NbtCompound display = new NbtCompound();
        NbtList lore = new NbtList();
        for (int i = 0; i < faces.size(); i++) {
            String faceName = QBlock.Face.values()[i].name();
            stack.getOrCreateSubNbt("faces").putString(faceName, faces.get(i));
            Block block = Registry.BLOCK.get(Identifier.tryParse(faces.get(i)));
            MutableText text = formatFace(faceName, block.getName());
            lore.addElement(i, NbtString.of(Text.Serializer.toJson(text)));
        }
        display.put("Lore", lore);
        if (stack.getNbt() != null) {
            stack.getNbt().put("display", display);
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
        int essenceSlot = Essence.findSlot(inventory);
        if (essenceSlot == -1
                || essenceSlot < QBlock.Face.CENTER
                || !(inventory.getStack(essenceSlot).getItem() instanceof QBlockEssence)) {
            return false;
        }
        int diff = essenceSlot - QBlock.Face.CENTER;
        for (int i : QBlock.Face.EMPTY_SLOTS) {
            if (!inventory.getStack(i + diff).isEmpty()) {
                return false;
            }
        }
        boolean atLeastOne = false;
        for (QBlock.Face face : QBlock.Face.values()) {
            ItemStack slot = inventory.getStack(face.slot + diff);
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

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    public static void register() {
        SERIALIZER = Registry.register(Registry.RECIPE_SERIALIZER, new Identifier(QCraft.ID, "crafting_special_qblock"), new SpecialRecipeSerializer<>(QBlockRecipe::new));
    }
}
