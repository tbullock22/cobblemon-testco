/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.Cobblemon.LOGGER
import com.cobblemon.mod.common.Cobblemon.config
import com.cobblemon.mod.common.api.conditional.RegistryLikeCondition
import com.cobblemon.mod.common.api.drop.DropEntry
import com.cobblemon.mod.common.api.drop.ItemDropMethod
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.spawning.condition.SpawningCondition
import com.cobblemon.mod.common.api.spawning.condition.TimeRange
import com.cobblemon.mod.common.api.spawning.context.RegisteredSpawningContext
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail
import com.cobblemon.mod.common.api.spawning.detail.SpawnPool
import com.cobblemon.mod.common.util.AssetLoading
import com.cobblemon.mod.common.util.AssetLoading.toPath
import com.cobblemon.mod.common.util.adapters.BiomeLikeConditionAdapter
import com.cobblemon.mod.common.util.adapters.BlockLikeConditionAdapter
import com.cobblemon.mod.common.util.adapters.DropEntryAdapter
import com.cobblemon.mod.common.util.adapters.IdentifierAdapter
import com.cobblemon.mod.common.util.adapters.IntRangeAdapter
import com.cobblemon.mod.common.util.adapters.NbtCompoundAdapter
import com.cobblemon.mod.common.util.adapters.RegisteredSpawningContextAdapter
import com.cobblemon.mod.common.util.adapters.SpawnBucketAdapter
import com.cobblemon.mod.common.util.adapters.SpawnDetailAdapter
import com.cobblemon.mod.common.util.adapters.SpawningConditionAdapter
import com.cobblemon.mod.common.util.adapters.TimeRangeAdapter
import com.cobblemon.mod.common.util.adapters.pokemonPropertiesShortAdapter
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.fromJson
import com.cobblemon.mod.common.util.isHigherVersion
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.file.Path
import kotlin.io.path.pathString
import net.minecraft.block.Block
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Identifier
import net.minecraft.world.biome.Biome

/**
 * Object responsible for actually deserializing spawns. You should probably
 * rely on this object for it as it would make your code better future proofed.
 *
 * @author Hiroku
 * @since January 31st, 2022
 */
object SpawnLoader {
    val gson = GsonBuilder()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .setLenient()
        .registerTypeAdapter(TypeToken.getParameterized(RegistryLikeCondition::class.java, Biome::class.java).type, BiomeLikeConditionAdapter)
        .registerTypeAdapter(TypeToken.getParameterized(RegistryLikeCondition::class.java, Block::class.java).type, BlockLikeConditionAdapter)
        .registerTypeAdapter(RegisteredSpawningContext::class.java, RegisteredSpawningContextAdapter)
        .registerTypeAdapter(Identifier::class.java, IdentifierAdapter)
        .registerTypeAdapter(SpawnDetail::class.java, SpawnDetailAdapter)
        .registerTypeAdapter(DropEntry::class.java, DropEntryAdapter)
        .registerTypeAdapter(SpawningCondition::class.java, SpawningConditionAdapter)
        .registerTypeAdapter(TimeRange::class.java, TimeRangeAdapter)
        .registerTypeAdapter(ItemDropMethod::class.java, ItemDropMethod.adapter)
        .registerTypeAdapter(PokemonProperties::class.java, pokemonPropertiesShortAdapter)
        .registerTypeAdapter(SpawnBucket::class.java, SpawnBucketAdapter)
        .registerTypeAdapter(NbtCompound::class.java, NbtCompoundAdapter)
        .registerTypeAdapter(IntRange::class.java, IntRangeAdapter)
        .create()

    var deserializingConditionClass: Class<out SpawningCondition<*>>? = null

//    fun load(folder: String): SpawnPool {
//        val pool = SpawnPool()
//
//        val external = getExternalSets(folder)
//        val internal = loadInternalFolder(folder)
//
//        external.forEach { set ->
//            val matchingInternal = internal.find { it.id == set.id }
//            if (set.preventOverwrite) {
//                if (matchingInternal != null) {
//                    internal.remove(matchingInternal)
//                }
//                if (set.isEnabled()) {
//                    pool.details.addAll(set.filter { it.isModDependencySatisfied() })
//                }
//            } else {
//                if (matchingInternal != null && matchingInternal.version.isHigherVersion(set.version)) {
//                    if (matchingInternal.isEnabled()) {
//                        pool.details.addAll(matchingInternal.filter { it.isModDependencySatisfied() })
//                    }
//                    internal.remove(matchingInternal)
//                    exportSet(folder, matchingInternal)
//                }
//            }
//        }
//
//        internal.forEach { set ->
//            if (set.isEnabled()) {
//                pool.details.addAll(set.filter { it.isModDependencySatisfied() })
//            }
//            if (config.exportSpawnsToConfig) {
//                exportSet(folder, set)
//            }
//        }
//
//        pool.precalculate()
//
//        return pool
//    }
//
//    fun exportSet(folder: String, set: SpawnSet) {
//        val file = File("./config/cobblemon/spawning/spawns/$folder/${set.id.lowercase()}.set.json")
//        file.parentFile.mkdirs()
//        file.createNewFile()
//
//        val internalStream = Cobblemon::class.java.getResourceAsStream(set.path.toAbsolutePath().toString())!!
//        val bytes = internalStream.readAllBytes()
//        internalStream.close()
//
//        val externalStream = FileOutputStream(file)
//        externalStream.write(bytes)
//        externalStream.close()
//    }
//
//    fun getExternalSets(folder: String): MutableList<SpawnSet> {
//        val sets = mutableListOf<SpawnSet>()
//        val files = mutableListOf<File>()
//        AssetLoading.searchFor(
//            dir = "config/cobblemon/spawning/spawns/$folder",
//            suffix = ".json",
//            list = files
//        )
//        val paths = files.map { it.toPath() }
//
//        sets.addAll(
//            paths.mapNotNull {
//                try {
//                    val file = it.toFile()
//                    val reader = FileReader(file)
//                    val set = gson.fromJson<SpawnSet>(reader)
//                    reader.close()
//                    return@mapNotNull set
//                } catch (exception : Exception) {
//                    LOGGER.error("Unable to load spawn set from ${it.pathString}")
//                    exception.printStackTrace()
//                    null
//                }
//            }
//        )
//
//        return sets
//    }
//
//    fun loadSetFromPath(path: Path): SpawnSet {
//        val pathString = if (path.toString().startsWith("/")) path.toString() else "/$path"
//        val inputStream = Cobblemon.javaClass.getResourceAsStream(pathString)
//            ?: throw IllegalArgumentException("Unable to open resource stream for: $pathString")
//        val reader = InputStreamReader(inputStream)
//        val set = gson.fromJson<SpawnSet>(reader)
//        reader.close()
//        return set
//    }
//
//    fun loadInternalFolder(folder: String): MutableList<SpawnSet> {
//        val internalFolder = cobblemonResource("spawning/spawns/$folder")
//        val path = internalFolder.toPath() ?: throw IllegalArgumentException("There is no valid, internal path for $internalFolder")
//        val internalPaths = AssetLoading.fileSearch(path, { it.toString().endsWith(".json") }, recursive = true)
//        val sets = mutableListOf<SpawnSet>()
//        sets.addAll(internalPaths.flatMap { filePath -> loadInternalFile(filePath) })
//        return sets
//    }
//
//    fun loadInternalFile(path: Path): MutableList<SpawnSet> {
//        if (path.toString().endsWith(".packed.json")) {
//            return loadPackedSets(path).onEach { it.path = path }
//        } else if (path.toString().endsWith(".json")) {
//            return mutableListOf(loadSetFromPath(path)).onEach { it.path = path }
//        }
//
//        return mutableListOf()
//    }
//
//    fun loadPackedSets(path: Path): MutableList<SpawnSet> {
//        val fileStream = Cobblemon::class.java.getResourceAsStream(path.toAbsolutePath().toString())
//            ?: throw IllegalArgumentException("Unable to create input stream for $path")
//
//        return try {
//            loadPackedSetsFromStream(fileStream)
//        } catch (e: Exception) {
//            LOGGER.error(e.message)
//            return mutableListOf()
//        } finally {
//            fileStream.close()
//        }
//    }
//
//
//    fun loadPackedSetsFromStream(inputStream: InputStream): MutableList<SpawnSet> {
//        val sets = mutableListOf<SpawnSet>()
//        val stream = InputStreamReader(inputStream)
//        val jsonArray = gson.fromJson(stream, JsonArray::class.java)
//        stream.close()
//        jsonArray.forEach { jsonElement ->
//            try {
//                sets.add(gson.fromJson<SpawnSet>(jsonElement))
//            } catch (exception: Exception) {
//                exception.printStackTrace()
//            }
//        }
//        return sets
//    }
}
