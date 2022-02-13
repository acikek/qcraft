package com.acikek.qcraft.items;

import net.minecraft.item.Item;

public class Essence extends Item {

    public enum Type {
        OBSERVATION,
        SUPERPOSITION,
        ENTANGLEMENT
    }

    public Type essenceType;

    public Essence(Settings settings, Type essenceType) {
        super(settings);
        this.essenceType = essenceType;
    }
}
