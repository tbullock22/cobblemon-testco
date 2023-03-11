/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.data

import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.Species
import net.minecraft.text.Text

/**
 * Represents the display name state of a Pokémon entity.
 *
 * @property shouldRender If this state should render a label on the Pokémon.
 * @property nameResolver Resolves the display name based on this state, can be null.
 *
 * @author Licious
 * @since February 9th, 2023
 */
enum class PokemonDisplayNameState(val shouldRender: Boolean, val nameResolver: (pokemon: Pokemon) -> Text?) {

    /**
     * The state where the displayed name is the [Species.translatedName].
     */
    SPECIES(true, { pokemon -> pokemon.species.translatedName }),

    // ToDo Change once nickname is implemented
    /**
     * The state where the displayed name is the [Pokemon.nickname].
     */
    NICKNAME(true, { pokemon -> null /* pokemon.nickname */ }),

    /**
     * The state where the name isn't displayed.
     */
    NONE(false, { null })

}