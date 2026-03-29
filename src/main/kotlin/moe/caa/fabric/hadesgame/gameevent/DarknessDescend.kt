package moe.caa.fabric.hadesgame.gameevent

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.caa.fabric.hadesgame.GameCore
import moe.caa.fabric.hadesgame.util.broadcast
import moe.caa.fabric.hadesgame.util.broadcastOverlay
import moe.caa.fabric.hadesgame.util.getActivePlayers
import moe.caa.fabric.hadesgame.util.getPlayers
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.sounds.SoundEvents
import net.minecraft.network.chat.Component
import java.awt.Color

data object DarknessDescend : AbstractGameEvent() {
    override val eventName = "黑暗降临"

    private var activeJob: Job? = null

    override suspend fun callEvent() {
        activeJob?.cancel()
        clearEffects()
        applyEffects(30 * 20)

        activeJob = GameCore.coroutineScope.launch {
            delay(30_000L)
            clearEffects()
            Component.literal("黑暗降临已结束").withColor(Color.RED.rgb).broadcastOverlay()
            SoundEvents.VILLAGER_NO.broadcast(1.0F, 1.0F)
        }

        Component.literal("黑暗降临已生效").withColor(Color.GREEN.rgb).broadcast()
        Component.literal("黑暗吞噬了所有人").withColor(Color.DARK_GRAY.rgb).broadcastOverlay()
        SoundEvents.BEACON_ACTIVATE.broadcast(1.0F, 0.7F)
    }

    override suspend fun endEvent() {
        activeJob?.cancel()
        clearEffects()
    }

    private fun applyEffects(durationTicks: Int) {
        for (player in getActivePlayers()) {
            player.addEffect(MobEffectInstance(MobEffects.BLINDNESS, durationTicks, 0, false, false, true))
            player.addEffect(MobEffectInstance(MobEffects.DARKNESS, durationTicks, 0, false, false, true))
        }
    }

    private fun clearEffects() {
        for (player in getPlayers()) {
            player.removeEffect(MobEffects.BLINDNESS)
            player.removeEffect(MobEffects.DARKNESS)
        }
    }
}
