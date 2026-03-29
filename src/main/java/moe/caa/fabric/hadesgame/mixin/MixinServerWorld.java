package moe.caa.fabric.hadesgame.mixin;

import moe.caa.fabric.hadesgame.event.OnSpawnEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public class MixinServerWorld {

    @Inject(method = "addFreshEntity", at = @At("HEAD"))
    private void onSpawnEntity(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        OnSpawnEntity.Companion.callEvent(entity);
    }
}
