/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.evolution.requirements

import com.cobblemon.mod.common.api.pokemon.evolution.requirement.EvolutionRequirement
import com.cobblemon.mod.common.battles.BattleRegistry
import com.cobblemon.mod.common.pokemon.Pokemon
import kotlin.jvm.optionals.getOrNull

/**
 * An [EvolutionRequirement] for a certain amount of critical hits in a single battle.
 *
 * @param amount The amount of critical hits required.
 *
 * @author Licious
 * @since October 2nd, 2022
 */
class BattleCriticalHitsRequirement(amount: Int) : EvolutionRequirement {

    constructor() : this(0)

    /**
     * The amount of critical hits required.
     */
    val amount = amount

    @OptIn(ExperimentalStdlibApi::class)
    override fun check(pokemon: Pokemon): Boolean {
        val pokemonEntity = pokemon.entity ?: return false
        val battleId = pokemonEntity.battleId.get().getOrNull() ?: return false
        val battle = BattleRegistry.getBattle(battleId) ?: return false
        battle.actors.forEach { actor ->
            actor.pokemonList.forEach { battlePokemon ->
                if (battlePokemon.effectedPokemon.uuid == pokemon.uuid) {
                    return battlePokemon.criticalHits >= this.amount
                }
            }
        }
        return false
    }

    companion object {
        const val ADAPTER_VARIANT = "battle_critical_hits"
    }

}