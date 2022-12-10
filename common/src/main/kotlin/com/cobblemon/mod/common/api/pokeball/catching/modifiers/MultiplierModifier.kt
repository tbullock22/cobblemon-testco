/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokeball.catching.modifiers

import com.cobblemon.mod.common.api.pokeball.catching.CatchRateModifier
import com.cobblemon.mod.common.pokemon.Pokemon
import java.util.function.Predicate
import net.minecraft.entity.LivingEntity
class MultiplierModifier(private val multiplier: Float, private val condition: (pokemon: Pokemon) -> Boolean =  { true }) : CatchRateModifier {
    override fun modifyCatchRate(currentCatchRate: Float, thrower: LivingEntity, pokemon: Pokemon, host: Pokemon?): Float {
        return if(condition(pokemon)) {
            currentCatchRate * multiplier
        } else {
            currentCatchRate
        }
    }
}