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
import net.minecraft.world.World;

import java.util.Random;

public class AutomatedObserver extends AbstractRedstoneGateBlock {

    public static Settings DEFAULT_SETTINGS = FabricBlockSettings.of(Material.DECORATION)
            .breakInstantly()
            .sounds(BlockSoundGroup.METAL);

    public AutomatedObserver() {
        super(DEFAULT_SETTINGS);
        setDefaultState(
                getStateManager().getDefaultState()
                    .with(FACING, Direction.SOUTH)
                    .with(POWERED, false)
        );
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (!state.get(POWERED)) {
            return;
        }
        Direction direction = state.get(FACING);
        double d = (double) pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.2;
        double e = (double) pos.getY() + 0.4 + (random.nextDouble() - 0.5) * 0.2;
        double f = (double) pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.2;
        float g = -5.0f;
        double h = (g /= 16.0f) * (float) direction.getOffsetX();
        double i = g * (float) direction.getOffsetZ();
        world.addParticle(QuantumOre.DUST_PARTICLE, d + h, e, f + i, 0.0, 0.0, 0.0);
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
