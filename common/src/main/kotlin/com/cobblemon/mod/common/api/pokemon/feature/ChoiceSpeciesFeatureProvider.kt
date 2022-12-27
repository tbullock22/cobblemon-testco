/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.feature

import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.aspect.AspectProvider
import com.cobblemon.mod.common.api.properties.CustomPokemonPropertyType
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.substitute

/**
 * A [SpeciesFeatureProvider] which is a string value selected from a fixed list of choices. Parameters exist
 * to change default behaviour, aspects, and the available choices. Choices must be lowercase.
 *
 * @author Hiroku
 * @since November 30th, 2022
 */
open class ChoiceSpeciesFeatureProvider(
    override val keys: List<String>,
    var default: String? = null,
    var choices: List<String> = listOf(),
    var isAspect: Boolean = true,
    var aspectFormat: String = "{{choice}}"
) : SpeciesFeatureProvider<StringSpeciesFeature>, CustomPokemonPropertyType<StringSpeciesFeature>, AspectProvider {
    override val needsKey = true
    fun getAspect(feature: StringSpeciesFeature) = aspectFormat.substitute("choice", feature.value)

    override fun examples() = choices

    internal constructor(): this(emptyList())

    fun get(pokemon: Pokemon) = pokemon.getFeature<StringSpeciesFeature>(keys.first())

    override fun invoke(pokemon: Pokemon): StringSpeciesFeature? {
        val existing = pokemon.getFeature<StringSpeciesFeature>(keys.first())
        return if (existing != null && existing.value in choices) {
            existing
        } else {
            val value = if (default in choices) {
                default!!
            } else if (default == "random") {
                // If it's mandatory, but they provided no value and no default, give it a random value.
                choices.randomOrNull() ?: throw IllegalStateException("The 'choices' list is empty for species feature provider: ${keys.joinToString()}")
            } else {
                return null
            }

            fromString(value)
        }
    }

    override fun fromString(value: String?): StringSpeciesFeature? {
        val lower = value?.lowercase()
        if (lower == null || lower !in choices) {
            return null
        }

        return StringSpeciesFeature(keys.first(), lower)
    }

    override fun provide(pokemon: Pokemon): Set<String> {
        return if (isAspect) {
            pokemon.getFeature<StringSpeciesFeature>(keys.first())?.let { setOf(getAspect(it)) } ?: emptySet()
        } else {
            emptySet()
        }
    }

    override fun provide(properties: PokemonProperties): Set<String> {
        return if (isAspect) {
            val feature = properties.customProperties.filterIsInstance<StringSpeciesFeature>().find { it.name == keys.first() }
            if (feature != null) {
                setOf(getAspect(feature))
            } else {
                emptySet()
            }
        } else {
            emptySet()
        }
    }
}