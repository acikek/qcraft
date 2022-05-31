package com.acikek.qcraft.command;

import com.acikek.qcraft.QCraft;
import com.acikek.qcraft.block.qblock.QBlockItem;
import com.acikek.qcraft.world.state.QBlockData;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.command.CommandException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class QCraftCommand implements Command<ServerCommandSource> {

    public static final String NAME = "qcraft";

    public static final Map<String, String> QUOTES = Map.of(
            "The cosmos is within us. We are a way for the universe to know itself.", "Carl Sagan",
            "Not only does God play dice but... he sometimes throws them where they cannot be seen.", "Stephen Hawking",
            "Not only is the Universe stranger than we think, it is stranger than we can think.", "Werner Heisenberg",
            "It is a quantum world. We are just living in it.", "Rodrigue Rizk",
            "You are my qubit! I shall never see you, but I know you do exist...", "Rodrigue Rizk",
            "Quantum mechanics makes absolutely no sense.", "Roger Penrose",
            "We must be clear that when it comes to atoms, language can be used only as in poetry.", "Niels Bohr"
    );

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Optional<ModContainer> modResult = FabricLoader.getInstance().getModContainer(QCraft.ID);
        if (modResult.isEmpty()) {
            return 1;
        }
        ModContainer mod = modResult.get();
        ModMetadata metadata = mod.getMetadata();
        MutableText title = Text.Serializer.fromJson("{\"text\":\"" + metadata.getName() + "\",\"color\":\"#1ec76a\"}");
        if (title == null) {
            return 1;
        }
        title.styled(style -> style.withBold(true));
        Text version = new LiteralText(" v" + metadata.getVersion().getFriendlyString())
                .styled(style -> style.withBold(false))
                .formatted(Formatting.GREEN);
        title.append(version);
        Map.Entry<String, String> quote = QUOTES.entrySet().stream().toList().get(context.getSource().getWorld().random.nextInt(QUOTES.size()));
        MutableText quoteText = new LiteralText("\"" + quote.getKey() + "\"")
                .formatted(Formatting.GRAY);
        Text author = new LiteralText(" - " + quote.getValue())
                .styled(style -> style.withItalic(true));
        quoteText.append(author);
        context.getSource().getPlayer().sendMessage(title, false);
        context.getSource().getPlayer().sendMessage(quoteText, false);
        return 0;
    }

    public enum ClearType {

        LOCATIONS("locations"),
        FREQUENCIES("frequencies"),
        ALL("all");

        public Text text;

        ClearType(String key) {
            text = new TranslatableText("command.qcraft.clear." + key);
        }

        public int clear(QBlockData data) {
            return switch (this) {
                case LOCATIONS -> data.clearLocations();
                case FREQUENCIES -> data.clearFrequencies();
                case ALL -> data.reset();
            };
        }

        public static final String[] SUGGESTS = {
                "locations", "frequencies"
        };
    }

    public static class ClearTypeSuggestionProvider implements SuggestionProvider<ServerCommandSource> {

        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
            for (String type : ClearType.SUGGESTS) {
                builder.suggest(type);
            }
            return builder.buildFuture();
        }
    }

    public int clear(CommandContext<ServerCommandSource> context, String type) throws CommandSyntaxException {
        try {
            ClearType clearType = type != null ? ClearType.valueOf(type) : ClearType.ALL;
            QBlockData data = QBlockData.get(context.getSource().getWorld(), false);
            int cleared = clearType.clear(data);
            Text text = new TranslatableText("command.qcraft.clear.success", cleared, clearType.text);
            context.getSource().getPlayer().sendMessage(text, false);
            return 0;
        }
        catch (IllegalArgumentException e) {
            throw new CommandException(new TranslatableText("command.qcraft.clear.failure"));
        }
    }

    public int kit(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        PlayerEntity player = EntityArgumentType.getPlayer(context, "player");
        for (ItemStack base : QBlockItem.getPylonBases()) {
            player.giveItemStack(base);
        }
        return 0;
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal(NAME)
                        .then(CommandManager.literal("clear")
                                .then(CommandManager.argument("type", StringArgumentType.string())
                                        .suggests(new ClearTypeSuggestionProvider())
                                        .executes(ctx -> clear(ctx, StringArgumentType.getString(ctx, "type").toUpperCase())))
                                .executes(ctx -> clear(ctx, null)))
                        .then(CommandManager.literal("kit")
                                .then(CommandManager.argument("player", EntityArgumentType.player())
                                        .executes(this::kit)))
                .requires(source -> source.hasPermissionLevel(4))
                .executes(this));
    }
}
