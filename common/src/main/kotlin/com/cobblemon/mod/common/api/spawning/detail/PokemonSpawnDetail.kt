/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.detail

import com.cobblemon.mod.common.Cobblemon.config
import com.cobblemon.mod.common.api.drop.DropTable
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.spawning.context.SpawningContext
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import com.cobblemon.mod.common.util.lang
import com.google.gson.annotations.SerializedName
import net.minecraft.text.MutableText

/**
 * A [SpawnDetail] for spawning a [PokemonEntity].
 *
 * @author Hiroku
 * @since February 13th, 2022
 */
class PokemonSpawnDetail : SpawnDetail() {
    companion object {
        val TYPE = "pokemon"
    }

    override val type: String = TYPE
    var pokemon = PokemonProperties()
    @SerializedName("level", alternate = ["levelRange"])
    var levelRange: IntRange? = null
    val drops: DropTable? = null
    val heldItems: MutableList<PossibleHeldItem>? = null

    /* todo breadcrumbing, ai */


    override fun getName(): MutableText {
        val speciesString = pokemon.species
        if (speciesString != null) {
            if (speciesString.lowercase() == "random") {
                return lang("species.random")
            }
            // ToDo exception handling
            val species = PokemonSpecies.getByIdentifier(speciesString.asIdentifierDefaultingNamespace())
            return if (species == null) {
                lang("species.unknown")
            } else {
                species.translatedName
            }
        } else {
            return lang("a_pokemon")
        }
    }

    override fun autoLabel() {
        super.autoLabel()
        if (pokemon.species != null) {
            val species = PokemonSpecies.getByIdentifier(pokemon.species!!.asIdentifierDefaultingNamespace())
            if (species != null) {
                labels.addAll(
                    species.secondaryType?.let { listOf(species.primaryType.name.lowercase(), it.name.lowercase()) }
                    ?: listOf(species.primaryType.name.lowercase())
                )
            }
        }
    }

    fun getDerivedLevelRange() = levelRange.let { levelRange ->
        if (levelRange == null && pokemon.level == null) {
            IntRange(1, config.maxPokemonLevel)
        } else if (levelRange == null) {
            IntRange(pokemon.level!!, pokemon.level!!)
        } else {
            levelRange
        }
    }

    override fun doSpawn(ctx: SpawningContext): SpawnAction<*> {
        // TODO should do more maybe
        return PokemonSpawnAction(ctx, this)
    }
}