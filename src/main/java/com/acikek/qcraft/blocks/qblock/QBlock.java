package com.acikek.qcraft.blocks.qblock;

import com.acikek.qcraft.world.QBlockData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;

public class QBlock extends Block  {

    public QBlock(Settings settings) {
        super(settings);
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
