/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.stats

import com.cobblemon.mod.common.battles.pokemon.BattlePokemon

/**
 * Responsible for resolving EV yield after each battle.
 * For default implementation see [Generation8EvCalculator].
 *
 * @author Licious
 * @since October 31st, 2022
 */
interface EvCalculator {

    fun calculate(battlePokemon: BattlePokemon): Map<Stat, Int> {
        val total = hashMapOf<Stat, Int>()
        battlePokemon.facedOpponents
            .filter { it.health == 0 }
            .forEach { opponent ->
                val results = this.calculate(battlePokemon, opponent)
                results.forEach { (stat, value) ->
                    var newValue = total[stat] ?: 0
                    newValue += value
                    total[stat] = newValue
                }
            }
        return total
    }

    fun calculate(battlePokemon: BattlePokemon, opponentPokemon: BattlePokemon): Map<Stat, Int>

}

object Generation8EvCalculator : EvCalculator {

    override fun calculate(battlePokemon: BattlePokemon, opponentPokemon: BattlePokemon): Map<Stat, Int> {
        // ToDo Once held items and EV related items are implemented update this
        return opponentPokemon.originalPokemon.form.evYield
    }

}