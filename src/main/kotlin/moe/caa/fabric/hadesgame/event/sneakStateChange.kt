package moe.caa.fabric.hadesgame.event

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.server.level.ServerPlayer


val sneakStateChangeEvent: Event<OnSneakStateChange> = EventFactory.createArrayBacked(
    OnSneakStateChange::class.java
) { callbacks ->
    OnSneakStateChange { player: ServerPlayer, newSneakingState: Boolean ->
        for (callback in callbacks) {
            callback.onSneakStateChange(player, newSneakingState)
        }
    }
}

fun interface OnSneakStateChange {
    fun onSneakStateChange(player: ServerPlayer, newSneakingState: Boolean)

    companion object {
        fun trigger(player: ServerPlayer, newSneakingState: Boolean) {
            sneakStateChangeEvent.invoker().onSneakStateChange(player, newSneakingState)
        }
    }
}