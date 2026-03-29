package moe.caa.fabric.hadesgame.gameevent

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.caa.fabric.hadesgame.GameCore
import moe.caa.fabric.hadesgame.util.broadcast
import moe.caa.fabric.hadesgame.util.broadcastOverlay
import moe.caa.fabric.hadesgame.util.getActivePlayers
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
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
                    player.hungerManager.saturationLevel = (player.hungerManager.saturationLevel - 1.5f).coerceAtLeast(0.0f)
                    player.hungerManager.foodLevel = (player.hungerManager.foodLevel - 1).coerceAtLeast(0)
                }
            }

            Text.literal("饥荒模式已结束").withColor(Color.RED.rgb).broadcastOverlay()
            SoundEvents.ENTITY_VILLAGER_NO.broadcast(1.0F, 1.0F)
        }

        Text.literal("饥荒模式已生效").withColor(Color.GREEN.rgb).broadcast()
        Text.literal("你的食物正在快速流失").withColor(Color.ORANGE.rgb).broadcastOverlay()
        SoundEvents.BLOCK_BEACON_ACTIVATE.broadcast(1.0F, 0.8F)
    }

    override suspend fun endEvent() {
        activeJob?.cancel()
    }
}
