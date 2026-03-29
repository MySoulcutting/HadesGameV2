package moe.caa.fabric.hadesgame.mixin;

import moe.caa.fabric.hadesgame.event.OnSneakStateChange;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class MixinServerPlayNetworkHandler {

    @Shadow
    public ServerPlayer player;

    @Inject(method = "handlePlayerInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;setShiftKeyDown(Z)V"))
    private void onSetSneaking(ServerboundPlayerInputPacket packet, CallbackInfo ci) {
        if (player.isShiftKeyDown() != packet.input().shift()) {
            OnSneakStateChange.Companion.trigger(player, packet.input().shift());
        }
    }
}
