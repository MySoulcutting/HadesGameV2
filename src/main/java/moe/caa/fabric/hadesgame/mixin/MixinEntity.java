package moe.caa.fabric.hadesgame.mixin;

import moe.caa.fabric.hadesgame.event.OnEntityFlagChange;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class MixinEntity {

    @Inject(method = "setSharedFlag", at = @At("HEAD"), cancellable = true)
    void onSetLivingFlag(int mask, boolean value, CallbackInfo ci) {
        if (!OnEntityFlagChange.Companion.shouldContinue((Entity) (Object) this, mask, value)) {
            ci.cancel();
        }
    }
}
