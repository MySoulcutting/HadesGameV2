package moe.caa.fabric.hadesgame.handler

import moe.caa.fabric.hadesgame.GameCore
import net.minecraft.scoreboard.ScoreHolder
import net.minecraft.scoreboard.ScoreboardCriterion
import net.minecraft.scoreboard.ScoreboardDisplaySlot
import net.minecraft.scoreboard.ScoreboardObjective
import net.minecraft.scoreboard.ServerScoreboard
import net.minecraft.scoreboard.number.BlankNumberFormat
import net.minecraft.text.Style
import net.minecraft.text.Text
import java.awt.Color

object ScoreboardHandler {
    private const val SCORE_HOLDER_PREFIX = "hades_game_line_"

    private lateinit var serverScoreboard: ServerScoreboard
    private lateinit var scoreboardObjective: ScoreboardObjective

    private val scoreHolder = HashMap<String, ScoreHolder>()

    private val scoreboardTitle by lazy {
        Text.empty().append(
            Text.literal("阴间游戏")
                .setStyle(Style.EMPTY.withBold(true).withColor(Color.YELLOW.rgb))
        )
            .append(Text.literal("v3").withColor(Color.LIGHT_GRAY.rgb))
    }

    fun setup() {
        serverScoreboard = GameCore.server.scoreboard
        serverScoreboard.getNullableObjective("hades_game_scoreboard")?.also { board ->
            serverScoreboard.removeObjective(board)
        }
        scoreHolder.clear()

        scoreboardObjective = serverScoreboard.addObjective(
            "hades_game_scoreboard",
            ScoreboardCriterion.DUMMY,
            Text.empty(),
            ScoreboardCriterion.RenderType.INTEGER,
            true,
            null
        )

        serverScoreboard.setObjectiveSlot(ScoreboardDisplaySlot.SIDEBAR, scoreboardObjective)
    }

    fun updateContents(title: Text = scoreboardTitle, contents: List<Text>) {
        val reversedContents = contents.reversed()

        scoreboardObjective.displayName = title

        for ((index, content) in reversedContents.withIndex()) {
            val holderName = "$SCORE_HOLDER_PREFIX$index"
            val scoreAccess = serverScoreboard.getOrCreateScore(scoreHolder.getOrPut(holderName) {
                ScoreHolder { holderName }
            }, scoreboardObjective, true)

            scoreAccess.score = index
            scoreAccess.displayText = content
            scoreAccess.setNumberFormat(BlankNumberFormat.INSTANCE)
        }

        val activeHolderNames = reversedContents.indices.mapTo(HashSet()) { index ->
            "$SCORE_HOLDER_PREFIX$index"
        }

        val shouldRemoveHolderNames = scoreHolder.keys.filter { it !in activeHolderNames }
        shouldRemoveHolderNames.forEach { holderName ->
            scoreHolder.remove(holderName)?.also { holder ->
                serverScoreboard.removeScore(holder, scoreboardObjective)
            }
        }
    }
}
