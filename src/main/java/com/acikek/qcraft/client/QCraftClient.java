package com.acikek.qcraft.client;

import com.acikek.qcraft.block.Blocks;
import com.acikek.qcraft.block.quantum_computer.QuantumComputerGuiDescription;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;

@Environment(EnvType.CLIENT)
public class QCraftClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(Blocks.AUTOMATED_OBSERVER, RenderLayer.getCutout());
        QuantumComputerGuiDescription.registerClient();
    }
}
