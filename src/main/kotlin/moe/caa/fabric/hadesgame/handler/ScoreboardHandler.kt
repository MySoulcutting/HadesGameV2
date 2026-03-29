package moe.caa.fabric.hadesgame.handler

import moe.caa.fabric.hadesgame.GameCore
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.numbers.BlankFormat
import net.minecraft.server.ServerScoreboard
import net.minecraft.world.scores.DisplaySlot
import net.minecraft.world.scores.Objective
import net.minecraft.world.scores.ScoreHolder
import net.minecraft.world.scores.criteria.ObjectiveCriteria
import java.awt.Color

object ScoreboardHandler {
    private const val SCORE_HOLDER_PREFIX = "hades_game_line_"

    private lateinit var serverScoreboard: ServerScoreboard
    private lateinit var scoreboardObjective: Objective

    private val scoreHolder = HashMap<String, ScoreHolder>()

    private val scoreboardTitle by lazy {
        Component.empty().append(
            Component.literal("阴间游戏")
                .setStyle(Style.EMPTY.withBold(true).withColor(Color.YELLOW.rgb))
        ).append(Component.literal("v3").withColor(Color.LIGHT_GRAY.rgb))
    }

    fun setup() {
        serverScoreboard = GameCore.server.scoreboard
        serverScoreboard.getObjective("hades_game_scoreboard")?.also { board ->
            serverScoreboard.removeObjective(board)
        }
        scoreHolder.clear()

        scoreboardObjective = serverScoreboard.addObjective(
            "hades_game_scoreboard",
            ObjectiveCriteria.DUMMY,
            Component.empty(),
            ObjectiveCriteria.RenderType.INTEGER,
            true,
            null
        )

        serverScoreboard.setDisplayObjective(DisplaySlot.SIDEBAR, scoreboardObjective)
    }

    fun updateContents(title: Component = scoreboardTitle, contents: List<Component>) {
        val reversedContents = contents.reversed()

        scoreboardObjective.displayName = title

        for ((index, content) in reversedContents.withIndex()) {
            val holderName = "$SCORE_HOLDER_PREFIX$index"
            val scoreAccess = serverScoreboard.getOrCreatePlayerScore(
                scoreHolder.getOrPut(holderName) { ScoreHolder.forNameOnly(holderName) },
                scoreboardObjective,
                true
            )

            scoreAccess.set(index)
            scoreAccess.display(content)
            scoreAccess.numberFormatOverride(BlankFormat.INSTANCE)
        }

        val activeHolderNames = reversedContents.indices.mapTo(HashSet()) { index ->
            "$SCORE_HOLDER_PREFIX$index"
        }

        val shouldRemoveHolderNames = scoreHolder.keys.filter { it !in activeHolderNames }
        shouldRemoveHolderNames.forEach { holderName ->
            scoreHolder.remove(holderName)?.also { holder ->
                serverScoreboard.resetSinglePlayerScore(holder, scoreboardObjective)
            }
        }
    }
}
