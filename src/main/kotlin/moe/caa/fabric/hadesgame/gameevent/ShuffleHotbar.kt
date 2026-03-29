package moe.caa.fabric.hadesgame.gameevent

import moe.caa.fabric.hadesgame.util.broadcast
import moe.caa.fabric.hadesgame.util.copyHotbar
import moe.caa.fabric.hadesgame.util.getActivePlayers
import moe.caa.fabric.hadesgame.util.sendOverlay
import moe.caa.fabric.hadesgame.util.setHotbar
import net.minecraft.sounds.SoundEvents
import net.minecraft.network.chat.Component
import java.awt.Color

data object ShuffleHotbar : AbstractGameEvent() {
    override val eventName = "热栏洗牌"

    override suspend fun callEvent() {
        val players = getActivePlayers()
        if (players.isEmpty()) return

        for (player in players) {
            player.setHotbar(player.copyHotbar().shuffled())
            Component.literal("你的快捷栏已被打乱").withColor(Color.LIGHT_GRAY.rgb).sendOverlay(player)
        }

        Component.literal("热栏洗牌已完成").withColor(Color.GREEN.rgb).broadcast()
        SoundEvents.FOX_TELEPORT.broadcast(100F, 0F)
    }
}
