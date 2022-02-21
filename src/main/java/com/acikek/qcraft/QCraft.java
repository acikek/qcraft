package com.acikek.qcraft;

import com.acikek.qcraft.blocks.Blocks;
import com.acikek.qcraft.blocks.recipe.EntangledPairRecipe;
import com.acikek.qcraft.blocks.recipe.QBlockRecipe;
import com.acikek.qcraft.commands.ClearQBlocksCommand;
import com.acikek.qcraft.items.Items;
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

    public static ItemGroup ITEM_GROUP = FabricItemGroupBuilder.create(
            new Identifier(ID, "main"))
            .icon(() -> new ItemStack(Items.QUANTUM_DUST))
            .build();

    public static Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitialize() {
        Blocks.registerAll();
        Items.registerAll();
        QBlockRecipe.register();
        EntangledPairRecipe.register();

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            new ClearQBlocksCommand().register(dispatcher);
        });

        ServerTickEvents.START_WORLD_TICK.register(new QBlockTickListener());
    }
}
