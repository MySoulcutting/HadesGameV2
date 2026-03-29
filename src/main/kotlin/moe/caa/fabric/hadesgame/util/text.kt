package moe.caa.fabric.hadesgame.util

import moe.caa.fabric.hadesgame.GameCore
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvent

fun Component.broadcast() {
    GameCore.server.sendSystemMessage(this)
    for (player in getPlayers()) {
        player.sendSystemMessage(this, false)
    }
}

fun Component.broadcastOverlay() {
    for (player in getPlayers()) {
        player.sendOverlayMessage(this)
    }
}

fun Component.sendOverlay(spe: ServerPlayer) {
    spe.sendOverlayMessage(this)
}

fun SoundEvent.playSound(spe: ServerPlayer, volume: Float = 1.0F, pitch: Float = spe.xRot) {
    spe.playSound(this, volume, pitch)
}

fun SoundEvent.broadcast(volume: Float, pitch: Float) {
    for (player in getPlayers()) {
        playSound(player, volume, pitch)
    }
}
