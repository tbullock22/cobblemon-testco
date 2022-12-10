/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokeball.catching.modifiers

import com.cobblemon.mod.common.api.pokeball.catching.CatchRateModifier
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.entity.LivingEntity

/**
 * A [CatchRateModifier] that resolves the catch rate based on the Entity attached to the target Pokémon.
 *
 * @property calculator Responsible for resolving the catch rate dynamically based on the given params.
 *
 * @author Licious
 * @since May 7th, 2022
 */
open class WorldStateModifier(
    private val calculator: (currentCatchRate: Float, thrower: LivingEntity, entity: PokemonEntity) -> Float
) : CatchRateModifier {

    final override fun modifyCatchRate(
        currentCatchRate: Float,
        thrower: LivingEntity,
        pokemon: Pokemon,
        host: Pokemon?
    ): Float {
        val entity = pokemon.entity ?: return currentCatchRate
        return this.modifyCatchRate(currentCatchRate, thrower, entity)
    }

    open fun modifyCatchRate(currentCatchRate: Float, thrower: LivingEntity, entity: PokemonEntity): Float = this.calculator.invoke(currentCatchRate, thrower, entity)

}
