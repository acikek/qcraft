package com.acikek.qcraft.block;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.AbstractRedstoneGateBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

public class AutomatedObserver extends AbstractRedstoneGateBlock {

    public static Settings DEFAULT_SETTINGS = FabricBlockSettings.of(Material.DECORATION)
            .breakInstantly()
            .sounds(BlockSoundGroup.METAL);

    public AutomatedObserver() {
        super(DEFAULT_SETTINGS);
        getStateManager().getDefaultState()
                .with(FACING, Direction.SOUTH)
                .with(POWERED, false);
    }

    @Override
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return 0;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    @Override
    protected int getUpdateDelayInternal(BlockState state) {
        return 0;
    }
}
