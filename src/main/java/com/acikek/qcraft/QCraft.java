package com.acikek.qcraft;

import com.acikek.qcraft.advancement.Criteria;
import com.acikek.qcraft.block.Blocks;
import com.acikek.qcraft.block.QuantumOre;
import com.acikek.qcraft.block.quantum_computer.QuantumComputerBlockEntity;
import com.acikek.qcraft.block.quantum_computer.QuantumComputerGuiDescription;
import com.acikek.qcraft.command.QCraftCommand;
import com.acikek.qcraft.item.Items;
import com.acikek.qcraft.recipe.CoolantCellRefillRecipe;
import com.acikek.qcraft.recipe.EntangledPairRecipe;
import com.acikek.qcraft.recipe.QBlockRecipe;
import com.acikek.qcraft.sound.Sounds;
import com.acikek.qcraft.world.QBlockTickListener;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class QCraft implements ModInitializer {

    public static final String ID = "qcraft";

    public static final ItemGroup ITEM_GROUP = FabricItemGroupBuilder.create(id("main"))
            .icon(() -> new ItemStack(Items.QUANTUM_DUST))
            .build();

    public static final Logger LOGGER = LogManager.getLogger();

    public static Identifier id(String name) {
        return new Identifier(ID, name);
    }

    public static String uid(String item) {
        return ID + "_" + item;
    }

    @Override
    public void onInitialize() {
        QCraft.LOGGER.info("Initializing qCraft");
        Sounds.registerAll();
        Blocks.registerAll();
        Items.registerAll();
        Criteria.registerAll();
        QBlockRecipe.register();
        EntangledPairRecipe.register();
        CoolantCellRefillRecipe.register();
        QuantumOre.createFeatures();
        QuantumOre.registerFeatures();
        QuantumComputerBlockEntity.register();
        QuantumComputerGuiDescription.register();
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> new QCraftCommand().register(dispatcher));
        ServerTickEvents.START_WORLD_TICK.register(new QBlockTickListener());
    }
}
