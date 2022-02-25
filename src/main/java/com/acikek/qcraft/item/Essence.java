package com.acikek.qcraft.item;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;

public class Essence extends Item {

    public enum Type {
        OBSERVATION,
        SUPERPOSITION,
        ENTANGLEMENT
    }

    public final Type essenceType;

    public Essence(Settings settings, Type essenceType) {
        super(settings);
        this.essenceType = essenceType;
    }

    public static int findSlot(CraftingInventory inventory) {
        if (inventory.isEmpty()) {
            return -1;
        }
        int essenceSlot = -1;
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.getStack(i).getItem() instanceof Essence) {
                essenceSlot = i;
                break;
            }
        }
        return essenceSlot;
    }
}
