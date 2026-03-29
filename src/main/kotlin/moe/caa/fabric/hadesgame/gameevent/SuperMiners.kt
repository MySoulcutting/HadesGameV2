package moe.caa.fabric.hadesgame.gameevent

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.caa.fabric.hadesgame.GameCore
import moe.caa.fabric.hadesgame.util.broadcast
import moe.caa.fabric.hadesgame.util.broadcastOverlay
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import java.awt.Color


data object SuperMiners : AbstractGameEvent() {
    override val eventName = "超级矿工"

    private var activeJob: Job? = null
    private val breakingPositions = mutableSetOf<Long>()

    override fun initEvent() {
        PlayerBlockBreakEvents.AFTER.register { world, player, pos, _, _ ->
            if (activeJob?.isActive != true) return@register

            val rootPosKey = pos.asLong()
            if (!breakingPositions.add(rootPosKey)) return@register

            val pendingKeys = mutableListOf<Long>()
            try {
                val radius = 2
                for (y in pos.y - radius..pos.y + radius) {
                    for (x in pos.x - radius..pos.x + radius) {
                        for (z in pos.z - radius..pos.z + radius) {
                            if (x == pos.x && y == pos.y && z == pos.z) continue

                            val targetPos = BlockPos(x, y, z)
                            val targetKey = targetPos.asLong()
                            if (breakingPositions.add(targetKey)) {
                                pendingKeys += targetKey
                            }
                            world.breakBlock(targetPos, true, player)
                        }
                    }
                }
            } finally {
                breakingPositions.remove(rootPosKey)
                pendingKeys.forEach(breakingPositions::remove)
            }
        }
    }

    override suspend fun callEvent() {
        activeJob?.cancel()

        activeJob = GameCore.coroutineScope.launch {
            delay(30_000L)
            Text.literal("超级矿工已失效").withColor(Color.RED.rgb).broadcastOverlay()
            SoundEvents.ENTITY_VILLAGER_NO.broadcast(1.0F, 1.0F)
        }
        Text.literal("超级矿工已生效").withColor(Color.GREEN.rgb).broadcastOverlay()
        SoundEvents.ENTITY_VILLAGER_YES.broadcast(1.0F, 1.0F)
    }

    override suspend fun endEvent() {
        breakingPositions.clear()
        activeJob?.cancel()
    }
}
