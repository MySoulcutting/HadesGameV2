package moe.caa.fabric.hadesgame.util

import net.minecraft.server.level.ServerPlayer

fun <DATA> eventSwap(
    attributeDataGetter: (ServerPlayer) -> DATA,
    attributeSetter: (ServerPlayer, ServerPlayer, DATA) -> Unit,
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
