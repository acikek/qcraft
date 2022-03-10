package com.acikek.qcraft.client.gui.screen;

import com.acikek.qcraft.block.quantum_computer.QuantumComputerGuiDescription;
import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

public class QuantumComputerScreen extends CottonInventoryScreen<QuantumComputerGuiDescription> {

    public QuantumComputerScreen(QuantumComputerGuiDescription gui, PlayerEntity player, Text title) {
        super(gui, player, title);
    }
}
