/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.conditional.RegistryLikeCondition
import com.cobblemon.mod.common.api.data.JsonDataRegistry
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.api.spawning.condition.SpawningCondition
import com.cobblemon.mod.common.api.spawning.context.RegisteredSpawningContext
import com.cobblemon.mod.common.api.spawning.preset.SpawnDetailPreset
import com.cobblemon.mod.common.util.adapters.BiomeLikeConditionAdapter
import com.cobblemon.mod.common.util.adapters.BlockLikeConditionAdapter
import com.cobblemon.mod.common.util.adapters.FluidLikeConditionAdapter
import com.cobblemon.mod.common.util.adapters.IdentifierAdapter
import com.cobblemon.mod.common.util.adapters.IntRangesAdapter
import com.cobblemon.mod.common.util.adapters.RegisteredSpawningContextAdapter
import com.cobblemon.mod.common.util.adapters.SpawnBucketAdapter
import com.cobblemon.mod.common.util.adapters.SpawnDetailPresetAdapter
import com.cobblemon.mod.common.util.adapters.SpawningConditionAdapter
import com.cobblemon.mod.common.util.adapters.pokemonPropertiesShortAdapter
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.minecraft.block.Block
import net.minecraft.fluid.Fluid
import net.minecraft.resource.ResourceType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.world.biome.Biome

/**
 * Data registry for [SpawnDetailPreset]s. These help the maintainability of spawn files by allowing common presets
 * to be defined separately to the spawns that obey it. You can register custom presets either programmatically or
 * by adding preset JSONs to the spawn_detail_presets data folder.
 *
 * @author Hiroku
 * @since December 9th, 2022
 */
object SpawnDetailPresets : JsonDataRegistry<SpawnDetailPreset> {
    val GSON = GsonBuilder()
        .setPrettyPrinting()
        .setLenient()
        .disableHtmlEscaping()
        .registerTypeAdapter(SpawnBucket::class.java, SpawnBucketAdapter)
        .registerTypeAdapter(RegisteredSpawningContext::class.java, RegisteredSpawningContextAdapter)
        .registerTypeAdapter(TypeToken.getParameterized(RegistryLikeCondition::class.java, Biome::class.java).type, BiomeLikeConditionAdapter)
        .registerTypeAdapter(TypeToken.getParameterized(RegistryLikeCondition::class.java, Block::class.java).type, BlockLikeConditionAdapter)
        .registerTypeAdapter(TypeToken.getParameterized(RegistryLikeCondition::class.java, Fluid::class.java).type, FluidLikeConditionAdapter)
        .registerTypeAdapter(SpawnDetailPreset::class.java, SpawnDetailPresetAdapter)
        .registerTypeAdapter(Identifier::class.java, IdentifierAdapter)
        .registerTypeAdapter(SpawningCondition::class.java, SpawningConditionAdapter)
        .registerTypeAdapter(TimeRange::class.java, IntRangesAdapter(TimeRange.timeRanges) { TimeRange(*it) })
        .registerTypeAdapter(MoonPhaseRange::class.java, IntRangesAdapter(MoonPhaseRange.moonPhaseRanges) { MoonPhaseRange(*it) })
        .registerTypeAdapter(PokemonProperties::class.java, pokemonPropertiesShortAdapter)
        .create()

    val presetTypes = mutableMapOf<String, Class<out SpawnDetailPreset>>()
    fun <T : SpawnDetailPreset> registerPresetType(name: String, detailClass: Class<T>) {
        presetTypes[name] = detailClass
    }

    override val gson: Gson = GSON
    override val typeToken = TypeToken.get(SpawnDetailPreset::class.java)
    override val resourcePath = "spawn_detail_presets"
    override val id = cobblemonResource(resourcePath)
    override val type = ResourceType.SERVER_DATA
    override val observable = SimpleObservable<SpawnDetailPresets>()

    var presets = mutableMapOf<Identifier, SpawnDetailPreset>()

    override fun sync(player: ServerPlayerEntity) {}
    override fun reload(data: Map<Identifier, SpawnDetailPreset>) {
        this.presets = data.toMutableMap()
        Cobblemon.LOGGER.info("Loaded ${presets.size} spawn detail presets.")
    }
}