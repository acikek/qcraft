package com.acikek.qcraft.block.quantum_computer;

import com.acikek.qcraft.block.BlockItemProvider;
import com.acikek.qcraft.world.state.QuantumComputerData;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

import java.util.function.BiFunction;

public class QuantumComputer extends Block implements BlockItemProvider, BlockEntityProvider {

    public static final BooleanProperty ENTANGLED = BooleanProperty.of("entangled");

    public static final Settings DEFAULT_SETTINGS = FabricBlockSettings.of(Material.METAL).strength(5.0f, 10.0f);

    public QuantumComputer() {
        super(DEFAULT_SETTINGS);
        setDefaultState(getStateManager().getDefaultState().with(ENTANGLED, false));
    }

    public void remove(World world, BlockPos pos) {
        if (!world.isClient()) {
            QuantumComputerData data = QuantumComputerData.get(world);
            data.locations.get(pos).ifPresent(location -> data.remove(location, false));
        }
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        super.onBreak(world, pos, state, player);
        remove(world, pos);
    }

    @Override
    public void onDestroyedByExplosion(World world, BlockPos pos, Explosion explosion) {
        remove(world, pos);
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

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new QuantumComputerBlockEntity(pos, state);
    }
}
