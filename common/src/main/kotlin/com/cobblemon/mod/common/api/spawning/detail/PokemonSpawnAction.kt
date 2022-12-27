/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.detail

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.spawning.context.SpawningContext
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.feature.SeasonFeatureHandler
import kotlin.random.Random

/**
 * A [SpawnAction] that will spawn a single [PokemonEntity].
 *
 * @author Hiroku
 * @since February 13th, 2022
 */
class PokemonSpawnAction(
    ctx: SpawningContext,
    override val detail: PokemonSpawnDetail,
    /** The [PokemonProperties] that are about to be used. */
    var props: PokemonProperties = detail.pokemon.copy()
) : SpawnAction<PokemonEntity>(ctx, detail) {
    override fun createEntity(): PokemonEntity {
        if (props.level == null) {
            props.level = detail.getDerivedLevelRange().random()
        }
        if (props.shiny == null) {
            // If the config value is at least 1, then do 1/x and use that as the shiny chance
            props.shiny = Cobblemon.config.shinyRate.takeIf { it >= 1 }?.let { Random.Default.nextFloat() < 1 / it }
        }
        val entity = props.createEntity(ctx.world)
        SeasonFeatureHandler.updateSeason(entity.pokemon, ctx.world, ctx.position)
        entity.drops = detail.drops
        return entity
    }
}