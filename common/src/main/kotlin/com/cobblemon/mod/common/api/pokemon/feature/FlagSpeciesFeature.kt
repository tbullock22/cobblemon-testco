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
import com.cobblemon.mod.common.api.properties.CustomPokemonProperty
import com.cobblemon.mod.common.api.properties.CustomPokemonPropertyType
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.substitute
import com.google.gson.JsonObject
import kotlin.random.Random
import net.minecraft.nbt.NbtCompound

/**
 * A simple [SpeciesFeature] that is a true/false flag value. It implements [CustomPokemonProperty] for use in
 * [PokemonProperties]. [FlagSpeciesFeatureProvider]s must be registered within [SpeciesFeatures].
 *
 * Implementations of this class don't need to implement anything.
 *
 * @author Hiroku
 * @since May 13th, 2022
 */
open class FlagSpeciesFeature(override val name: String) : SpeciesFeature, CustomPokemonProperty {
    constructor(name: String, enabled: Boolean): this(name) {
        this.enabled = enabled
    }

    var enabled = false
    override fun saveToNBT(pokemonNBT: NbtCompound): NbtCompound {
        pokemonNBT.putBoolean(name, enabled)
        return pokemonNBT
    }

    override fun loadFromNBT(pokemonNBT: NbtCompound): SpeciesFeature {
        enabled = if (pokemonNBT.contains(name)) pokemonNBT.getBoolean(name) else enabled
        return this
    }

    override fun saveToJSON(pokemonJSON: JsonObject): JsonObject {
        pokemonJSON.addProperty(name, enabled)
        return pokemonJSON
    }

    override fun loadFromJSON(pokemonJSON: JsonObject): SpeciesFeature {
        val isEnabled = pokemonJSON.get(name)?.asBoolean
        enabled = isEnabled ?: this.enabled
        return this
    }

    override fun asString() = "$name=$enabled"

    override fun apply(pokemon: Pokemon) {
        val featureProvider = SpeciesFeatures.getFeature(name) ?: return
        if (featureProvider in SpeciesFeatures.getFeaturesFor(pokemon.species)) {
            val existingFeature = pokemon.getFeature<FlagSpeciesFeature>(name)
            if (existingFeature != null) {
                existingFeature.enabled = enabled
            } else {
                pokemon.features.add(FlagSpeciesFeature(name, enabled))
            }
            pokemon.updateAspects()
        }
    }

    override fun matches(pokemon: Pokemon) = pokemon.getFeature<FlagSpeciesFeature>(name)?.enabled == enabled
}

class FlagSpeciesFeatureProvider : SpeciesFeatureProvider<FlagSpeciesFeature>, CustomPokemonPropertyType<FlagSpeciesFeature>, AspectProvider {
    override val keys: List<String>
    override val needsKey get() = true
    val default: String? = null
    val isAspect = true

    override fun examples() = setOf("true", "false")

    internal constructor() {
        this.keys = emptyList()
    }

    constructor(keys: List<String>) {
        this.keys = keys
    }

    constructor(vararg keys: String) : this(keys.toList())

    override fun invoke(pokemon: Pokemon): FlagSpeciesFeature? {
        return pokemon.getFeature(keys.first())
            ?: when (default) {
                "random" -> FlagSpeciesFeature(keys.first(), Random.Default.nextBoolean())
                in setOf("true", "false") -> FlagSpeciesFeature(keys.first(), default.toBoolean())
                else -> null
            }
    }

    override fun fromString(value: String?): FlagSpeciesFeature? {
        val isWeirdValue = value != null && value !in examples()

        if (isWeirdValue) {
            return null
        }

        return if (value == null) {
            FlagSpeciesFeature(keys.first(), true)
        } else {
            FlagSpeciesFeature(keys.first(), value.toBoolean())
        }
    }

    override fun provide(pokemon: Pokemon): Set<String> {
        return if (isAspect && pokemon.getFeature<FlagSpeciesFeature>(keys.first())?.enabled == true) {
            setOf(keys.first())
        } else {
            emptySet()
        }
    }

    override fun provide(properties: PokemonProperties): Set<String> {
        return if (isAspect &&  properties.customProperties.filterIsInstance<FlagSpeciesFeature>().find { it.name == keys.first() }?.enabled == true) {
            setOf(keys.first())
        } else {
            emptySet()
        }
    }
}