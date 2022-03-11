package com.acikek.qcraft.block;

import com.acikek.qcraft.QCraft;
import com.acikek.qcraft.block.qblock.InertQBlock;
import com.acikek.qcraft.block.qblock.QBlock;
import com.acikek.qcraft.block.quantum_computer.QuantumComputer;
import com.acikek.qcraft.item.Items;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;
import java.util.Map;

public class Blocks {

    public static final Block QUANTUM_ORE = new QuantumOre(QuantumOre.QUANTUM_ORE_SETTINGS);
    public static final Block DEEPSLATE_QUANTUM_ORE = new QuantumOre(QuantumOre.DEEPSLATE_QUANTUM_ORE_SETTINGS);
    public static final Block QUANTUM_DUST_BLOCK = new Block(FabricBlockSettings.of(Material.STONE).luminance(1).strength(1.0f));
    public static final InertQBlock INERT_OBSERVER_DEPENDENT_BLOCK = new InertQBlock();
    public static final InertQBlock INERT_QUANTUM_BLOCK = new InertQBlock();
    public static final QBlock OBSERVER_DEPENDENT_BLOCK = new QBlock(QBlock.Type.OBSERVER_DEPENDENT);
    public static final QBlock QUANTUM_BLOCK = new QBlock(QBlock.Type.QUANTUM);
    public static final AutomatedObserver AUTOMATED_OBSERVER = new AutomatedObserver();
    public static final QuantumComputer QUANTUM_COMPUTER = new QuantumComputer();

    public static final Map<String, Block> BLOCKS = new HashMap<>();

    static {
        BLOCKS.put("quantum_ore", QUANTUM_ORE);
        BLOCKS.put("deepslate_quantum_ore", DEEPSLATE_QUANTUM_ORE);
        BLOCKS.put("quantum_dust_block", QUANTUM_DUST_BLOCK);
        BLOCKS.put("inert_observer_dependent_block", INERT_OBSERVER_DEPENDENT_BLOCK);
        BLOCKS.put("inert_quantum_block", INERT_QUANTUM_BLOCK);
        BLOCKS.put("observer_dependent_block", OBSERVER_DEPENDENT_BLOCK);
        BLOCKS.put("quantum_block", QUANTUM_BLOCK);
        BLOCKS.put("automated_observer", AUTOMATED_OBSERVER);
        BLOCKS.put("quantum_computer", QUANTUM_COMPUTER);
    }

    public static void register(String name, Block block) {
        Registry.register(Registry.BLOCK, QCraft.id(name), block);
        BlockItem blockItem = block instanceof BlockItemProvider provider
                ? provider.getBlockItem().apply(block, Items.defaultSettings())
                : new BlockItem(block, Items.defaultSettings());
        Items.register(name, blockItem);
    }

    public static void registerAll() {
        for (Map.Entry<String, Block> data : BLOCKS.entrySet()) {
            register(data.getKey(), data.getValue());
        }
    }
}
