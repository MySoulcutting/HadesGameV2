package moe.caa.fabric.hadesgame.handler

import moe.caa.fabric.hadesgame.GameCore
import moe.caa.fabric.hadesgame.stage.EndStage
import moe.caa.fabric.hadesgame.stage.GamingStage
import moe.caa.fabric.hadesgame.stage.InitStage
import moe.caa.fabric.hadesgame.stage.WaitReadyStage
import moe.caa.fabric.hadesgame.stage.WaitReadyStage.preparedPlayers
import moe.caa.fabric.hadesgame.util.broadcast
import moe.caa.fabric.hadesgame.util.resetState
import moe.caa.fabric.hadesgame.util.teleport
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.GameType
import java.awt.Color

object JoinLeaveHandler {
    fun setup() {
        ServerPlayerEvents.JOIN.register {
            preparedPlayers.remove(it.uuid)

            var player = it
            if (!player.isAlive) {
                player = GameCore.server.playerList.respawn(
                    player,
                    true,
                    Entity.RemovalReason.CHANGED_DIMENSION
                )
            }

            player.teleport(InitStage.lobbySpawnLoc)
            player.resetState()

            when (GameCore.currentStage) {
                EndStage, GamingStage -> {
                    player.setGameMode(GameType.SPECTATOR)
                }

                InitStage, WaitReadyStage -> {
                    player.setGameMode(GameType.ADVENTURE)
                }
            }
        }

        ServerPlayerEvents.LEAVE.register {
            when (GameCore.currentStage) {
                GamingStage -> {
                    if (it.gameMode() != GameType.SPECTATOR) {
                        it.kill(it.level())
                        Component.literal("玩家 ").withColor(Color.LIGHT_GRAY.rgb)
                            .append(it.name.copy().withColor(Color.WHITE.rgb))
                            .append(Component.literal(" 畏战自鲨了...").withColor(Color.LIGHT_GRAY.rgb))
                            .broadcast()
                    }
                }

                else -> {}
            }
        }
    }
}
