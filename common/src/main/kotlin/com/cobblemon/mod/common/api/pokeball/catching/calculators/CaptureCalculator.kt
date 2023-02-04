/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokeball.catching.calculators

import com.cobblemon.mod.common.api.pokeball.catching.CaptureContext
import com.cobblemon.mod.common.pokeball.PokeBall
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.entity.LivingEntity

/**
 * Used to process Pokémon captures.
 * This interface is here with the intention that several capture calculators can be created,
 * i.e. supporting an earlier generation capture system.
 *
 * To register a calculator in order to be used in the Cobblemon config use [CaptureCalculators.register].
 *
 * @author landonjw
 * @since November 30, 2021
 */
interface CaptureCalculator {

    /**
     * The literal ID of this calculator.
     * Used when registering the calculator to the registry in order to be used by the game rule.
     *
     * @return The literal ID of this calculator.
     */
    fun id(): String

    /**
     * Processes a capture attempt with the given params.
     *
     * @param thrower The [LivingEntity] that threw the [PokeBall].
     * @param pokeBall The [PokeBall] used.
     * @param target The target [Pokemon] attempting to be captured.
     * @return
     */
    fun processCapture(thrower: LivingEntity, pokeBall: PokeBall, target: Pokemon) : CaptureContext

}