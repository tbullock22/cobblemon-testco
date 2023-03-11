/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.events

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.advancement.CobblemonCriteria
import com.cobblemon.mod.common.advancement.criterion.CountablePokemonTypeContext
import com.cobblemon.mod.common.advancement.criterion.trigger
import com.cobblemon.mod.common.api.events.battles.BattleVictoryEvent
import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent
import com.cobblemon.mod.common.api.events.pokemon.evolution.EvolutionCompleteEvent
import com.cobblemon.mod.common.util.getPlayer
import java.util.UUID

object AdvancementHandler {

    fun onCapture(event : PokemonCapturedEvent) {
        val playerData = Cobblemon.playerData.get(event.player)
        val advancementData = playerData.advancementData
        advancementData.updateTotalCaptureCount()
        CobblemonCriteria.CATCH_POKEMON.trigger(event.player, CountablePokemonTypeContext(advancementData.totalCaptureCount, "any"))
        event.pokemon.types.forEach {
            advancementData.updateTotalTypeCaptureCount(it)
            CobblemonCriteria.CATCH_POKEMON.trigger(event.player, CountablePokemonTypeContext(advancementData.getTotalTypeCaptureCount(it), it.name))
        }
        if (event.pokemon.shiny) {
            advancementData.updateTotalShinyCaptureCount()
            CobblemonCriteria.CATCH_SHINY_POKEMON.trigger(event.player, advancementData.totalShinyCaptureCount)
        }
        Cobblemon.playerData.saveSingle(playerData)
    }

//    fun onHatch(event: HatchEggEvent) {
//        val playerData = Cobblemon.playerData.get(event.player)
//        val advancementData = playerData.advancementData
//        advancementData.updateTotalEggsHatched()
//        Cobblemon.playerData.saveSingle(playerData)
//        CobblemonCriteria.EGG_HATCH.trigger(event.player, advancementData.totalEggsHatched)
//    }

    fun onEvolve(event: EvolutionCompleteEvent) {
        val playerData = event.pokemon.getOwnerPlayer()?.let { Cobblemon.playerData.get(it) }
        if (playerData != null) {
            val advancementData = playerData.advancementData
            advancementData.updateTotalEvolvedCount()
            Cobblemon.playerData.saveSingle(playerData)
            CobblemonCriteria.EVOLVE_POKEMON.trigger(event.pokemon.getOwnerPlayer()!!, advancementData.totalEvolvedCount)
        }
    }

    fun onWinBattle(event: BattleVictoryEvent) {
        if (event.battle.isPvW) {
            return
        }

        event.winners
            .flatMap { it.getPlayerUUIDs().mapNotNull(UUID::getPlayer) }
            .forEach { player ->
                val playerData = Cobblemon.playerData.get(player)
                val advancementData = playerData.advancementData
                advancementData.updateTotalBattleVictoryCount()
                Cobblemon.playerData.saveSingle(playerData)
                CobblemonCriteria.WIN_BATTLE.trigger(player, advancementData.totalBattleVictoryCount)
            }
    }
}