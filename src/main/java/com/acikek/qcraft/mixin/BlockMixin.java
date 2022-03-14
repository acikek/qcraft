package com.acikek.qcraft.mixin;

import com.acikek.qcraft.world.state.QBlockData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(Block.class)
public abstract class BlockMixin {

    private static void setQBlockDrop(ServerWorld world, BlockPos pos, CallbackInfoReturnable<List<ItemStack>> cir) {
        QBlockData data = QBlockData.get(world, false);
        if (data.locations.removed != null) {
            if (data.locations.removed.pos.equals(pos)) {
                cir.setReturnValue(List.of(data.locations.removed.getItemStack()));
            }
            data.locations.removed = null;
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
        setQBlockDrop(world, pos, cir);
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
        setQBlockDrop(world, pos, cir);
    }

    @Inject(method = "onBreak", locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "HEAD"))
    private void removeQBlock(
            World world,
            BlockPos pos,
            BlockState state,
            PlayerEntity player,
            CallbackInfo ci
    ) {
        if (!world.isClient()) {
            QBlockData data = QBlockData.get(world, false);
            data.locations.get(pos).ifPresent(location -> data.remove(location, true));
        }
    }
}
