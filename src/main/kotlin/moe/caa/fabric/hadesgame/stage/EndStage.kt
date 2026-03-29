package moe.caa.fabric.hadesgame.stage

import moe.caa.fabric.hadesgame.GameCore
import moe.caa.fabric.hadesgame.handler.ScoreboardHandler
import moe.caa.fabric.hadesgame.util.DATE_FORMAT
import moe.caa.fabric.hadesgame.util.broadcast
import moe.caa.fabric.hadesgame.util.broadcastOverlay
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.Entity
import java.awt.Color
import java.time.LocalDateTime

data object EndStage : AbstractStage() {
    override val stageName = "结束"
    override val nextStage = InitStage

    private var tick = 0
    private var countdown = 0

    override suspend fun startStage() {
        tick = 0
        countdown = 15

        runCatching {
            for (world in GameCore.server.allLevels) {
                for (entity in world.allEntities) {
                    if (entity !is ServerPlayer) {
                        entity.remove(Entity.RemovalReason.KILLED)
                    }
                }
            }
        }

        SoundEvents.GOAT_HORN_SOUND_VARIANTS[1].value().broadcast(1000F, 1F)

        val winner = GamingStage.winner
        if (winner == null) {
            Component.literal("游戏结束, 这局没有人获胜").withColor(Color.RED.rgb).broadcast()
        } else {
            Component.literal("游戏结束, 最后的赢家是: ").withColor(Color.RED.rgb)
                .append(Component.literal(winner.string).withColor(Color.WHITE.rgb)).broadcast()
        }
    }

    override suspend fun tickStage() {
        if (tick++ % 20 == 0) {
            countdown--

            ScoreboardHandler.updateContents(contents = buildList {
                add(Component.literal(DATE_FORMAT.format(LocalDateTime.now())).withColor(Color.LIGHT_GRAY.rgb))
                add(Component.literal(" "))
                add(Component.literal("   游戏结束    "))

                val winner = GamingStage.winner
                if (winner == null) {
                    add(Component.literal(" 这局没有人获胜 "))
                } else {
                    add(Component.literal(" 这局的赢家是 "))
                    add(Component.literal("  " + winner.string + " ").withColor(Color.RED.rgb))
                }

                add(Component.literal(" "))
                add(Component.literal("(╯°□°)╯").withColor(Color.YELLOW.rgb))
            })

            Component.literal("将在 ").withColor(Color.LIGHT_GRAY.rgb)
                .append(Component.literal(countdown.toString()).withColor(Color.RED.rgb))
                .append(Component.literal(" 秒后随机下一轮游戏!").withColor(Color.LIGHT_GRAY.rgb)).broadcastOverlay()
        }
    }

    override suspend fun shouldEndStage(): Boolean {
        return countdown <= 0
    }
}
