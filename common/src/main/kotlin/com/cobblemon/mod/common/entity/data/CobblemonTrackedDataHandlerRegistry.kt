/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.data

import com.cobblemon.mod.common.entity.pokemon.data.PokemonDisplayNameState
import net.minecraft.entity.data.TrackedDataHandler
import net.minecraft.entity.data.TrackedDataHandlerRegistry

object CobblemonTrackedDataHandlerRegistry {

    val POKEMON_DISPLAY_NAME_STATE = TrackedDataHandler.ofEnum(PokemonDisplayNameState::class.java)

    fun register() {
        TrackedDataHandlerRegistry.register(POKEMON_DISPLAY_NAME_STATE)
    }

}