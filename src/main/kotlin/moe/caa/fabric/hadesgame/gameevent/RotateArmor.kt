package moe.caa.fabric.hadesgame.gameevent

import moe.caa.fabric.hadesgame.util.broadcast
import moe.caa.fabric.hadesgame.util.copyArmorContents
import moe.caa.fabric.hadesgame.util.eventSwap
import moe.caa.fabric.hadesgame.util.sendOverlay
import moe.caa.fabric.hadesgame.util.setArmorContents
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import java.awt.Color

data object RotateArmor : AbstractGameEvent() {
    override val eventName = "装备轮换"

    override suspend fun callEvent() {
        eventSwap({ it.copyArmorContents() }) { self, source, armorContents ->
            self.setArmorContents(armorContents)
            Text.literal("你已穿上 ").withColor(Color.LIGHT_GRAY.rgb)
                .append(Text.literal(source.name.literalString).withColor(Color.WHITE.rgb))
                .append(Text.literal(" 的装备").withColor(Color.LIGHT_GRAY.rgb))
                .sendOverlay(self)
        }

        Text.literal("装备轮换已完成").withColor(Color.GREEN.rgb).broadcast()
        SoundEvents.ENTITY_FOX_TELEPORT.broadcast(100F, 0F)
    }
}
