package moe.caa.fabric.hadesgame.gameevent

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.caa.fabric.hadesgame.GameCore
import moe.caa.fabric.hadesgame.util.broadcast
import moe.caa.fabric.hadesgame.util.broadcastOverlay
import moe.caa.fabric.hadesgame.util.getActivePlayers
import net.minecraft.sounds.SoundEvents
import net.minecraft.network.chat.Component
import java.awt.Color

data object StarvationMode : AbstractGameEvent() {
    override val eventName = "饥荒模式"

    private var activeJob: Job? = null

    override suspend fun callEvent() {
        activeJob?.cancel()

        activeJob = GameCore.coroutineScope.launch {
            repeat(15) {
                delay(2_000L)
                for (player in getActivePlayers()) {
                    player.foodData.setSaturation((player.foodData.saturationLevel - 1.5f).coerceAtLeast(0.0f))
                    player.foodData.setFoodLevel((player.foodData.foodLevel - 1).coerceAtLeast(0))
                }
            }

            Component.literal("饥荒模式已结束").withColor(Color.RED.rgb).broadcastOverlay()
            SoundEvents.VILLAGER_NO.broadcast(1.0F, 1.0F)
        }

        Component.literal("饥荒模式已生效").withColor(Color.GREEN.rgb).broadcast()
        Component.literal("你的食物正在快速流失").withColor(Color.ORANGE.rgb).broadcastOverlay()
        SoundEvents.BEACON_ACTIVATE.broadcast(1.0F, 0.8F)
    }

    override suspend fun endEvent() {
        activeJob?.cancel()
    }
}
