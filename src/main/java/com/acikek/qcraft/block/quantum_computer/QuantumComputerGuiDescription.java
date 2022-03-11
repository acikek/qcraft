package com.acikek.qcraft.block.quantum_computer;

import com.acikek.qcraft.QCraft;
import com.acikek.qcraft.client.gui.screen.QuantumComputerScreen;
import com.acikek.qcraft.item.Items;
import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WItemSlot;
import io.github.cottonmc.cotton.gui.widget.WSprite;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import io.github.cottonmc.cotton.gui.widget.icon.TextureIcon;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;

public class QuantumComputerGuiDescription extends SyncedGuiDescription {

    public static ScreenHandlerType<QuantumComputerGuiDescription> SCREEN_HANDLER_TYPE;

    public static final int SIZE = 1;

    public static final WSprite STATUS_FAILURE = new WSprite(QCraft.id("textures/gui/warning_indicator.png"));
    public static final WSprite STATUS_SUCCESS = new WSprite(QCraft.id("textures/gui/checkmark_indicator.png"));

    public QuantumComputerGuiDescription(int syncId, PlayerInventory inventory, ScreenHandlerContext context) {
        super(SCREEN_HANDLER_TYPE, syncId, inventory, getBlockInventory(context, SIZE), getBlockPropertyDelegate(context));
        WGridPanel root = new WGridPanel();
        setRootPanel(root);
        root.setSize(175, 150);
        root.setInsets(Insets.ROOT_PANEL);
        WItemSlot itemSlot = WItemSlot.of(blockInventory, 0)
                .setFilter(stack -> stack.isOf(Items.COOLANT_CELL))
                .setIcon(new TextureIcon(QCraft.id("textures/gui/coolant_cell_slot.png")));
        root.add(itemSlot, 4, 1);
        //WButton energize = new WButton().setLabel(new TranslatableText("gui.qcraft.energize")).
        root.add(createPlayerInventoryPanel(), 0, 3);
        root.validate(this);
    }

    public static void register() {
        SCREEN_HANDLER_TYPE = ScreenHandlerRegistry.registerSimple(QCraft.id("qc_gui"), (syncId, inventory) ->
                new QuantumComputerGuiDescription(syncId, inventory, ScreenHandlerContext.EMPTY));
    }

    public static void registerClient() {
        ScreenRegistry.<QuantumComputerGuiDescription, QuantumComputerScreen>register(SCREEN_HANDLER_TYPE, (handler, inventory, title) ->
                new QuantumComputerScreen(handler, inventory.player, title));
    }
}
