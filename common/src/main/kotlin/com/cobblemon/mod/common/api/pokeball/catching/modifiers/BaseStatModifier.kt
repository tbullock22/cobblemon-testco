/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokeball.catching.modifiers

import com.cobblemon.mod.common.api.pokeball.catching.CatchRateModifier
import com.cobblemon.mod.common.api.pokemon.stats.Stat
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.entity.LivingEntity

/**
 * A [CatchRateModifier] based on the value of a [Stat].
 *
 * @property stat The [Stat] being queried.
 * @property comparator A higher order function that determines if the catch rate should be boosted.
 * @property multiplier The multiplier that will be applied if [comparator] returns true.
 * @throws IllegalArgumentException if the provided [stat] is not of type [Stat.Type.PERMANENT].
 *
 * @author Licious
 * @since May 7th, 2022
 */
class BaseStatModifier(
    val stat: Stat,
    val comparator: (value: Int) -> Boolean,
    val multiplier: Float
) : CatchRateModifier {

    init {
        if (stat.type != Stat.Type.PERMANENT) {
            throw IllegalArgumentException("${stat.identifier} is not of type PERMANENT")
        }
    }

    override fun modifyCatchRate(
        currentCatchRate: Float,
        thrower: LivingEntity,
        pokemon: Pokemon,
        host: Pokemon?
    ): Float {
        val value = pokemon.form.baseStats[this.stat] ?: return currentCatchRate
        return if (this.comparator(value)) currentCatchRate * this.multiplier else currentCatchRate
    }

}
