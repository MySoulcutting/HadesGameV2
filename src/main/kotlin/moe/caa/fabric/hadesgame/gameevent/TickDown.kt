package moe.caa.fabric.hadesgame.gameevent

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.caa.fabric.hadesgame.GameCore
import moe.caa.fabric.hadesgame.util.broadcast
import moe.caa.fabric.hadesgame.util.broadcastOverlay
import net.minecraft.sounds.SoundEvents
import net.minecraft.network.chat.Component
import java.awt.Color

data object TickDown : AbstractGameEvent() {
    override val eventName = "超级减速"

    private var activeJob: Job? = null


    override suspend fun callEvent() {
        activeJob?.cancel()

        activeJob = GameCore.coroutineScope.launch {
            GameCore.server.tickRateManager().setTickRate(10F)
            // 30 秒
            delay(1000 * 30)

            Component.literal("超级减速效果已失效").withColor(Color.GREEN.rgb).broadcastOverlay()

            SoundEvents.EXPERIENCE_ORB_PICKUP.broadcast(1F, 1F)

        }.apply {
            invokeOnCompletion {
                GameCore.server.tickRateManager().setTickRate(20F)
            }
        }
        Component.literal("超级减速效果已生效").withColor(Color.GREEN.rgb).broadcastOverlay()

        SoundEvents.BEACON_ACTIVATE.broadcast(1F, 1F)
    }

    override suspend fun endEvent() {
        activeJob?.cancel()
    }
}