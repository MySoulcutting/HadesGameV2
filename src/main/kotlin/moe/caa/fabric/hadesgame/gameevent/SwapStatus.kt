package moe.caa.fabric.hadesgame.gameevent

import moe.caa.fabric.hadesgame.util.broadcast
import moe.caa.fabric.hadesgame.util.eventSwap
import moe.caa.fabric.hadesgame.util.sendOverlay
import net.minecraft.sounds.SoundEvents
import net.minecraft.network.chat.Component
import java.awt.Color

data object SwapStatus : AbstractGameEvent() {
    override val eventName = "交换状态"

    override suspend fun callEvent() {
        eventSwap({ it.health to it.foodData.foodLevel }) { self, source, status ->
            self.health = status.first
            self.foodData.foodLevel = status.second

            Component.literal("你已应用 ").withColor(Color.LIGHT_GRAY.rgb)
                .append(Component.literal(source.name.string).withColor(Color.WHITE.rgb)).append(" 的状态")
                .sendOverlay(self)
        }
        SoundEvents.FOX_TELEPORT.broadcast(100F, 0F)
    }
}