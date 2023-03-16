/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.config.starter

import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.config.Category
import com.cobblemon.mod.common.config.NodeCategory
import com.cobblemon.mod.common.util.adapters.pokemonPropertiesShortAdapter
import com.google.gson.GsonBuilder
class StarterConfig {
    companion object {
        val GSON = GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .registerTypeAdapter(PokemonProperties::class.java, pokemonPropertiesShortAdapter)
            .create()
    }

    @NodeCategory(Category.Starter)
    var allowStarterOnJoin = true

    @NodeCategory(Category.Starter)
    var promptStarterOnceOnly = false

    @NodeCategory(Category.Starter)
    var starters = mutableListOf(
        StarterCategory(
            name = "Kanto",
            displayName = "cobblemon.starterselection.category.kanto",
            pokemon = mutableListOf(
                PokemonProperties.parse("Bulbasaur level=10"),
                PokemonProperties.parse("Charmander level=10"),
                PokemonProperties.parse("Squirtle level=10"),
            )
        ),
        StarterCategory(
                name = "Hoenn",
                displayName = "cobblemon.starterselection.category.hoenn",
                pokemon = mutableListOf(
                        PokemonProperties.parse("Treecko level=10"),
                        PokemonProperties.parse("Torchic level=10"),
                        PokemonProperties.parse("Mudkip level=10"),
                )
        ),
        StarterCategory(
            name = "Sinnoh",
            displayName = "cobblemon.starterselection.category.sinnoh",
            pokemon = mutableListOf(
                PokemonProperties.parse("Turtwig level=10"),
                PokemonProperties.parse("Chimchar level=10"),
                PokemonProperties.parse("Piplup level=10"),
            )
        ),
        StarterCategory(
            name = "Alola",
            displayName = "cobblemon.starterselection.category.alola",
            pokemon = mutableListOf(
                PokemonProperties.parse("Rowlet level=10"),
                PokemonProperties.parse("Litten level=10"),
                PokemonProperties.parse("Popplio level=10"),
            )
        )
    //,
//            StarterCategory(
//                name = "Johto",
//                displayName = lang("starterselection.category.johto"),
//                pokemon = mutableListOf(
//                    PokemonProperties().also { it.level = 5 ; it.species = "Chikorita" },
//                    PokemonProperties().also { it.level = 5 ; it.species = "Cyndaquil" },
//                    PokemonProperties().also { it.level = 5 ; it.species = "Totodile" }
//                )
//            )
    )
}