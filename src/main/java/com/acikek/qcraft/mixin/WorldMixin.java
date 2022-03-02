package com.acikek.qcraft.mixin;

import com.acikek.qcraft.world.state.QCraftData;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(World.class)
public abstract class WorldMixin {

    @Shadow public abstract boolean isClient();

    @Inject(method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;II)Z",
            cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "HEAD"))
    private void checkQBlock(
            BlockPos pos,
            BlockState state,
            int flags,
            int maxUpdateDepth,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!isClient()) {
            World world = (World) (Object) this;
            QCraftData data = QCraftData.get((World) (Object) this, false);
            if (!data.settingBlock) {
                data.getBlock(pos).ifPresent(location -> {
                    if (!world.getBlockState(pos).isOf(state.getBlock())) {
                        data.removeBlock(location, false);
                    }
                });
            }
            else {
                data.settingBlock = false;
            }
        }
    }
}
