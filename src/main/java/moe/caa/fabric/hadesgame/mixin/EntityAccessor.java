package moe.caa.fabric.hadesgame.mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityAccessor {

    @Invoker("setSharedFlag")
    void invokeSetSharedFlag(int mask, boolean value);

    @Invoker("getSharedFlag")
    boolean invokeGetSharedFlag(int mask);
}
