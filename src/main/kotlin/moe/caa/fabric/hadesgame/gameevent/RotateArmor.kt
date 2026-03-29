package moe.caa.fabric.hadesgame.gameevent

import moe.caa.fabric.hadesgame.util.broadcast
import moe.caa.fabric.hadesgame.util.copyArmorContents
import moe.caa.fabric.hadesgame.util.eventSwap
import moe.caa.fabric.hadesgame.util.sendOverlay
import moe.caa.fabric.hadesgame.util.setArmorContents
import net.minecraft.sounds.SoundEvents
import net.minecraft.network.chat.Component
import java.awt.Color

data object RotateArmor : AbstractGameEvent() {
    override val eventName = "装备轮换"

    override suspend fun callEvent() {
        eventSwap({ it.copyArmorContents() }) { self, source, armorContents ->
            self.setArmorContents(armorContents)
            Component.literal("你已穿上 ").withColor(Color.LIGHT_GRAY.rgb)
                .append(Component.literal(source.name.string).withColor(Color.WHITE.rgb))
                .append(Component.literal(" 的装备").withColor(Color.LIGHT_GRAY.rgb))
                .sendOverlay(self)
        }

        Component.literal("装备轮换已完成").withColor(Color.GREEN.rgb).broadcast()
        SoundEvents.FOX_TELEPORT.broadcast(100F, 0F)
    }
}
