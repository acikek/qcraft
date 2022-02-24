package com.acikek.qcraft.item;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public class Goggles extends ArmorItem {

    public enum Type {
        QUANTUM("quantum"),
        ANTI_OBSERVATION("anti_observation");

        public String id;

        Type(String id) {
            this.id = id;
        }
    }

    public static class Material implements ArmorMaterial {

        public Type type;

        public Material(Type type) {
            this.type = type;
        }

        @Override
        public int getDurability(EquipmentSlot slot) {
            return -1;
        }

        @Override
        public int getProtectionAmount(EquipmentSlot slot) {
            return 0;
        }

        @Override
        public int getEnchantability() {
            return 0;
        }

        @Override
        public SoundEvent getEquipSound() {
            return SoundEvents.ITEM_ARMOR_EQUIP_CHAIN;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return null;
        }

        @Override
        public String getName() {
            return type.id + "_goggles";
        }

        @Override
        public float getToughness() {
            return 0;
        }

        @Override
        public float getKnockbackResistance() {
            return 0;
        }
    }

    public Type type;

    public Goggles(Type type) {
        super(new Material(type), EquipmentSlot.HEAD, Items.defaultSettings());
        this.type = type;
    }

    @Override
    public boolean isDamageable() {
        return false;
    }

    public static boolean isWearingGoggles(PlayerEntity player, Type type) {
        return player.hasStackEquipped(EquipmentSlot.HEAD)
                && player.getEquippedStack(EquipmentSlot.HEAD).getItem() instanceof Goggles goggles
                && goggles.type == type;
    }
}
