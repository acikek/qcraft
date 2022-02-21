package com.acikek.qcraft.mixin;

import com.acikek.qcraft.world.QBlockData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(Block.class)
public abstract class BlockMixin {

    private static void setQBlockDrop(ServerWorld world, CallbackInfoReturnable<List<ItemStack>> cir) {
        QBlockData data = QBlockData.get(world, false);
        if (data.removed != null) {
            cir.setReturnValue(List.of(data.removed.getItemStack()));
        }
    }

    @Inject(method = "getDroppedStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;)Ljava/util/List;",
            cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "TAIL"))
    private static void dropQBlock(
            BlockState state,
            ServerWorld world,
            BlockPos pos,
            @Nullable BlockEntity blockEntity,
            CallbackInfoReturnable<List<ItemStack>> cir
    ) {
        setQBlockDrop(world, cir);
    }

    @Inject(method = "getDroppedStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;)Ljava/util/List;",
            cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "TAIL"))
    private static void dropQBlock(
            BlockState state,
            ServerWorld world,
            BlockPos pos,
            @Nullable BlockEntity blockEntity,
            @Nullable Entity entity,
            ItemStack stack,
            CallbackInfoReturnable<List<ItemStack>> cir,
            LootContext.Builder builder
    ) {
        setQBlockDrop(world, cir);
    }
}
