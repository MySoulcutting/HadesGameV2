package moe.caa.fabric.hadesgame.gameevent

import moe.caa.fabric.hadesgame.util.broadcast
import moe.caa.fabric.hadesgame.util.getActivePlayers
import moe.caa.fabric.hadesgame.util.sendOverlay
import net.minecraft.sounds.SoundEvents
import net.minecraft.network.chat.Component
import java.awt.Color
import kotlin.math.roundToInt

data object Equalizer : AbstractGameEvent() {
    override val eventName = "均贫卡"

    override suspend fun callEvent() {
        val players = getActivePlayers()
        if (players.isEmpty()) return

        val averageHealth = players.map { it.health.toDouble() }.average().toFloat().coerceAtLeast(1.0f)
        val averageFoodLevel = players.map { it.foodData.foodLevel.toDouble() }.average().roundToInt().coerceIn(1, 20)
        val averageSaturation = players.map { it.foodData.saturationLevel.toDouble() }.average().toFloat().coerceAtLeast(0.0f)

        for (player in players) {
            player.health = averageHealth.coerceIn(1.0f, player.maxHealth)
            player.foodData.setFoodLevel(averageFoodLevel)
            player.foodData.setSaturation(averageSaturation.coerceIn(0.0f, averageFoodLevel.toFloat()))

            Component.literal("你的生命与饱食已被拉平").withColor(Color.LIGHT_GRAY.rgb).sendOverlay(player)
        }

        Component.literal("均贫卡已生效").withColor(Color.GREEN.rgb).broadcast()
        SoundEvents.EXPERIENCE_ORB_PICKUP.broadcast(1.0F, 0.8F)
    }
}
