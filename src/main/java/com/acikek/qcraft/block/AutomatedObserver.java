package com.acikek.qcraft.block;

import com.acikek.qcraft.block.qblock.QBlock;
import com.acikek.qcraft.world.state.QBlockData;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.AbstractRedstoneGateBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.Random;

public class AutomatedObserver extends AbstractRedstoneGateBlock {

    public static final Settings DEFAULT_SETTINGS = FabricBlockSettings.of(Material.DECORATION)
            .breakInstantly()
            .sounds(BlockSoundGroup.WOOD);

    public AutomatedObserver() {
        super(DEFAULT_SETTINGS);
        setDefaultState(
                getStateManager().getDefaultState()
                    .with(FACING, Direction.NORTH)
                    .with(POWERED, false)
        );
    }

    @Override
    protected boolean hasPower(World world, BlockPos pos, BlockState state) {
        return getPower(world, pos, state) > 0;
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        super.scheduledTick(state, world, pos, random);
        if (!world.isClient()) {
            QBlockData data = QBlockData.get(world, false);
            data.locations.get(pos.offset(state.get(FACING).getOpposite())).ifPresent(location -> {
                if (hasPower(world, pos, state)) {
                    if (!location.observed && data.getOtherNotObserved(location)) {
                        PlayerEntity player = world.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ(), 10.0, false);
                        QBlock.Face face = location.type.pickFace(state.get(FACING), world.random);
                        data.observe(location, world, face, QBlock.Observation.AUTOMATED_OBSERVER, player);
                    }
                }
                else if (location.observed) {
                    data.unobserve(location, world, true);
                }
            });
        }
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
