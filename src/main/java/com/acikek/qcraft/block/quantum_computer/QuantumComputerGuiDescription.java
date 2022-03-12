package com.acikek.qcraft.block.quantum_computer;

import com.acikek.qcraft.QCraft;
import com.acikek.qcraft.client.gui.screen.QuantumComputerScreen;
import com.acikek.qcraft.item.Items;
import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.networking.NetworkSide;
import io.github.cottonmc.cotton.gui.networking.ScreenNetworking;
import io.github.cottonmc.cotton.gui.widget.*;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import io.github.cottonmc.cotton.gui.widget.icon.TextureIcon;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class QuantumComputerGuiDescription extends SyncedGuiDescription {

    public static ScreenHandlerType<QuantumComputerGuiDescription> SCREEN_HANDLER_TYPE;

    public static final int SIZE = 1;

    public static final WSprite STATUS_FAILURE = new WSprite(QCraft.id("textures/gui/warning_indicator.png"));
    public static final WSprite STATUS_SUCCESS = new WSprite(QCraft.id("textures/gui/checkmark_indicator.png"));

    public static final Identifier ENERGIZE = QCraft.id("gui.energize");

    public void addStatus(WPlainPanel root, WSprite sprite, Text text) {
        root.add(sprite, 0, 18, 18, 18);
        root.add(new WText(text), 27, 18, 135, 9);
    }

    public QuantumComputerGuiDescription(int syncId, PlayerInventory inventory, ScreenHandlerContext context, QuantumComputer.Result<QuantumComputer.Result.Teleportation> result) {
        super(SCREEN_HANDLER_TYPE, syncId, inventory, getBlockInventory(context, SIZE), getBlockPropertyDelegate(context));
        WPlainPanel root = new WPlainPanel();
        setRootPanel(root);
        root.setSize(175, 150);
        root.setInsets(Insets.ROOT_PANEL);
        WItemSlot itemSlot = WItemSlot.of(blockInventory, 0)
                .setFilter(stack -> stack.isOf(Items.COOLANT_CELL))
                .setIcon(new TextureIcon(QCraft.id("textures/gui/coolant_cell_slot.png")));
        WButton energize = new WButton().setLabel(new TranslatableText("gui.qcraft.energize")).setEnabled(false);
        result.error.ifPresent(error -> addStatus(root, STATUS_FAILURE, error.getText()));
        result.value.ifPresent(teleportation -> {
            addStatus(root, STATUS_SUCCESS, new TranslatableText("gui.qcraft.success.quantum_computer"));
            energize.setEnabled(true).setOnClick(() -> {
                ItemStack stack = blockInventory.getStack(0);
                if (stack.isOf(Items.COOLANT_CELL) && stack.getDamage() < stack.getMaxDamage()) {
                    ScreenNetworking.of(this, NetworkSide.CLIENT).send(ENERGIZE, buf -> buf.encode(QuantumComputer.Result.Teleportation.CODEC, teleportation));
                    teleportation.toBoth(pos -> QuantumComputer.playEffects(world, pos));
                }
                else {
                    teleportation.toBoth(pos -> QuantumComputer.playSound(world, pos, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE));
                }
            });
        });
        ScreenNetworking.of(this, NetworkSide.SERVER).receive(ENERGIZE, buf -> {
            QuantumComputer.teleport(world, inventory.player, buf.decode(QuantumComputer.Result.Teleportation.CODEC));
            ItemStack stack = blockInventory.getStack(0);
            stack.setDamage(stack.getDamage() + 1);
        });
        root.add(itemSlot, 0, 46);
        root.add(energize, 36, 45, 108, 20);
        root.add(createPlayerInventoryPanel(), 0, 72);
        root.validate(this);
    }

    public static void register() {
        SCREEN_HANDLER_TYPE = ScreenHandlerRegistry.registerExtended(QCraft.id("qc_gui"), (syncId, inventory, buf) ->
                new QuantumComputerGuiDescription(syncId, inventory, ScreenHandlerContext.EMPTY, buf.decode(QuantumComputer.Result.CODEC)));
    }

    public static void registerClient() {
        ScreenRegistry.<QuantumComputerGuiDescription, QuantumComputerScreen>register(SCREEN_HANDLER_TYPE, (handler, inventory, title) ->
                new QuantumComputerScreen(handler, inventory.player, title));
    }
}
