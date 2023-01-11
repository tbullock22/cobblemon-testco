/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.battle

import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.pokemon.stats.Stat
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonFloatingState
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.pokemon.status.PersistentStatus
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import java.util.UUID
import net.minecraft.text.MutableText
class ClientBattlePokemon(
    val uuid: UUID,
    var displayName: MutableText,
    var properties: PokemonProperties,
    var aspects: Set<String>,
    var hpRatio: Float,
    var status: PersistentStatus?,
    var statChanges: MutableMap<Stat, Int>
) {
    lateinit var actor: ClientBattleActor
    val species: Species
        get() = PokemonSpecies.getByIdentifier(properties.species!!.asIdentifierDefaultingNamespace())!!
    val level: Int
        get() = properties.level ?: 0

    val gender: Gender
        get() = properties.gender ?: Gender.GENDERLESS

    var state = PokemonFloatingState()
}