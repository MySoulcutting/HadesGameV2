package moe.caa.fabric.hadesgame.stage

import kotlinx.coroutines.delay
import moe.caa.fabric.hadesgame.GameCore
import moe.caa.fabric.hadesgame.event.sneakStateChangeEvent
import moe.caa.fabric.hadesgame.handler.ScoreboardHandler
import moe.caa.fabric.hadesgame.util.*
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.GameType
import net.minecraft.world.level.block.Blocks
import java.awt.Color
import java.time.LocalDateTime
import java.util.UUID
import java.util.WeakHashMap

data object WaitReadyStage : AbstractStage() {
    override val stageName = "等待开始"
    override val nextStage = GamingStage

    private var tick = 0
    private var tipNoPreparedIndex = 0

    private var scheduleStart = false
    private var scheduleStartCountdown = 5

    private var shouldEndStage = false

    private val tipState: Component = Component.empty()
        .append(Component.literal("双击 ").withColor(Color.LIGHT_GRAY.rgb))
        .append(Component.keybind("key.sneak").withColor(Color.WHITE.rgb))
        .append(Component.literal(" 键可切换等待状态, 当前状态: ").withColor(Color.LIGHT_GRAY.rgb))

    private val tipGlobalState: Component = Component.empty()
        .append(Component.literal("当所有人状态都切换为 ").withColor(Color.LIGHT_GRAY.rgb))
        .append(Component.literal("已准备").withColor(Color.GREEN.rgb))
        .append(Component.literal(" 状态后, 游戏将会自动开始.").withColor(Color.LIGHT_GRAY.rgb))

    private val tipNotPreparedPlayers: Component = Component.empty()
        .append(Component.literal("当前未准备的玩家").withColor(Color.LIGHT_GRAY.rgb))

    private val tipInsufficient: Component = Component.empty()
        .append(Component.literal("游戏人数不足 ").withColor(Color.RED.rgb))
        .append(Component.literal("2").withColor(Color.WHITE.rgb))
        .append(Component.literal(" 人, 无法开始游戏.").withColor(Color.RED.rgb))

    val preparedPlayers = mutableSetOf<UUID>()
    private val changeStateCacheMap = WeakHashMap<UUID, Long>()

    override fun initStage() {
        sneakStateChangeEvent.register { player, newSneakingState ->
            if (isCurrentRunStage() && newSneakingState) {
                val currentTimeMillis = System.currentTimeMillis()
                val lastClick = changeStateCacheMap[player.uuid]
                if ((lastClick ?: 0) + 500 > currentTimeMillis) {
                    changeStateCacheMap.remove(player.uuid)
                    SoundEvents.UI_BUTTON_CLICK.value().playSound(player, 2F, 1000F)

                    if (player.uuid in preparedPlayers) {
                        preparedPlayers.remove(player.uuid)
                        player.sendSystemMessage(
                            Component.literal("准备状态切换为: ").withColor(Color.LIGHT_GRAY.rgb)
                                .append(Component.literal("未准备").withColor(Color.RED.rgb)),
                            false
                        )

                        if (scheduleStart) {
                            scheduleStart = false
                            player.name.copy().append(
                                Component.literal("取消了准备状态, 已终止倒计时开始游戏!").withColor(Color.RED.rgb)
                            ).broadcast()

                            SoundEvents.UI_BUTTON_CLICK.value().playSound(player, 2F, 1000F)
                        }
                    } else {
                        preparedPlayers.add(player.uuid)
                        player.sendSystemMessage(
                            Component.literal("准备状态切换为: ").withColor(Color.LIGHT_GRAY.rgb)
                                .append(Component.literal("已准备").withColor(Color.GREEN.rgb)),
                            false
                        )
                    }
                } else {
                    changeStateCacheMap[player.uuid] = currentTimeMillis
                }
            }
        }
    }

    override suspend fun endStage() {
        changeStateCacheMap.clear()
        preparedPlayers.clear()

        ScoreboardHandler.updateContents(contents = buildList {
            add(Component.literal(DATE_FORMAT.format(LocalDateTime.now())).withColor(Color.LIGHT_GRAY.rgb))
            add(Component.literal(" "))
            add(Component.literal(" 笼子即将打开 "))
            add(Component.literal(" "))
            add(Component.literal("(╯°□°)╯").withColor(Color.YELLOW.rgb))
        })

        repeat(24) {
            SoundEvents.NOTE_BLOCK_BIT.value().broadcast(1000F, 2F)
            Component.literal("笼子即将打开").withColor(Color.LIGHT_GRAY.rgb).broadcastOverlay()
            delay(100)
        }
        delay(100)
        SoundEvents.GENERIC_EXPLODE.value().broadcast(1000F, 0F)
        for (viewer in getPlayers()) {
            for (pos in getPlayers().map { it.position() }) {
                viewer.level().sendParticles(
                    viewer,
                    ParticleTypes.EXPLOSION_EMITTER,
                    false,
                    false,
                    pos.x,
                    pos.y,
                    pos.z,
                    1,
                    0.0,
                    0.0,
                    0.0,
                    0.0
                )
            }
        }

        InitStage.placeLobbyBlock(Blocks.AIR.defaultBlockState())
    }

    override suspend fun startStage() {
        shouldEndStage = false
        scheduleStart = false
        preparedPlayers.clear()
        changeStateCacheMap.clear()
        tick = 0
        GameCore.server.playerList.players.forEach { player ->
            player.teleport(InitStage.lobbySpawnLoc)
            player.setGameMode(GameType.ADVENTURE)
            player.resetState()
        }
    }

    override suspend fun tickStage() {
        tick++

        val players = getPlayers()
        if (players.isEmpty()) return

        if (tick % 20 == 0) {
            for (p0 in players) {
                var player = p0
                if (!player.isAlive) {
                    player = GameCore.server.playerList.respawn(
                        player,
                        true,
                        Entity.RemovalReason.CHANGED_DIMENSION
                    )
                    player.teleport(InitStage.lobbySpawnLoc)
                }

                player.heal()
            }

            GameCore.server.overworld().resetWeatherCycle()
        }

        if (players.size < 2) {
            if (scheduleStart) {
                scheduleStart = false
            }

            if (tick % 10 == 0) {
                ScoreboardHandler.updateContents(contents = buildList {
                    add(Component.literal(DATE_FORMAT.format(LocalDateTime.now())).withColor(Color.LIGHT_GRAY.rgb))
                    add(Component.literal("                    "))
                    add(Component.literal(" 至少需要").append(Component.literal("2").withColor(Color.RED.rgb)).append("名玩家"))
                    add(Component.literal(" 才能进行游戏, 请"))
                    add(Component.literal(" 等待或邀请更多的"))
                    add(Component.literal(" 玩家加入游戏!"))
                    add(Component.literal(" "))
                    add(Component.literal("(╯°□°)╯").withColor(Color.YELLOW.rgb))
                })
            }
            return
        }

        val noPreparedPlayers = players.filter { it.uuid !in preparedPlayers }
        if (noPreparedPlayers.isNotEmpty()) {
            if (tick % 10 == 0) {
                ScoreboardHandler.updateContents(contents = buildList {
                    add(Component.literal(DATE_FORMAT.format(LocalDateTime.now())).withColor(Color.LIGHT_GRAY.rgb))
                    add(Component.literal("                    "))
                    add(Component.literal(" 当所有人状态都"))
                    add(Component.literal(" 切换为").append(Component.literal("已准备").withColor(Color.GREEN.rgb)).append("状"))
                    add(Component.literal(" 态后, 游戏将会"))
                    add(Component.literal(" 自动开始."))
                    add(Component.literal(" "))
                    add(
                        Component.literal("未准备: ").withColor(Color.WHITE.rgb)
                            .append(Component.literal(noPreparedPlayers.size.toString()).withColor(Color.RED.rgb))
                    )
                    add(
                        Component.literal("已准备: ").withColor(Color.WHITE.rgb)
                            .append(
                                Component.literal(players.count { it.uuid in preparedPlayers }.toString())
                                    .withColor(Color.YELLOW.rgb)
                            )
                    )
                    add(Component.literal(" "))
                    add(Component.literal("(╯°□°)╯").withColor(Color.YELLOW.rgb))
                })

                if (tick % 20 == 0) {
                    if (players.size < 2) {
                        tipInsufficient.broadcastOverlay()
                    } else when (tick / 100 % 3) {
                        0 -> {
                            for (player in players) {
                                tipState.copy()
                                    .append(
                                        if (player.uuid in preparedPlayers)
                                            Component.literal("已准备").withColor(Color.GREEN.rgb)
                                        else
                                            Component.literal("未准备").withColor(Color.RED.rgb)
                                    )
                                    .sendOverlay(player)
                            }
                        }

                        1 -> tipGlobalState.broadcastOverlay()
                        2 -> {
                            tipNotPreparedPlayers.copy()
                                .append(Component.literal("(").withColor(Color.LIGHT_GRAY.rgb))
                                .append(Component.literal(noPreparedPlayers.size.toString()).withColor(Color.RED.rgb))
                                .append(Component.literal("): ").withColor(Color.LIGHT_GRAY.rgb))
                                .append(noPreparedPlayers[tipNoPreparedIndex++ % noPreparedPlayers.size].name)
                                .broadcastOverlay()
                        }
                    }
                }
            }
        } else {
            if (!scheduleStart) {
                scheduleStart = true
                scheduleStartCountdown = 5

                SoundEvents.EXPERIENCE_ORB_PICKUP.broadcast(1000F, 2F)
                Component.literal("所有玩家已准备就绪, 游戏即将开始!").withColor(Color.GREEN.rgb).broadcast()
            }
            if (tick % 20 == 0) {
                scheduleStartCountdown--
                if (scheduleStartCountdown <= 0) {
                    shouldEndStage = true
                }
            }

            if (tick % 10 == 0) {
                ScoreboardHandler.updateContents(contents = buildList {
                    add(Component.literal(DATE_FORMAT.format(LocalDateTime.now())).withColor(Color.LIGHT_GRAY.rgb))
                    add(Component.literal("                        "))
                    add(Component.literal(" 游戏即将开始, 请做").withColor(Color.WHITE.rgb))
                    add(
                        Component.literal(" 好准备, 将在")
                            .append(Component.literal(scheduleStartCountdown.toString()).withColor(Color.RED.rgb))
                            .append("秒后").withColor(Color.WHITE.rgb)
                    )
                    add(Component.literal(" 传送到游戏位置!").withColor(Color.WHITE.rgb))
                    add(Component.literal(" "))
                    add(Component.literal("(╯°□°)╯").withColor(Color.YELLOW.rgb))
                })

                Component.literal("游戏将在 ").withColor(Color.LIGHT_GRAY.rgb)
                    .append(Component.literal(scheduleStartCountdown.toString()).withColor(Color.RED.rgb))
                    .append(" 秒后开始!").withColor(Color.LIGHT_GRAY.rgb)
                    .broadcastOverlay()
            }
        }
    }

    override suspend fun shouldEndStage(): Boolean {
        return shouldEndStage
    }
}
