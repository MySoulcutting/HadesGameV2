package moe.caa.fabric.hadesgame.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor extends EntityAccessor {

    @Invoker("dropAllDeathLoot")
    void invokeDropAllDeathLoot(ServerLevel world, DamageSource damageSource);

    @Invoker("setLivingEntityFlag")
    void invokeSetLivingFlag(int mask, boolean value);

    @Accessor("lastDamageTaken")
    float getLastDamageTaken();
}
