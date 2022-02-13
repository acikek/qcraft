package com.acikek.qcraft.blocks.qblock;

import com.acikek.qcraft.QCraft;
import com.acikek.qcraft.blocks.Blocks;
import com.acikek.qcraft.world.QBlockData;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.Optional;

public class QBlock extends Block implements BlockEntityProvider {

    public static BlockEntityType<QBlockEntity> QBLOCK_ENTITY;

    public QBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new QBlockEntity(pos, state);
    }

    public static void registerBlockEntity() {
        QBLOCK_ENTITY = Registry.register(
                Registry.BLOCK_ENTITY_TYPE,
                new Identifier(QCraft.ID, "quantum_block_entity"),
                FabricBlockEntityTypeBuilder.create(QBlockEntity::new, Blocks.QUANTUM_BLOCK).build(null)
        );
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient()) {
            Optional<QBlockData.QBlockLocation> block = QBlockData.get(world).getBlock(pos, world);
            block.ifPresent(qBlockLocation -> world.setBlockState(pos, qBlockLocation.getFace(world.random.nextInt(6)).getDefaultState()));
        }
        return ActionResult.SUCCESS;
        //return super.onUse(state, world, pos, player, hand, hit);
    }
}
