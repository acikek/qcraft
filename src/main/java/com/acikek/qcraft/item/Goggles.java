package com.acikek.qcraft.item;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterials;

public class Goggles extends ArmorItem {

    public enum Type {
        QUANTUM,
        ANTI_OBSERVATION
    }

    public Type type;

    public Goggles(Type type) {
        super(ArmorMaterials.LEATHER, EquipmentSlot.HEAD, Items.defaultSettings());
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
