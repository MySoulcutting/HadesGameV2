package moe.caa.fabric.hadesgame.stage

import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import moe.caa.fabric.hadesgame.GameCore
import moe.caa.fabric.hadesgame.event.OnHello
import moe.caa.fabric.hadesgame.event.networkHelloEvent
import moe.caa.fabric.hadesgame.util.Location
import moe.caa.fabric.hadesgame.util.broadcastOverlay
import moe.caa.fabric.hadesgame.util.randomLobbySpawnLocation
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import java.awt.Color
import kotlin.properties.Delegates

// 地图初始化阶段
data object InitStage : AbstractStage() {
    var lobbySpawnLoc by Delegates.notNull<Location>()

    override val stageName = "地图初始化"
    override val nextStage = WaitReadyStage
    override suspend fun shouldEndStage() = true

    override fun initStage() {
        networkHelloEvent.register {
            if (runCatching { lobbySpawnLoc }.getOrNull() == null) {
                return@register OnHello.Result.KICK(
                    Component.literal("请稍后再试, 游戏尚未初始化完成!").withColor(Color.RED.rgb)
                )
            }
            return@register OnHello.Result.ALLOWED
        }
    }

    override suspend fun endStage() {
        val border = GameCore.server.overworld().worldBorder
        border.setCenter(lobbySpawnLoc.x, lobbySpawnLoc.z)
        border.size = 500.0
    }

    override suspend fun tickStage() {
        GameCore.logger.info("开始随机地图位置...")
        val tipJob = GameCore.coroutineScope.launch {
            var second = 0
            while (isActive) {
                second++
                var tip = Component.literal("随机地图中").withColor(Color.LIGHT_GRAY.rgb)
                repeat(second % 5 + 1) {
                    tip = tip.append(Component.literal(".").withColor(Color.LIGHT_GRAY.rgb))
                }
                tip.broadcastOverlay()
                delay(500)
            }
        }

        val startTimeMills = System.currentTimeMillis()
        lobbySpawnLoc = GameCore.server.overworld().randomLobbySpawnLocation()
        tipJob.cancel()

        val endTimeMills = System.currentTimeMillis()
        val elapsedTime = endTimeMills - startTimeMills

        GameCore.logger.info("地图随机完成, 随机到位置: x = ${lobbySpawnLoc.x}, z = ${lobbySpawnLoc.z}, 耗时: $elapsedTime ms")
        Component.literal("随机成功").withColor(Color.GREEN.rgb).append(
            Component.literal(" (耗时: $elapsedTime ms)").withColor(Color.LIGHT_GRAY.rgb)
        ).broadcastOverlay()

        placeLobbyBlock(Blocks.BARRIER.defaultBlockState())
    }

    fun placeLobbyBlock(state: BlockState) {
        for (x in -10 + lobbySpawnLoc.x.toInt()..10 + lobbySpawnLoc.x.toInt()) {
            for (z in -10 + lobbySpawnLoc.z.toInt()..10 + lobbySpawnLoc.z.toInt()) {
                lobbySpawnLoc.world.setBlock(BlockPos(x, lobbySpawnLoc.y.toInt() - 2, z), state, 3)
            }
        }

        for (xz in -10..10) {
            for (y in lobbySpawnLoc.y.toInt() - 2..lobbySpawnLoc.y.toInt() + 7) {
                lobbySpawnLoc.world.setBlock(
                    BlockPos((lobbySpawnLoc.x + xz).toInt(), y, (lobbySpawnLoc.z + 10).toInt()),
                    state,
                    3
                )
                lobbySpawnLoc.world.setBlock(
                    BlockPos((lobbySpawnLoc.x + xz).toInt(), y, (lobbySpawnLoc.z - 10).toInt()),
                    state,
                    3
                )
                lobbySpawnLoc.world.setBlock(
                    BlockPos((lobbySpawnLoc.x + 10).toInt(), y, (lobbySpawnLoc.z + xz).toInt()),
                    state,
                    3
                )
                lobbySpawnLoc.world.setBlock(
                    BlockPos((lobbySpawnLoc.x - 10).toInt(), y, (lobbySpawnLoc.z + xz).toInt()),
                    state,
                    3
                )
            }
        }
    }
}
