package moe.caa.fabric.hadesgame.gameevent

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.caa.fabric.hadesgame.GameCore
import moe.caa.fabric.hadesgame.util.broadcast
import moe.caa.fabric.hadesgame.util.broadcastOverlay
import moe.caa.fabric.hadesgame.util.getActivePlayers
import moe.caa.fabric.hadesgame.util.getPlayers
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
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
            Text.literal("黑暗降临已结束").withColor(Color.RED.rgb).broadcastOverlay()
            SoundEvents.ENTITY_VILLAGER_NO.broadcast(1.0F, 1.0F)
        }

        Text.literal("黑暗降临已生效").withColor(Color.GREEN.rgb).broadcast()
        Text.literal("黑暗吞噬了所有人").withColor(Color.DARK_GRAY.rgb).broadcastOverlay()
        SoundEvents.BLOCK_BEACON_ACTIVATE.broadcast(1.0F, 0.7F)
    }

    override suspend fun endEvent() {
        activeJob?.cancel()
        clearEffects()
    }

    private fun applyEffects(durationTicks: Int) {
        for (player in getActivePlayers()) {
            player.addStatusEffect(StatusEffectInstance(StatusEffects.BLINDNESS, durationTicks, 0, false, false, true))
            player.addStatusEffect(StatusEffectInstance(StatusEffects.DARKNESS, durationTicks, 0, false, false, true))
        }
    }

    private fun clearEffects() {
        for (player in getPlayers()) {
            player.removeStatusEffect(StatusEffects.BLINDNESS)
            player.removeStatusEffect(StatusEffects.DARKNESS)
        }
    }
}
