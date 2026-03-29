package moe.caa.fabric.hadesgame.gameevent

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.caa.fabric.hadesgame.GameCore
import moe.caa.fabric.hadesgame.event.preDeathEvent
import moe.caa.fabric.hadesgame.mixin.LivingEntityAccessor
import moe.caa.fabric.hadesgame.util.broadcast
import moe.caa.fabric.hadesgame.util.broadcastOverlay
import moe.caa.fabric.hadesgame.util.getActivePlayers
import moe.caa.fabric.hadesgame.util.sendOverlay
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.level.GameType
import java.awt.Color
import java.util.UUID

data object CoupledDamage : AbstractGameEvent() {
    override val eventName = "连坐"

    private var activeJob: Job? = null
    private val partnerMap = HashMap<UUID, UUID>()
    private val relayingPlayers = HashSet<UUID>()

    override fun initEvent() {
        ServerLivingEntityEvents.AFTER_DAMAGE.register { livingEntity, _, _, damageTaken, blocked ->
            val player = livingEntity as? ServerPlayer ?: return@register
            if (blocked) return@register

            val actualDamage = (player as LivingEntityAccessor).lastDamageTaken.coerceAtLeast(damageTaken)
            relayDamage(player, actualDamage.coerceAtLeast(0.5f))
        }

        preDeathEvent.register { livingEntity, _ ->
            val player = livingEntity as? ServerPlayer ?: return@register false
            val actualDamage = (player as LivingEntityAccessor).lastDamageTaken.coerceAtLeast(1.0f)
            relayDamage(player, actualDamage)
            return@register false
        }
    }

    override suspend fun callEvent() {
        activeJob?.cancel()
        clearPairs()

        val players = getActivePlayers().shuffled()
        if (players.size < 2) return

        players.chunked(2).forEach { group ->
            val first = group.first()
            val second = group.getOrNull(1)

            if (second == null) {
                Component.literal("本轮连坐你轮空了").withColor(Color.LIGHT_GRAY.rgb).sendOverlay(first)
                return@forEach
            }

            partnerMap[first.uuid] = second.uuid
            partnerMap[second.uuid] = first.uuid

            Component.literal("本轮连坐搭档: ").withColor(Color.LIGHT_GRAY.rgb)
                .append(second.name.copy().withColor(Color.WHITE.rgb))
                .sendOverlay(first)
            Component.literal("本轮连坐搭档: ").withColor(Color.LIGHT_GRAY.rgb)
                .append(first.name.copy().withColor(Color.WHITE.rgb))
                .sendOverlay(second)
        }

        activeJob = GameCore.coroutineScope.launch {
            delay(30_000L)
            clearPairs()
            Component.literal("连坐已结束").withColor(Color.RED.rgb).broadcastOverlay()
            SoundEvents.VILLAGER_NO.broadcast(1.0F, 1.0F)
        }

        Component.literal("连坐已开始, 受伤会牵连你的搭档").withColor(Color.GREEN.rgb).broadcast()
        SoundEvents.BEACON_ACTIVATE.broadcast(1.0F, 0.6F)
    }

    override suspend fun endEvent() {
        activeJob?.cancel()
        clearPairs()
    }

    private fun relayDamage(player: ServerPlayer, amount: Float) {
        if (activeJob?.isActive != true) return
        if (player.gameMode() == GameType.SPECTATOR) return
        if (player.uuid in relayingPlayers) return
        if (amount <= 0.0f) return

        val partnerUuid = partnerMap[player.uuid] ?: return
        val partner = GameCore.server.playerList.getPlayer(partnerUuid) ?: return
        if (partner.gameMode() == GameType.SPECTATOR) return
        if (partner.uuid in relayingPlayers) return

        relayingPlayers += player.uuid
        relayingPlayers += partner.uuid
        try {
            val level = partner.level()
            partner.hurtServer(level, level.damageSources().generic(), amount)
        } finally {
            relayingPlayers.remove(player.uuid)
            relayingPlayers.remove(partner.uuid)
        }
    }

    private fun clearPairs() {
        relayingPlayers.clear()
        partnerMap.clear()
    }
}
