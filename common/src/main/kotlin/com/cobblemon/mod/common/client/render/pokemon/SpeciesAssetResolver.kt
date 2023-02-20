/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.pokemon

import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.client.render.models.blockbench.repository.PokemonModelRepository
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.util.adapters.IdentifierAdapter
import com.cobblemon.mod.common.util.adapters.ModelTextureSupplierAdapter
import com.cobblemon.mod.common.util.adapters.Vec3fAdapter
import com.cobblemon.mod.common.util.adapters.Vector4fAdapter
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.gson.GsonBuilder
import kotlin.math.floor
import net.minecraft.client.model.ModelPart
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3f
import net.minecraft.util.math.Vector4f

/**
 * All the information required for rendering a Pokémon [Species] with aspects.
 *
 * @author Hiroku
 * @since May 14th, 2022
 */
class RegisteredSpeciesRendering(
    val species: Identifier,
    val variations: MutableList<ModelAssetVariation>
) {
    fun getResolvedPoser(aspects: Set<String>): Identifier {
        return getVariationValue(aspects) { poser }
            ?: throw IllegalStateException("Unable to find a poser for $species with aspects ${aspects.joinToString()}. This shouldn't be possible if you've defined the fallback variation.")
    }

    fun getResolvedModel(aspects: Set<String>): Identifier {
        return getVariationValue(aspects) { model }
            ?: throw IllegalStateException("Unable to find a model for $species with aspects ${aspects.joinToString()}. This shouldn't be possible if you've defined the fallback variation.")
    }

    fun getResolvedTexture(aspects: Set<String>, animationSeconds: Float): Identifier {
        return getVariationValue(aspects) { texture }?.invoke(animationSeconds)
            ?: throw IllegalStateException("Unable to find a texture for $species with aspects ${aspects.joinToString()}. This shouldn't be possible if you've defined the fallback variation.")
    }

    private fun <T> getVariationValue(aspects: Set<String>, selector: ModelAssetVariation.() -> T?): T? {
        return variations.lastOrNull { it.aspects.all { it in aspects } && selector(it) != null }?.let(selector)
    }

    fun getResolvedLayers(aspects: Set<String>): Iterable<ModelLayer> {
        val layerMaps = mutableMapOf<String, ModelLayer>()
        for (variation in variations) {
            val layers = variation.layers
            if (layers != null && variation.aspects.all { it in aspects }) {
                for (layer in layers) {
                    layerMaps[layer.name] = layer
                }
            }
        }
        return layerMaps.values
    }

    fun getAllModels(): Set<Identifier> {
        val models = mutableSetOf<Identifier>()
        for (variation in variations) {
            if (variation.model != null) {
                models.add(variation.model)
            }
        }
        return models
    }

    companion object {
        val GSON = GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(Identifier::class.java, IdentifierAdapter)
            .registerTypeAdapter(Vec3f::class.java, Vec3fAdapter)
            .registerTypeAdapter(Vector4f::class.java, Vector4fAdapter)
            .registerTypeAdapter(ModelTextureSupplier::class.java, ModelTextureSupplierAdapter)
            .disableHtmlEscaping()
            .setLenient()
            .create()
    }

    val posers = mutableMapOf<Pair<Identifier, Identifier>, PokemonPoseableModel>()
    val models = mutableMapOf<Identifier, ModelPart>()

    fun initialize() {
        posers.clear()
        getAllModels().forEach { identifier ->
            models[identifier] = PokemonModelRepository.texturedModels[identifier]!!.create().createModel()
        }
    }

    fun getPoser(aspects: Set<String>): PokemonPoseableModel {
        val poserName = getResolvedPoser(aspects)
        val poserSupplier = PokemonModelRepository.posers[poserName] ?: throw IllegalStateException("No poser found for name: $poserName")
        val modelName = getResolvedModel(aspects)
        val existingEntityModel = posers[poserName to modelName]
        return if (existingEntityModel != null) {
            existingEntityModel
        } else {
            val entityModel = poserSupplier(models[modelName]!!)
            entityModel.initializeLocatorAccess()
            entityModel.registerPoses()
            posers[poserName to modelName] = entityModel
            entityModel
        }
    }

    fun getTexture(aspects: Set<String>, animationSeconds: Float): Identifier {
        PokemonModelRepository.posers[getResolvedPoser(aspects)] ?: throw IllegalStateException("No poser for $species")
        return getResolvedTexture(aspects, animationSeconds)
    }

    fun getLayers(aspects: Set<String>): Iterable<ModelLayer> {
        PokemonModelRepository.posers[getResolvedPoser(aspects)] ?: throw IllegalStateException("No poser for $species")
        return getResolvedLayers(aspects)
    }
}

/**
 * A set of species variations. This is essentially a prioritized list of [ModelAssetVariation]s for a species, with
 * an [order] property to control the priority of this set compared to other sets.
 *
 * @author Hiroku
 * @since December 4th, 2022
 */
class SpeciesVariationSet(
    val species: Identifier = cobblemonResource("pokemon"),
    val order: Int = 0,
    val variations: MutableList<ModelAssetVariation> = mutableListOf()
)


/**
 * A variation to the base species, which can overwrite the poser, model, texture, or any combination of the above.
 * It contains a set of aspects that must ALL be present on a renderable for this variation to be considered.
 * If a later variation also matches, but provides different properties, both this and the other variation will
 * be used for their respective non-null properties.
 *
 * @author Hiroku
 * @since May 14th, 2022
 */
class ModelAssetVariation(
    val aspects: MutableSet<String> = mutableSetOf(),
    val poser: Identifier? = null,
    val model: Identifier? = null,
    val texture: ModelTextureSupplier? = null,
    val layers: List<ModelLayer>? = null
)

/**
 * Given the animation seconds, returns a texture to use. Only implemented
 * by [StaticModelTextureSupplier] and [AnimatedModelTextureSupplier].
 *
 * @author Hiroku
 * @since February 6th, 2023
 */
fun interface ModelTextureSupplier {
    operator fun invoke(animationSeconds: Float): Identifier
}

class StaticModelTextureSupplier(val texture: Identifier): ModelTextureSupplier {
    override fun invoke(animationSeconds: Float): Identifier {
        return texture
    }
}

class AnimatedModelTextureSupplier(
    val loop: Boolean,
    val fps: Float,
    val frames: List<Identifier>
): ModelTextureSupplier {
    override fun invoke(animationSeconds: Float): Identifier {
        val frameIndex = floor(animationSeconds * fps).toInt()
        if (frameIndex >= frames.size && !loop) {
            return frames.last()
        }
        return frames[frameIndex % frames.size]
    }
}

class ModelLayer {
    val name: String = ""
    val tint: Vector4f = Vector4f(1F, 1F, 1F, 1F)
    val texture: ModelTextureSupplier? = null
    val emissive: Boolean = false
    val translucent: Boolean = false
}

/*
{
  "poser": "cobblemon:squirtle",
  "model": "cobblemon:bedrock/models/squirtle.geo.json",
  "texture": "cobblemon:textures/pokemon/squirtle.png",
  "variations": [
    {
      "aspects": ["shiny"],
      "texture": "cobblemon:textures/pokemon/squirtle_shiny.png"
    },
    {
      "aspects": ["sunglasses"],
      "layers": [
        {
          "name": "sunnies",
          "texture": "cobblemon:textures/pokemon/squirtle_sunglasses.png",
          "scale": [1.05, 1, 1.05],
          "tint": [1, 0.1, 0.1, 0.6]
        }
      ]
    }
  ]
}
 */