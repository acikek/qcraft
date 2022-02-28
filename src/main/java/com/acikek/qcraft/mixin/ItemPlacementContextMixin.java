package com.acikek.qcraft.mixin;

import com.acikek.qcraft.world.state.QCraftData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ItemPlacementContext.class)
public abstract class ItemPlacementContextMixin extends ItemUsageContext {

    public ItemPlacementContextMixin(PlayerEntity player, Hand hand, BlockHitResult hit) {
        super(player, hand, hit);
    }

    @Shadow public abstract BlockPos getBlockPos();

    @Inject(method = "canPlace", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "HEAD"))
    private void cancelIfQBlock(CallbackInfoReturnable<Boolean> cir) {
        if (!getWorld().isClient() && QCraftData.get(getWorld(), true).hasBlock(getBlockPos())) {
            cir.setReturnValue(false);
        }
    }
}
