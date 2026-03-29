package moe.caa.fabric.hadesgame.util

import net.minecraft.server.network.ServerPlayerEntity

fun <DATA> eventSwap(
    attributeDataGetter: (ServerPlayerEntity) -> DATA,
    attributeSetter: (ServerPlayerEntity, ServerPlayerEntity, DATA) -> Unit,
) {
    val targets = getActivePlayers()
        .toMutableList()
        .apply { shuffle() }

    if (targets.isEmpty()) return

    val sources = targets.map { it to attributeDataGetter.invoke(it) }.toMutableList().apply {
        add(removeFirst())
    }

    targets.forEach {
        val (player, data) = sources.removeFirst()
        attributeSetter.invoke(it, player, data)
    }
}
