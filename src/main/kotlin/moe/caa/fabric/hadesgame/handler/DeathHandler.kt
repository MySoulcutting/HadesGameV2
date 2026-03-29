package moe.caa.fabric.hadesgame.handler

import moe.caa.fabric.hadesgame.GameCore
import moe.caa.fabric.hadesgame.event.preDeathEvent
import moe.caa.fabric.hadesgame.mixin.LivingEntityAccessor
import moe.caa.fabric.hadesgame.stage.EndStage
import moe.caa.fabric.hadesgame.stage.GamingStage
import moe.caa.fabric.hadesgame.stage.InitStage
import moe.caa.fabric.hadesgame.stage.WaitReadyStage
import moe.caa.fabric.hadesgame.util.broadcast
import moe.caa.fabric.hadesgame.util.resetState
import moe.caa.fabric.hadesgame.util.teleport
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.GameType

object DeathHandler {
    fun setup() {
        ServerLivingEntityEvents.ALLOW_DEATH.register { livingEntity, damageSource, _ ->
            if (livingEntity !is ServerPlayer) return@register true
            livingEntity as LivingEntityAccessor

            livingEntity.resetState()
            when (GameCore.currentStage) {
                EndStage, GamingStage -> {
                    livingEntity.setGameMode(GameType.SPECTATOR)
                    livingEntity.invokeDrop(livingEntity.level(), damageSource)
                }

                InitStage, WaitReadyStage -> {
                    livingEntity.setGameMode(GameType.ADVENTURE)
                    livingEntity.teleport(InitStage.lobbySpawnLoc)
                }
            }

            livingEntity.combatTracker.deathMessage.broadcast()

            return@register false
        }

        preDeathEvent.register { livingEntity: LivingEntity, _ ->
            if (livingEntity !is ServerPlayer) return@register true
            livingEntity.resetState()

            return@register false
        }
    }
}
