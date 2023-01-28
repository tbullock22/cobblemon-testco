package com.cobblemon.mod.common.pokemon.evolution

import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage
import com.cobblemon.mod.common.pokemon.evolution.progress.RecoilEvolutionProgress
import org.junit.jupiter.api.Test
import kotlin.math.roundToInt

internal class RecoilProgressTest {

    @Test
    fun `test incoming message`() {
        val battleMessage = BattleMessage("|-damage|p1a: c41d9800-ed47-464f-87b3-b3dcfa781f97|75/100|[from] Recoil")
        val newPercentage = battleMessage.argumentAt(1)?.split("/")?.getOrNull(0)?.toIntOrNull() ?: 0
        val newHealth = (200 * (newPercentage / 100.0)).roundToInt()
        assert(newHealth == 150)
        val difference = 200 - newHealth
        val progress = RecoilEvolutionProgress()
        progress.updateProgress(RecoilEvolutionProgress.Progress(progress.currentProgress().recoil + difference))
        assert(progress.currentProgress().recoil == 50)
    }

}