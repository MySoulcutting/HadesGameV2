package moe.caa.fabric.hadesgame.gameevent

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.caa.fabric.hadesgame.GameCore
import moe.caa.fabric.hadesgame.util.broadcast
import moe.caa.fabric.hadesgame.util.broadcastOverlay
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import java.awt.Color

data object ReverseDamage : AbstractGameEvent() {
    override val eventName = "反向伤害"

    private var activeJob: Job? = null
    private var handlingEntity = ArrayList<Entity>()

    override fun initEvent() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register { entity, source, amount ->
            val attacker = source.entity as? LivingEntity ?: return@register true

            if (handlingEntity.contains(entity)) return@register true
            if (handlingEntity.contains(attacker)) return@register true

            if (activeJob?.isActive == true) {
                handlingEntity.add(entity)
                handlingEntity.add(attacker)
                val level = entity.level() as net.minecraft.server.level.ServerLevel
                attacker.hurtServer(level, level.damageSources().thorns(attacker), amount)
                handlingEntity.remove(entity)
                handlingEntity.remove(attacker)
                return@register false
            }
            return@register true
        }
    }

    override suspend fun callEvent() {
        activeJob?.cancel()

        activeJob = GameCore.coroutineScope.launch {
            delay(1000 * 30)
            Component.literal("反向伤害效果已失效").withColor(Color.RED.rgb).broadcastOverlay()
            SoundEvents.EXPERIENCE_ORB_PICKUP.broadcast(1F, 1F)
        }
        Component.literal("反向伤害效果已生效").withColor(Color.GREEN.rgb).broadcastOverlay()

        SoundEvents.NOTE_BLOCK_PLING.value().broadcast(1F, 1F)
    }

    override suspend fun endEvent() {
        handlingEntity.clear()
        activeJob?.cancel()
    }
}
