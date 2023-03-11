/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.pokemon.stats.Stat
import kotlin.math.min

class EVs : PokemonStats() {
    override val acceptableRange = 0..MAX_STAT_VALUE
    override val defaultValue = 0

    override fun canSet(stat: Stat, value: Int): Boolean {
        if (!super.canSet(stat, value)) {
            return false
        }
        val simulated = this.associate { it.key to it.value }.toMutableMap()
        simulated[stat] = value
        val simulatedTotal = simulated.values.sum()
        return simulatedTotal <= MAX_TOTAL_VALUE
    }

    /**
     * Safely adds the given amount of EVs to this store.
     *
     * @param key The [Stat] being mutated.
     * @param value The amount to attempt to add, this wil safely be coerced to the highest possible value.
     * @return The amount added or 0 if the addition was impossible.
     */
    fun add(key: Stat, value: Int): Int {
        val currentTotal = this.sumOf { it.value }
        if (currentTotal == MAX_TOTAL_VALUE) {
            return 0
        }
        val currentStat = this.getOrDefault(key)
        val possibleForStat = MAX_STAT_VALUE - currentStat
        val possibleForTotal = MAX_TOTAL_VALUE - currentTotal
        val coercedValue = value.coerceAtMost(min(possibleForStat, possibleForTotal))
        val newValue = currentStat + coercedValue
        // avoid unnecessary updates
        if (newValue != currentStat) {
            this[key] = newValue
            return coercedValue
        }
        return 0
    }

    companion object {
        const val MAX_STAT_VALUE = 252
        const val MAX_TOTAL_VALUE = 510

        fun createEmpty() : EVs = Cobblemon.statProvider.createEmptyEVs()
    }
}