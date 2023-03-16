/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.repository

import com.cobblemon.mod.common.api.pokeball.PokeBalls
import com.cobblemon.mod.common.client.render.models.blockbench.pokeball.PokeBallModel
import com.cobblemon.mod.common.entity.pokeball.EmptyPokeBallEntity
import net.minecraft.client.model.ModelPart

object PokeBallModelRepository : VaryingModelRepository<EmptyPokeBallEntity, PokeBallModel>() {
    override val title = "Poké Ball"
    override val type = "poke_balls"
    override val variationDirectories: List<String> = listOf("bedrock/$type/variations")
    override val poserDirectories: List<String> = listOf("bedrock/$type/posers")
    override val modelDirectories: List<String> = listOf("bedrock/$type/models")
    override val animationDirectories: List<String> = listOf("bedrock/$type/animations")

    override val fallback = PokeBalls.POKE_BALL.name

    override fun loadJsonPoser(json: String): (ModelPart) -> PokeBallModel {
        TODO("Not yet implemented")
    }

    override fun registerInBuiltPosers() {
        inbuilt("azure_ball", ::PokeBallModel)
        inbuilt("beast_ball", ::PokeBallModel)
        inbuilt("cherish_ball", ::PokeBallModel)
        inbuilt("citrine_ball", ::PokeBallModel)
        inbuilt("dive_ball", ::PokeBallModel)
        inbuilt("dream_ball", ::PokeBallModel)
        inbuilt("dusk_ball", ::PokeBallModel)
        inbuilt("fast_ball", ::PokeBallModel)
        inbuilt("friend_ball", ::PokeBallModel)
        inbuilt("great_ball", ::PokeBallModel)
        inbuilt("heal_ball", ::PokeBallModel)
        inbuilt("heavy_ball", ::PokeBallModel)
        inbuilt("level_ball", ::PokeBallModel)
        inbuilt("love_ball", ::PokeBallModel)
        inbuilt("lure_ball", ::PokeBallModel)
        inbuilt("luxury_ball", ::PokeBallModel)
        inbuilt("master_ball", ::PokeBallModel)
        inbuilt("moon_ball", ::PokeBallModel)
        inbuilt("nest_ball", ::PokeBallModel)
        inbuilt("net_ball", ::PokeBallModel)
        inbuilt("park_ball", ::PokeBallModel)
        inbuilt("poke_ball", ::PokeBallModel)
        inbuilt("premier_ball", ::PokeBallModel)
        inbuilt("quick_ball", ::PokeBallModel)
        inbuilt("repeat_ball", ::PokeBallModel)
        inbuilt("roseate_ball", ::PokeBallModel)
        inbuilt("safari_ball", ::PokeBallModel)
        inbuilt("slate_ball", ::PokeBallModel)
        inbuilt("sport_ball", ::PokeBallModel)
        inbuilt("strange_ball", ::PokeBallModel)
        inbuilt("timer_ball", ::PokeBallModel)
        inbuilt("ultra_ball", ::PokeBallModel)
        inbuilt("verdant_ball", ::PokeBallModel)
    }
}