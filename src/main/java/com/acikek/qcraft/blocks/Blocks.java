package com.acikek.qcraft.blocks;

import com.acikek.qcraft.QCraft;
import com.acikek.qcraft.blocks.qblock.QBlock;
import com.acikek.qcraft.blocks.qblock.QBlockItem;
import com.acikek.qcraft.items.Items;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;
import java.util.Map;

public class Blocks {

    public static final Block QUANTUM_ORE = new QuantumOre(QuantumOre.QUANTUM_ORE_SETTINGS);
    public static final Block DEEPSLATE_QUANTUM_ORE = new QuantumOre(QuantumOre.DEEPSLATE_QUANTUM_ORE_SETTINGS);
    public static final QBlock OBSERVER_DEPENDENT_BLOCK = new QBlock(FabricBlockSettings.of(Material.STONE).strength(5.0f, 5.0f), QBlock.Type.OBSERVER_DEPENDENT);
    public static final QBlock QUANTUM_BLOCK = new QBlock(FabricBlockSettings.of(Material.STONE).strength(5.0f, 5.0f), QBlock.Type.QUANTUM);

    public static Map<String, Block> BLOCKS = new HashMap<>();

    static {
        BLOCKS.put("quantum_ore", QUANTUM_ORE);
        BLOCKS.put("deepslate_quantum_ore", DEEPSLATE_QUANTUM_ORE);
        BLOCKS.put("observer_dependent_block", OBSERVER_DEPENDENT_BLOCK);
        BLOCKS.put("quantum_block", QUANTUM_BLOCK);
    }

    public static void register(String name, Block block) {
        Registry.register(Registry.BLOCK, new Identifier(QCraft.ID, name), block);
        BlockItem blockItem = block instanceof QBlock qBlock
                ? new QBlockItem(qBlock, new FabricItemSettings().group(QCraft.ITEM_GROUP))
                : new BlockItem(block, new FabricItemSettings().group(QCraft.ITEM_GROUP));
        Items.register(name, blockItem);
    }

    public static void registerAll() {
        for (Map.Entry<String, Block> data : BLOCKS.entrySet()) {
            register(data.getKey(), data.getValue());
        }
    }
}