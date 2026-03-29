package moe.caa.fabric.hadesgame.gameevent

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.caa.fabric.hadesgame.GameCore
import moe.caa.fabric.hadesgame.event.spawnEntityEvent
import moe.caa.fabric.hadesgame.util.broadcast
import moe.caa.fabric.hadesgame.util.broadcastOverlay
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.sounds.SoundEvents
import net.minecraft.network.chat.Component
import java.awt.Color
import kotlin.jvm.optionals.getOrNull

data object RandomDrop : AbstractGameEvent() {
    override val eventName = "随机掉落"

    private var activeJob: Job? = null

    override fun initEvent() {
        spawnEntityEvent.register {
            if (activeJob?.isActive == true) {
                if (it is ItemEntity) {
                    val randomItem = BuiltInRegistries.ITEM.getRandom(it.random).getOrNull() ?: return@register
                    if (randomItem.value() != Items.AIR) {
                        it.item = ItemStack(randomItem)
                    }
                }
            }
        }
    }

    override suspend fun callEvent() {
        activeJob?.cancel()

        activeJob = GameCore.coroutineScope.launch {
            // 30 秒
            delay(1000 * 30)
            Component.literal("随机掉落效果已失效").withColor(Color.RED.rgb).broadcastOverlay()
            SoundEvents.VILLAGER_NO.broadcast(100F, 0F)
        }
        Component.literal("随机掉落效果已生效").withColor(Color.GREEN.rgb).broadcastOverlay()
        SoundEvents.ITEM_PICKUP.broadcast(100F, 0F)
    }

    override suspend fun endEvent() {
        activeJob?.cancel()
    }
}