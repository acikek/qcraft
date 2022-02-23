package com.acikek.qcraft.block.qblock;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Material;

public class InertQBlock extends Block {

    public static Settings DEFAULT_SETTINGS = FabricBlockSettings.of(Material.STONE).strength(5.0f, 5.0f);

    public InertQBlock() {
        super(DEFAULT_SETTINGS);
    }
}
