package com.acikek.qcraft;

import com.acikek.qcraft.blocks.Blocks;
import com.acikek.qcraft.blocks.qblock.QBlockRecipe;
import com.acikek.qcraft.commands.ClearQBlocksCommand;
import com.acikek.qcraft.items.Items;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class QCraft implements ModInitializer {

    public static final String ID = "qcraft";

    public static ItemGroup ITEM_GROUP = FabricItemGroupBuilder.create(
            new Identifier(ID, "main"))
            .icon(() -> new ItemStack(Items.QUANTUM_DUST))
            .build();

    @Override
    public void onInitialize() {
        Blocks.registerAll();
        Items.registerAll();
        QBlockRecipe.register();

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            new ClearQBlocksCommand().register(dispatcher);
        });
    }
}
