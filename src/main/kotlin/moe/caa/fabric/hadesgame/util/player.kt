package moe.caa.fabric.hadesgame.util

import moe.caa.fabric.hadesgame.GameCore
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.world.GameMode

fun getPlayers(): List<ServerPlayerEntity> = GameCore.server.playerManager.playerList

fun getActivePlayers(): List<ServerPlayerEntity> = getPlayers().filter {
    it.interactionManager.gameMode != GameMode.SPECTATOR
}

fun ServerPlayerEntity.resetState() {
    dismountVehicle()
    removeAllPassengers()
    setVelocity(0.0, 0.0, 0.0)
    inventory.clear()
    inventory.selectedSlot = 0
    enderChestInventory.clear()

    experienceLevel = 0
    experienceProgress = 0.toFloat()
    totalExperience = 0

    heal()
    sendAbilitiesUpdate()
}

fun ServerPlayerEntity.heal() {
    hungerManager.foodLevel = 20
    hungerManager.saturationLevel = 5.0f
    health = maxHealth
    kotlin.runCatching {
        for (entry in statusEffects.map { it.effectType }) {
            removeStatusEffect(entry)
        }
    }
}

fun ServerPlayerEntity.syncInventory() {
    inventory.updateItems()
    currentScreenHandler.sendContentUpdates()
}

fun ServerPlayerEntity.copyHotbar(): List<ItemStack> {
    return List(PlayerInventory.getHotbarSize()) { slot ->
        inventory.getStack(slot).copy()
    }
}

fun ServerPlayerEntity.setHotbar(stacks: List<ItemStack>) {
    repeat(PlayerInventory.getHotbarSize()) { slot ->
        inventory.setStack(slot, stacks.getOrNull(slot)?.copy() ?: ItemStack.EMPTY)
    }
    syncInventory()
}

private val armorSlotIndices = listOf(36, 37, 38, 39)

fun ServerPlayerEntity.copyArmorContents(): List<ItemStack> {
    return armorSlotIndices.map { slot ->
        inventory.getStack(slot).copy()
    }
}

fun ServerPlayerEntity.setArmorContents(stacks: List<ItemStack>) {
    armorSlotIndices.forEachIndexed { index, slot ->
        inventory.setStack(slot, stacks.getOrNull(index)?.copy() ?: ItemStack.EMPTY)
    }
    syncInventory()
}
