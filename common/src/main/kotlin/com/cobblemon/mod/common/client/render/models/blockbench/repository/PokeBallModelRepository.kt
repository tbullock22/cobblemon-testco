/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.repository

import com.cobblemon.mod.common.api.pokeball.PokeBalls
import com.cobblemon.mod.common.client.render.models.blockbench.BlockBenchModelWrapper
import com.cobblemon.mod.common.client.render.models.blockbench.pokeball.PokeBallModel
import com.cobblemon.mod.common.entity.pokeball.EmptyPokeBallEntity
import com.cobblemon.mod.common.pokeball.PokeBall
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.util.Identifier

object PokeBallModelRepository : ModelRepository<EmptyPokeBallEntity>() {
    private val modelsByPokeBall: MutableMap<PokeBall, BlockBenchModelWrapper<EmptyPokeBallEntity>> = mutableMapOf()
    private val modelTexturesByPokeBall: MutableMap<PokeBall, Identifier> = mutableMapOf()

    override fun registerAll() {
        this.modelsByPokeBall.clear()
        this.modelTexturesByPokeBall.clear()
        val baseModel = BlockBenchModelWrapper(PokeBallModel.LAYER_LOCATION, PokeBallModel::createBodyLayer) { PokeBallModel(it) }
        // ToDo when datapack pokeballs are implemented we will need them to provide these fields instead of our defaults
        PokeBalls.all().forEach { pokeball ->
            registerModel(pokeball, baseModel)
            registerModelTexture(pokeball, Identifier(pokeball.name.namespace, "textures/items/poke_balls/models/${pokeball.name.path}.png"))
        }
    }


    private fun registerModel(pokeBall: PokeBall, model: BlockBenchModelWrapper<EmptyPokeBallEntity>) {
        modelsByPokeBall[pokeBall] = model
        addModel(model)
    }

    private fun registerModelTexture(pokeBall: PokeBall, texture: Identifier) {
        modelTexturesByPokeBall[pokeBall] = texture
    }

    fun getModel(pokeBall: PokeBall): BlockBenchModelWrapper<EmptyPokeBallEntity> {
        return modelsByPokeBall[pokeBall] ?: throw IllegalStateException("pokeball has no appropriate model")
    }

    fun getModelTexture(pokeBall: PokeBall): Identifier {
        return modelTexturesByPokeBall[pokeBall] ?: throw IllegalStateException("pokeball has no appropriate model texture")
    }
}