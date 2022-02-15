package com.acikek.qcraft.items;

import com.acikek.qcraft.QCraft;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;
import java.util.Map;

public class Items {

    public static final Item QUANTUM_DUST = new Item(defaultSettings());
    public static final QBlockEssence OBSERVATION_ESSENCE = new QBlockEssence(defaultSettings(), Essence.Type.OBSERVATION);
    public static final QBlockEssence SUPERPOSITION_ESSENCE = new QBlockEssence(defaultSettings(), Essence.Type.SUPERPOSITION);
    public static final Essence ENTANGLEMENT_ESSENCE = new Essence(defaultSettings(), Essence.Type.ENTANGLEMENT);

    public static FabricItemSettings defaultSettings() {
        return new FabricItemSettings().group(QCraft.ITEM_GROUP);
    }

    public static final HashMap<String, Item> ITEMS = new HashMap<>();

    static {
        ITEMS.put("quantum_dust", QUANTUM_DUST);
        ITEMS.put("observation_essence", OBSERVATION_ESSENCE);
        ITEMS.put("superposition_essence", SUPERPOSITION_ESSENCE);
        ITEMS.put("entanglement_essence", ENTANGLEMENT_ESSENCE);
    }

    public static void register(String name, Item item) {
        Registry.register(Registry.ITEM, new Identifier(QCraft.ID, name), item);
    }

    public static void registerAll() {
        for (Map.Entry<String, Item> data : ITEMS.entrySet()) {
            register(data.getKey(), data.getValue());
        }
    }
}