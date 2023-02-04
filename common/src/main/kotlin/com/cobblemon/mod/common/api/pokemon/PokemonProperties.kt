/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.pokeball.PokeBalls
import com.cobblemon.mod.common.api.pokemon.aspect.AspectProvider
import com.cobblemon.mod.common.api.properties.CustomPokemonProperty
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.RenderablePokemon
import com.cobblemon.mod.common.util.DataKeys
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import com.cobblemon.mod.common.util.isInt
import com.cobblemon.mod.common.util.splitMap
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString
import net.minecraft.util.Identifier
import net.minecraft.util.InvalidIdentifierException
import net.minecraft.world.World

/**
 * A grouping of typical, selectable properties for a Pokémon. This is serializable
 * on its own and can be used to apply properties to a [Pokemon] or [PokemonEntity],
 * as well as check that a [Pokemon] or [PokemonEntity] matches all the defined
 * properties.
 *
 * Custom property types can be registered using [CustomPokemonProperty.register].
 *
 * Parsing a [PokemonProperties] from text is done using [PokemonProperties.parse]
 *
 * @author Hiroku
 * @since November 23rd, 2021
 */
open class PokemonProperties {
    companion object {
        fun parse(string: String, delimiter: String = " ", assigner: String = "="): PokemonProperties {
            val props = PokemonProperties()
            props.originalString = string
            val keyPairs = string.splitMap(delimiter, assigner)
            props.customProperties = CustomPokemonProperty.properties.mapNotNull { property ->
                val matchedKeyPair = keyPairs.find { it.first.lowercase() in property.keys }
                if (matchedKeyPair == null) {
                    if (!property.needsKey) {
                        var savedProperty: CustomPokemonProperty? = null
                        val keyPair = keyPairs.find { keyPair ->
                            savedProperty = property.fromString(keyPair.second)
                            return@find savedProperty != null
                        }
                        if (keyPair != null) {
                            keyPairs.remove(keyPair)
                        }
                        return@mapNotNull savedProperty
                    } else {
                        return@mapNotNull null
                    }
                } else {
                    keyPairs.remove(matchedKeyPair)
                    return@mapNotNull property.fromString(matchedKeyPair.second)
                }
            }.toMutableList()
            props.gender = Gender.values().toList().parsePropertyOfCollection(keyPairs, listOf("gender"), labelsOptional = true) { it.name.lowercase() }
            props.level = parseIntProperty(keyPairs, listOf("level", "lvl", "l"))?.coerceIn(1, Cobblemon.config.maxPokemonLevel)
            props.shiny = parseBooleanProperty(keyPairs, listOf("shiny", "s"))
            props.species = parseSpeciesIdentifier(keyPairs)
            props.form = parseForm(keyPairs)
            props.friendship = parseIntProperty(keyPairs, listOf("friendship"))?.coerceIn(0, Cobblemon.config.maxPokemonFriendship)
            props.pokeball = parseIdentifierOfRegistry(keyPairs, listOf("pokeball")) { identifier -> PokeBalls.getPokeBall(identifier)?.name?.toString() }
            props.nature = parseIdentifierOfRegistry(keyPairs, listOf("nature")) { identifier -> Natures.getNature(identifier)?.name?.toString() }
            props.updateAspects()
            return props
        }

        private fun getMatchedKeyPair(keyPairs: MutableList<Pair<String, String?>>, labels: Iterable<String>): Pair<String, String?>? {
            return keyPairs.findLast { it.first in labels }
        }

        private fun parseIntProperty(keyPairs: MutableList<Pair<String, String?>>, labels: Iterable<String>): Int? {
            val matchingKeyPair = getMatchedKeyPair(keyPairs, labels) ?: return null
            val value = matchingKeyPair.second
            return if (value == null || !value.isInt()) {
                null
            } else {
                value.toInt()
            }
        }

        private fun parseIdentifierOfRegistry(keyPairs: MutableList<Pair<String, String?>>, validKeys: List<String>, valueFetcher: (Identifier) -> String?): String? {
            val matched = getMatchedKeyPair(keyPairs, validKeys) ?: return null
            val value = matched.second?.lowercase() ?: return null
            return try {
                val identifier = value.asIdentifierDefaultingNamespace()
                valueFetcher(identifier)
            } catch (_: InvalidIdentifierException) {
                null
            }
        }

        private fun parseSpeciesIdentifier(keyPairs: MutableList<Pair<String, String?>>): String? {
            val matched = getMatchedKeyPair(keyPairs, listOf("species"))
            if (matched != null) {
                val value = matched.second?.lowercase() ?: return null
                return if (value.lowercase() == "random") {
                    "random"
                } else {
                    try {
                        val species = PokemonSpecies.getByIdentifier(value.asIdentifierDefaultingNamespace()) ?: return null
                        return if (species.resourceIdentifier.namespace == Cobblemon.MODID) species.resourceIdentifier.path else species.resourceIdentifier.toString()
                    } catch (e: InvalidIdentifierException) {
                        return null
                    }
                }
            } else {
                var species: String? = null

                val keyPair = keyPairs.find { pair ->
                    species = if (pair.second == null && pair.first.lowercase() == "random") {
                        "random"
                    } else {
                        try {
                            val identifier = pair.first.asIdentifierDefaultingNamespace()
                            val found = PokemonSpecies.getByIdentifier(identifier) ?: return@find false
                            if (found.resourceIdentifier.namespace == Cobblemon.MODID) found.resourceIdentifier.path else found.resourceIdentifier.toString()
                        } catch (e: InvalidIdentifierException) {
                            return@find false
                        }
                    }
                    return@find species != null
                }

                if (keyPair != null) {
                    keyPairs.remove(keyPair)
                }

                return species
            }
        }

        private fun parseForm(keyPairs: MutableList<Pair<String, String?>>): String? {
            val matchingKeyPair = getMatchedKeyPair(keyPairs, listOf("form")) ?: return null
            keyPairs.remove(matchingKeyPair)
            return matchingKeyPair.second
        }

        private fun parseBooleanProperty(keyPairs: MutableList<Pair<String, String?>>, labels: Iterable<String>): Boolean? {
            val matchingKeyPair = getMatchedKeyPair(keyPairs, labels) ?: return null
            keyPairs.remove(matchingKeyPair)
            return when (matchingKeyPair.second?.lowercase()) {
                null -> true
                "true", "yes" -> true
                "false", "no" -> false
                else -> null
            }
        }

        private fun <T> Iterable<T>.parsePropertyOfCollection(
            keyPairs: MutableList<Pair<String, String?>>,
            labels: Iterable<String>,
            labelsOptional: Boolean = false,
            stringer: (T) -> String
        ): T? {
            val matchingKeyPair = getMatchedKeyPair(keyPairs, labels)
            if (matchingKeyPair != null) {
                val value = matchingKeyPair.second?.lowercase()
                return if (value == null) {
                    null
                } else {
                    val matched = find { stringer(it).lowercase() == value }
                    if (matched != null) {
                        keyPairs.remove(matchingKeyPair)
                    }
                    matched
                }
            } else if (labelsOptional) {
                val keyPair = keyPairs.firstOrNull { pair -> any { stringer(it).lowercase() == pair.first } }
                if (keyPair != null) {
                    keyPairs.remove(keyPair)
                    return first { stringer(it).lowercase() == keyPair.first }
                }
            }
            return null
        }
    }

    var originalString: String = ""

    var species: String? = null
    var form: String? = null
    var shiny: Boolean? = null
    var gender: Gender? = null
    var level: Int? = null
    var friendship: Int? = null
    var pokeball: String? = null
    var nature: String? = null
    var aspects: Set<String> = emptySet()

    var customProperties = mutableListOf<CustomPokemonProperty>()

    fun asRenderablePokemon() = RenderablePokemon(
        species = species?.let {
            return@let try {
                PokemonSpecies.getByIdentifier(it.asIdentifierDefaultingNamespace())
            } catch (e: InvalidIdentifierException) {
                PokemonSpecies.random()
            }
        } ?: PokemonSpecies.random(),
        aspects = aspects
    )

    fun apply(pokemon: Pokemon) {
        species?.let {
            return@let try {
                if (it == "random") {
                    PokemonSpecies.species.random()
                } else {
                    PokemonSpecies.getByIdentifier(it.asIdentifierDefaultingNamespace())
                }
            } catch (e: InvalidIdentifierException) {
                null
            }
        }?.let { pokemon.species = it }
        form?.let { formID -> pokemon.species.forms.firstOrNull { it.formOnlyShowdownId().equals(formID, true) } }?.let { form -> pokemon.form = form }
        shiny?.let { pokemon.shiny = it }
        gender?.let { pokemon.gender = it }
        level?.let { pokemon.level = it }
        friendship?.let { pokemon.setFriendship(it) }
        pokeball?.let { PokeBalls.getPokeBall(it.asIdentifierDefaultingNamespace())?.let { pokeball -> pokemon.caughtBall = pokeball } }
        nature?.let { Natures.getNature(it.asIdentifierDefaultingNamespace())?.let { nature -> pokemon.nature = nature } }
        customProperties.forEach { it.apply(pokemon) }
    }

    fun apply(pokemonEntity: PokemonEntity) {
        species?.let {
            return@let try {
                if (it == "random") {
                    PokemonSpecies.species.random()
                } else {
                    PokemonSpecies.getByIdentifier(it.asIdentifierDefaultingNamespace())
                }
            } catch (e: InvalidIdentifierException) {
                null
            }
        }?.let { pokemonEntity.pokemon.species = it }
        form?.let { formID -> pokemonEntity.pokemon.species.forms.firstOrNull { it.formOnlyShowdownId().equals(formID, true) } }?.let { form -> pokemonEntity.pokemon.form = form }
        level?.let { pokemonEntity.pokemon.level = it }
        shiny?.let { pokemonEntity.pokemon.shiny = it }
        gender?.let { pokemonEntity.pokemon.gender = it }
        friendship?.let { pokemonEntity.pokemon.setFriendship(it) }
        pokeball?.let { PokeBalls.getPokeBall(it.asIdentifierDefaultingNamespace())?.let { pokeball -> pokemonEntity.pokemon.caughtBall = pokeball } }
        nature?.let { Natures.getNature(it.asIdentifierDefaultingNamespace())?.let { nature -> pokemonEntity.pokemon.nature = nature } }
        customProperties.forEach { it.apply(pokemonEntity) }
    }

    fun matches(pokemon: Pokemon): Boolean {
        level?.takeIf { it != pokemon.level }?.let { return false }
        shiny?.takeIf { it != pokemon.shiny }?.let { return false }
        gender?.takeIf { it != pokemon.gender }?.let { return false }
        species?.run {
            try {
                val species = if (this == "random") {
                    PokemonSpecies.species.random()
                } else {
                    PokemonSpecies.getByIdentifier(this.asIdentifierDefaultingNamespace()) ?: return@run
                }
                if (pokemon.species != species) {
                    return false
                }
            } catch (e: InvalidIdentifierException) {
                return false
            }
        }
        form?.takeIf { !it.equals(pokemon.form.name, true) }?.let { return false }
        friendship?.takeIf { it != pokemon.friendship }?.let { return false }
        pokeball?.takeIf { it != pokemon.caughtBall.name.toString() }?.let { return false }
        nature?.takeIf { it != pokemon.nature.name.toString() }?.let { return false }
        return customProperties.none { !it.matches(pokemon) }
    }

    fun matches(pokemonEntity: PokemonEntity): Boolean {
        level?.takeIf { it != pokemonEntity.pokemon.level }?.let { return false }
        shiny?.takeIf { it != ("shiny" in pokemonEntity.pokemon.aspects) }?.let { return false }
        gender?.takeIf { it != pokemonEntity.pokemon.gender }?.let { return false }
        species?.run {
            try {
                val species = if (this == "random") {
                    PokemonSpecies.species.random()
                } else {
                    PokemonSpecies.getByIdentifier(this.asIdentifierDefaultingNamespace()) ?: return@run
                }
                if (pokemonEntity.pokemon.species != species) {
                    return false
                }
            } catch (e: InvalidIdentifierException) {}
        }
        form?.takeIf { !it.equals(pokemonEntity.pokemon.form.name, true) }?.let { return false }
        friendship?.takeIf { it != pokemonEntity.pokemon.friendship }?.let { return false }
        pokeball?.takeIf { it != pokemonEntity.pokemon.caughtBall.name.toString() }?.let { return false }
        nature?.takeIf { it != pokemonEntity.pokemon.nature.name.toString() }?.let { return false }
        return customProperties.none { !it.matches(pokemonEntity) }
    }

    fun create(): Pokemon {
        val pokemon = Pokemon()
        apply(pokemon)
        // Force the setter to initialize it
        pokemon.species = pokemon.species
        // This should only be a thing once we have moveset control in properties until then a creation should require a moveset init.
        /*
        if (pokemon.moveSet.none { it != null }) {
            pokemon.initializeMoveset()
        }
         */
        pokemon.initializeMoveset()
        return pokemon
    }

    fun createEntity(world: World): PokemonEntity {
        return PokemonEntity(world, create())
    }

    fun saveToNBT(): NbtCompound {
        val nbt = NbtCompound()
        originalString.let { nbt.putString(DataKeys.POKEMON_PROPERTIES_ORIGINAL_TEXT, it) }
        level?.let { nbt.putInt(DataKeys.POKEMON_LEVEL, it) }
        shiny?.let { nbt.putBoolean(DataKeys.POKEMON_SHINY, it) }
        gender?.let { nbt.putString(DataKeys.POKEMON_GENDER, it.name) }
        species?.let { nbt.putString(DataKeys.POKEMON_SPECIES_TEXT, it) }
        form?.let { nbt.putString(DataKeys.POKEMON_FORM_ID, it) }
        friendship?.let { nbt.putInt(DataKeys.POKEMON_FRIENDSHIP, it) }
        pokeball?.let { nbt.putString(DataKeys.POKEMON_CAUGHT_BALL, it) }
        nature?.let { nbt.putString(DataKeys.POKEMON_NATURE, it) }
        val custom = NbtList()
        customProperties.map { NbtString.of(it.asString()) }.forEach { custom.add(it) }
        nbt.put(DataKeys.POKEMON_PROPERTIES_CUSTOM, custom)
        return nbt
    }

    fun loadFromNBT(tag: NbtCompound): PokemonProperties {
        originalString = tag.getString(DataKeys.POKEMON_PROPERTIES_ORIGINAL_TEXT)
        level = if (tag.contains(DataKeys.POKEMON_LEVEL)) tag.getInt(DataKeys.POKEMON_LEVEL) else null
        shiny = if (tag.contains(DataKeys.POKEMON_SHINY)) tag.getBoolean(DataKeys.POKEMON_SHINY) else null
        gender = if (tag.contains(DataKeys.POKEMON_GENDER)) Gender.valueOf(tag.getString(DataKeys.POKEMON_GENDER)) else null
        species = if (tag.contains(DataKeys.POKEMON_SPECIES_TEXT)) tag.getString(DataKeys.POKEMON_SPECIES_TEXT) else null
        form = if (tag.contains(DataKeys.POKEMON_FORM_ID)) tag.getString(DataKeys.POKEMON_FORM_ID) else null
        friendship = if (tag.contains(DataKeys.POKEMON_FRIENDSHIP)) tag.getInt(DataKeys.POKEMON_FRIENDSHIP) else null
        pokeball = if (tag.contains(DataKeys.POKEMON_CAUGHT_BALL)) tag.getString(DataKeys.POKEMON_CAUGHT_BALL) else null
        nature = if (tag.contains(DataKeys.POKEMON_NATURE)) tag.getString(DataKeys.POKEMON_NATURE) else null
        val custom = tag.getList(DataKeys.POKEMON_PROPERTIES_CUSTOM, NbtElement.STRING_TYPE.toInt())
        // This is kinda gross
        custom.forEach { customProperties.addAll(parse(it.asString()).customProperties) }
        updateAspects()
        return this
    }

    fun saveToJSON(): JsonObject {
        val json = JsonObject()
        originalString.let { json.addProperty(DataKeys.POKEMON_PROPERTIES_ORIGINAL_TEXT, it) }
        level?.let { json.addProperty(DataKeys.POKEMON_LEVEL, it) }
        shiny?.let { json.addProperty(DataKeys.POKEMON_SHINY, it) }
        gender?.let { json.addProperty(DataKeys.POKEMON_GENDER, it.name) }
        species?.let { json.addProperty(DataKeys.POKEMON_SPECIES_TEXT, it) }
        form?.let { json.addProperty(DataKeys.POKEMON_FORM_ID, it) }
        friendship?.let { json.addProperty(DataKeys.POKEMON_FRIENDSHIP, it) }
        pokeball?.let { json.addProperty(DataKeys.POKEMON_CAUGHT_BALL, it) }
        nature?.let { json.addProperty(DataKeys.POKEMON_NATURE, it) }
        val custom = JsonArray()
        customProperties.map { it.asString() }.forEach { custom.add(it) }
        json.add(DataKeys.POKEMON_PROPERTIES_CUSTOM, custom)

        return json
    }

    fun loadFromJSON(json: JsonObject): PokemonProperties {
        originalString = json.get(DataKeys.POKEMON_PROPERTIES_ORIGINAL_TEXT)?.asString ?: ""
        level = json.get(DataKeys.POKEMON_LEVEL)?.asInt
        shiny = json.get(DataKeys.POKEMON_SHINY)?.asBoolean
        gender = json.get(DataKeys.POKEMON_GENDER)?.asString?.let { Gender.valueOf(it) }
        species = json.get(DataKeys.POKEMON_SPECIES_TEXT)?.asString
        form = json.get(DataKeys.POKEMON_FORM_ID)?.asString
        friendship = json.get(DataKeys.POKEMON_FRIENDSHIP)?.asInt
        pokeball = json.get(DataKeys.POKEMON_CAUGHT_BALL)?.asString
        nature = json.get(DataKeys.POKEMON_NATURE)?.asString
        val custom = json.get(DataKeys.POKEMON_PROPERTIES_CUSTOM)?.asJsonArray
        // This is still kinda gross
        custom?.forEach { customProperties.addAll(parse(it.asString).customProperties) }
        updateAspects()
        return this
    }

    fun asString(separator: String = " "): String {
        val pieces = mutableListOf<String>()
        species?.let { pieces.add(it) }
        form?.let { pieces.add("form=$it") }
        level?.let { pieces.add("level=$it") }
        shiny?.let { pieces.add("shiny=$it") }
        gender?.let { pieces.add("gender=$it")}
        friendship?.let { pieces.add("friendship=$it") }
        pokeball?.let { pieces.add("pokeball=$it") }
        nature?.let { pieces.add("nature=$it") }
        customProperties.forEach { pieces.add(it.asString()) }
        return pieces.joinToString(separator)
    }

    fun updateAspects() {
        val aspects = mutableSetOf<String>()
        AspectProvider.providers.forEach { aspects.addAll(it.provide(this)) }
        this.aspects = aspects.toSet()
    }

    fun copy(): PokemonProperties {
        return PokemonProperties().loadFromJSON(saveToJSON())
    }
}
