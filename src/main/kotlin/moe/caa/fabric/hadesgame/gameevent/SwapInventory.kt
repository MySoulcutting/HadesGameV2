package moe.caa.fabric.hadesgame.gameevent

import moe.caa.fabric.hadesgame.util.broadcast
import moe.caa.fabric.hadesgame.util.eventSwap
import moe.caa.fabric.hadesgame.util.sendOverlay
import moe.caa.fabric.hadesgame.util.syncInventory
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.item.ItemStack
import java.awt.Color

data object SwapInventory : AbstractGameEvent() {
    override val eventName = "交换背包"

    override suspend fun callEvent() {
        eventSwap({ it.getInventoryData() }) { self, source, data ->
            data.applyTo(self)
            Component.literal("你已应用 ").withColor(Color.LIGHT_GRAY.rgb)
                .append(Component.literal(source.name.string).withColor(Color.WHITE.rgb)).append(" 的背包")
                .sendOverlay(self)
        }
        SoundEvents.FOX_TELEPORT.broadcast(100F, 0F)
    }

    data class InventoryData(
        val itemStacks: List<ItemStack>
    )

    private fun ServerPlayer.getInventoryData(): InventoryData {
        val itemStacks = ArrayList<ItemStack>(inventory.containerSize)
        for (i in 0 until inventory.containerSize) {
            itemStacks.add(inventory.getItem(i).copy())
        }

        return InventoryData(itemStacks)
    }

    private fun InventoryData.applyTo(player: ServerPlayer) {
        for (i in itemStacks.indices) {
            player.inventory.setItem(i, itemStacks[i])
        }
        player.syncInventory()
    }
}
