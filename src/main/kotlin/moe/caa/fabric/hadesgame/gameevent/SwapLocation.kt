package moe.caa.fabric.hadesgame.gameevent

import moe.caa.fabric.hadesgame.util.*
import net.minecraft.sounds.SoundEvents
import net.minecraft.network.chat.Component
import java.awt.Color

data object SwapLocation : AbstractGameEvent() {
    override val eventName = "交换位置"

    override suspend fun callEvent() {
        eventSwap({ it.getLocation() }) { self, source, loc ->
            self.teleport(loc)

            Component.literal("你被传送到 ").withColor(Color.LIGHT_GRAY.rgb)
                .append(Component.literal(source.name.string).withColor(Color.WHITE.rgb)).append(" 的位置")
                .sendOverlay(self)
        }

        SoundEvents.FOX_TELEPORT.broadcast(100F, 0F)
    }
}