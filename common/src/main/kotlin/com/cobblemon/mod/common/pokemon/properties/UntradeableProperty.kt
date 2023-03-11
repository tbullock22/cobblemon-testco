/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.properties

import com.cobblemon.mod.common.api.properties.CustomPokemonPropertyType

/**
 * A type of [CustomPokemonPropertyType] handling a [FlagProperty] which, when
 * present, indicates that the Pokémon cannot be traded by any means.
 *
 * @author Hiroku
 * @since July 1st, 2022
 */
object UntradeableProperty : CustomPokemonPropertyType<FlagProperty> {
    override val keys = setOf("untradeable")
    override val needsKey = true

    override fun fromString(value: String?) =
        when {
            value == null || value.lowercase() in listOf("true", "yes") -> untradeable()
            value.lowercase() in listOf("false", "no") -> tradeable()
            else -> null
        }

    fun tradeable() = FlagProperty(keys.first(), true)
    fun untradeable() = FlagProperty(keys.first(), false)
    override fun examples() = setOf("yes", "no")
}