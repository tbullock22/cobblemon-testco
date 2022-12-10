/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokeball.catching.modifiers

import com.cobblemon.mod.common.api.pokeball.catching.CatchRateModifier
import com.cobblemon.mod.common.api.pokemon.labels.CobblemonPokemonLabels
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.entity.LivingEntity

/**
 * A [CatchRateModifier] based on the presence of [Pokemon] literal labels.
 * This is checked against [Pokemon.hasLabels].
 * See [CobblemonPokemonLabels] for some default labels.
 *
 * @property multiplier The multiplier if the label is present.
 * @property matching Will this multiplier should be applied if the labels match?
 * @property labels The literal labels being queried.
 */
class LabelModifier(
    val multiplier: Float,
    val matching: Boolean,
    vararg val labels: String
) : CatchRateModifier {

    override fun modifyCatchRate(currentCatchRate: Float, thrower: LivingEntity, pokemon: Pokemon, host: Pokemon?) = if (pokemon.hasLabels(*this.labels)) currentCatchRate * this.resolveMatchRate() else currentCatchRate * this.resolveNoMatchRate()

    private fun resolveMatchRate() = if (this.matching) this.multiplier else 1F

    private fun resolveNoMatchRate() = if (this.matching) 1F else this.multiplier

}