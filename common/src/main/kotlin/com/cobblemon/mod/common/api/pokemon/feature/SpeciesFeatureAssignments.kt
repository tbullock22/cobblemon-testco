/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.feature

import com.cobblemon.mod.common.api.data.JsonDataRegistry
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.minecraft.resource.ResourceType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier

/**
 * A registry of assignments combining [SpeciesFeatures] and [PokemonSpecies]. This is a way around the issue
 * of when multiple data packs want to add their own [SpeciesFeature]s to the same species. The correct way,
 * with this registry, is to add a new JSON that joins together a list of species with a list of species feature
 * keys.
 *
 * @author Hiroku
 * @since December 1st, 2022
 */
object SpeciesFeatureAssignments : JsonDataRegistry<SpeciesFeatureAssignment> {
    override val id: Identifier = cobblemonResource("species_feature_assignments")
    override val type: ResourceType = ResourceType.SERVER_DATA
    override val observable = SimpleObservable<SpeciesFeatureAssignments>()

    override val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    override val typeToken = TypeToken.get(SpeciesFeatureAssignment::class.java)
    override val resourcePath = "species_feature_assignments"

    private val assignments = mutableMapOf<String, MutableSet<String>>()

    override fun sync(player: ServerPlayerEntity) {}
    override fun reload(data: Map<Identifier, SpeciesFeatureAssignment>) {
        data.values.forEach {
            it.pokemon.forEach { pokemon ->
                assignments.getOrPut(pokemon) { mutableSetOf() }.addAll(it.features)
            }
        }
        this.observable.emit(this)
    }

    fun getFeatures(species: Species) = assignments[species.showdownId()] ?: emptySet()
}