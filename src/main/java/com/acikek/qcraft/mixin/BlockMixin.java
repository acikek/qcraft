package com.acikek.qcraft.mixin;

import com.acikek.qcraft.world.QBlockData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Optional;

@Mixin(Block.class)
public abstract class BlockMixin {

    @Inject(method = "getDroppedStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;)Ljava/util/List;",
            cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "TAIL"))
    private static void dropQBlock(
            BlockState state,
            ServerWorld world,
            BlockPos pos,
            @Nullable BlockEntity blockEntity,
            CallbackInfoReturnable<List<ItemStack>> cir
    ) {
        Optional<QBlockData.QBlockLocation> qBlockData = QBlockData.get(world).getBlock(pos, world);
        qBlockData.ifPresent(qBlockLocation -> cir.setReturnValue(List.of(qBlockLocation.getItemStack())));
    }
}
