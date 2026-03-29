package moe.caa.fabric.hadesgame.gameevent

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.caa.fabric.hadesgame.GameCore
import moe.caa.fabric.hadesgame.util.broadcast
import moe.caa.fabric.hadesgame.util.broadcastOverlay
import moe.caa.fabric.hadesgame.util.getActivePlayers
import moe.caa.fabric.hadesgame.util.sendOverlay
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LightningBolt
import java.awt.Color
import kotlin.random.Random

data object ThunderCall : AbstractGameEvent() {
    override val eventName = "雷暴点名"

    private var activeJob: Job? = null

    override suspend fun callEvent() {
        activeJob?.cancel()

        activeJob = GameCore.coroutineScope.launch {
            repeat(7) {
                delay(4_000L)

                val target = getActivePlayers().randomOrNull() ?: return@launch
                val world = target.level() as ServerLevel
                val strikeX = target.x + Random.nextDouble(-3.0, 3.0)
                val strikeZ = target.z + Random.nextDouble(-3.0, 3.0)
                val strikeY = target.y

                LightningBolt(EntityType.LIGHTNING_BOLT, world).apply {
                    setVisualOnly(true)
                    snapTo(strikeX, strikeY, strikeZ)
                    world.addFreshEntity(this)
                }

                Component.literal("雷暴点名: ").withColor(Color.YELLOW.rgb)
                    .append(target.name.copy().withColor(Color.WHITE.rgb))
                    .broadcastOverlay()
                Component.literal("你被雷暴盯上了, 快闪开!").withColor(Color.RED.rgb).sendOverlay(target)
                SoundEvents.LIGHTNING_BOLT_THUNDER.broadcast(1.0F, 1.0F)
            }

            Component.literal("雷暴点名已结束").withColor(Color.RED.rgb).broadcastOverlay()
            SoundEvents.VILLAGER_NO.broadcast(1.0F, 1.0F)
        }

        Component.literal("雷暴点名已开始").withColor(Color.GREEN.rgb).broadcast()
        SoundEvents.BEACON_ACTIVATE.broadcast(1.0F, 1.0F)
    }

    override suspend fun endEvent() {
        activeJob?.cancel()
    }
}
