/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.repository

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.client.render.ModelLayer
import com.cobblemon.mod.common.client.render.ModelVariationSet
import com.cobblemon.mod.common.client.render.VaryingRenderableResolver
import com.cobblemon.mod.common.client.render.models.blockbench.PoseableEntityModel
import com.cobblemon.mod.common.client.render.models.blockbench.PoseableEntityState
import com.cobblemon.mod.common.client.render.models.blockbench.TexturedModel
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.endsWith
import com.cobblemon.mod.common.util.fromJson
import java.io.File
import java.nio.charset.StandardCharsets
import net.minecraft.client.model.ModelPart
import net.minecraft.entity.Entity
import net.minecraft.resource.ResourceManager
import net.minecraft.util.Identifier

abstract class VaryingModelRepository<E : Entity, M : PoseableEntityModel<E>> {
    val posers = mutableMapOf<Identifier, (ModelPart) -> M>()
    val variations = mutableMapOf<Identifier, VaryingRenderableResolver<E, M>>()

    val texturedModels = mutableMapOf<Identifier, TexturedModel>()

    abstract val title: String
    abstract val type: String
    abstract val variationDirectories: List<String>
    abstract val poserDirectories: List<String>
    abstract val modelDirectories: List<String>
    abstract val animationDirectories: List<String>
    abstract val fallback: Identifier

    abstract fun loadJsonPoser(json: String): (ModelPart) -> M

    fun registerPosers(resourceManager: ResourceManager) {
        posers.clear()
        registerInBuiltPosers()
        registerJsonPosers(resourceManager)
    }

    abstract fun registerInBuiltPosers()

    open fun registerJsonPosers(resourceManager: ResourceManager) {
        for (directory in poserDirectories) {
            resourceManager
                .findResources(directory) { path -> path.endsWith(".json") }
                .forEach { (identifier, resource) ->
                    resource.inputStream.use { stream ->
                        val json = String(stream.readAllBytes(), StandardCharsets.UTF_8)
                        val resolvedIdentifier = Identifier(identifier.namespace, File(identifier.path).nameWithoutExtension)
                        posers[resolvedIdentifier] = loadJsonPoser(json)
                    }
                }
        }
    }

    fun inbuilt(name: String, model: (ModelPart) -> M) {
        posers[cobblemonResource(name)] = model
    }

    fun registerVariations(resourceManager: ResourceManager) {
        val nameToModelVariationSets = mutableMapOf<Identifier, MutableList<ModelVariationSet>>()
        for (directory in variationDirectories) {
            resourceManager
                .findResources(directory) { path -> path.endsWith(".json") }
                .forEach { (_, resource) ->
                    resource.inputStream.use { stream ->
                        val json = String(stream.readAllBytes(), StandardCharsets.UTF_8)
                        val modelVariationSet = VaryingRenderableResolver.GSON.fromJson<ModelVariationSet>(json)
                        nameToModelVariationSets.getOrPut(modelVariationSet.name) { mutableListOf() }.add(modelVariationSet)
                    }
                }
        }

        for ((species, speciesVariationSets) in nameToModelVariationSets) {
            val variations = speciesVariationSets.sortedBy { it.order }.flatMap { it.variations }.toMutableList()
            this.variations[species] = VaryingRenderableResolver(species, variations)
        }

        variations.values.forEach { it.initialize(this) }
    }

    fun registerModels(resourceManager: ResourceManager) {
        var models = 0
        for (directory in modelDirectories) {
            resourceManager
                .findResources(directory) { path -> path.endsWith(".geo.json") }
                .forEach { (identifier, resource) ->
                    resource.inputStream.use { stream ->
                        val json = String(stream.readAllBytes(), StandardCharsets.UTF_8)
                        val resolvedIdentifier = Identifier(identifier.namespace, File(identifier.path).nameWithoutExtension)
                        texturedModels[resolvedIdentifier] = TexturedModel.from(json)
                        models++
                    }
                }
        }

        Cobblemon.LOGGER.info("Loaded $models $title models.")
    }

    fun reload(resourceManager: ResourceManager) {
        this.variations.clear()
        this.posers.clear()
        Cobblemon.LOGGER.info("Loading $title models...")
        registerPosers(resourceManager)
        registerModels(resourceManager)
        registerVariations(resourceManager)
    }

    fun getPoser(name: Identifier, aspects: Set<String>): M {
        try {
            val poser = this.variations[name]?.getPoser(aspects)
            if (poser != null) {
                return poser
            }
        } catch(e: IllegalStateException) {
//            e.printStackTrace()
        }
        return this.variations[fallback]!!.getPoser(aspects)
    }

    fun getTexture(name: Identifier, aspects: Set<String>, state: PoseableEntityState<E>?): Identifier {
        try {
            val texture = this.variations[name]?.getTexture(aspects, state?.animationSeconds ?: 0F)
            if (texture != null) {
                return texture
            }
        } catch(_: IllegalStateException) { }
        return this.variations[fallback]!!.getTexture(aspects, state?.animationSeconds ?: 0F)
    }

    fun getLayers(name: Identifier, aspects: Set<String>): Iterable<ModelLayer> {
        try {
            val layers = this.variations[name]?.getLayers(aspects)
            if (layers != null) {
                return layers
            }
        } catch(_: IllegalStateException) { }
        return this.variations[fallback]!!.getLayers(aspects)
    }
}