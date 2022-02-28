package com.acikek.qcraft.block.quantum_computer;

import com.acikek.qcraft.block.BlockItemProvider;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.explosion.Explosion;

import java.util.function.BiFunction;

public class QuantumComputer extends Block implements BlockItemProvider {

    public static final BooleanProperty ENTANGLED = BooleanProperty.of("entangled");

    public static final Settings DEFAULT_SETTINGS = FabricBlockSettings.of(Material.METAL).strength(5.0f, 10.0f);

    public QuantumComputer() {
        super(DEFAULT_SETTINGS);
        setDefaultState(getStateManager().getDefaultState().with(ENTANGLED, false));
    }

    @Override
    public void onBroken(WorldAccess world, BlockPos pos, BlockState state) {
        // TODO
    }

    @Override
    public void onDestroyedByExplosion(World world, BlockPos pos, Explosion explosion) {
        // TODO
    }

    @Override
    public PistonBehavior getPistonBehavior(BlockState state) {
        return PistonBehavior.BLOCK;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(ENTANGLED);
    }

    @Override
    public BiFunction<Block, Item.Settings, BlockItem> getBlockItem() {
        return QuantumComputerItem::new;
    }
}
