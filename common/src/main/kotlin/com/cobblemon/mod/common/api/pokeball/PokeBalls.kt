/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokeball

import com.cobblemon.mod.common.api.pokeball.catching.modifiers.GuaranteedModifier
import com.cobblemon.mod.common.api.pokeball.catching.modifiers.MultiplierModifier
import com.cobblemon.mod.common.pokeball.PokeBall
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.util.Identifier

/**
 * Main API point for Pokeballs
 *
 * Get or register Pokeballs
 */
object PokeBalls {
    private val allPokeBalls = mutableListOf<PokeBall>()

    val POKE_BALL = registerPokeBall(PokeBall(cobblemonResource("poke_ball")))
    val VERDANT_BALL = registerPokeBall(PokeBall(cobblemonResource("verdant_ball")))
    val SPORT_BALL = registerPokeBall(PokeBall(cobblemonResource("sport_ball")))
    val SLATE_BALL = registerPokeBall(PokeBall(cobblemonResource("slate_ball")))
    val ROSEATE_BALL = registerPokeBall(PokeBall(cobblemonResource("roseate_ball")))
    val AZURE_BALL = registerPokeBall(PokeBall(cobblemonResource("azure_ball")))
    val CITRINE_BALL = registerPokeBall(PokeBall(cobblemonResource("citrine_ball")))
    val GREAT_BALL = registerPokeBall(PokeBall(cobblemonResource("great_ball"), listOf(MultiplierModifier(1.5f, null))))
    val ULTRA_BALL = registerPokeBall(PokeBall(cobblemonResource("ultra_ball"), listOf(MultiplierModifier(2.0f, null))))
    val MASTER_BALL = registerPokeBall(PokeBall(cobblemonResource("master_ball"), listOf(GuaranteedModifier())))

    /**
     * Registers a new pokeball type.
     * @return the pokeball type.
     */
    fun registerPokeBall(pokeBall: PokeBall) : PokeBall {
        allPokeBalls.add(pokeBall)
        return pokeBall
    }

    /**
     * Gets a Pokeball from registry name.
     * @return the pokeball object if found otherwise null.
     */
    fun getPokeBall(name : Identifier) : PokeBall? {
        return allPokeBalls.find { pokeball -> pokeball.name == name }
    }

    fun all() = this.allPokeBalls.toList()

}