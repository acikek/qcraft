package com.acikek.qcraft.commands;

import com.acikek.qcraft.world.QBlockData;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class ClearQBlocksCommand implements Command<ServerCommandSource> {

    public static final String NAME = "clear_qblocks";

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        QBlockData data = QBlockData.get(context.getSource().getWorld());
        int size = data.locations.size();
        data.locations.clear();
        context.getSource().getPlayer().sendMessage(Text.of("Removed " + size + " qBlocks from the current world"), false);
        return 0;
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal(NAME)
                .requires(source -> source.hasPermissionLevel(4))
                .executes(this));
    }
}
