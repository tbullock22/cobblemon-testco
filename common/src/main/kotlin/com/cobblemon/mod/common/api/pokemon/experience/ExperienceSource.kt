/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.experience

import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import net.minecraft.command.CommandSource
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity

/**
 * A source of experience gain for a Pokémon. This could be a battle, a command, something like a level-up item, etc.
 *
 * @author Hiroku
 * @since August 5th, 2022
 */
interface ExperienceSource {
    fun isBattle() = this is BattleExperienceSource
    fun isInteraction() = this is CandyExperienceSource
    fun isCommand() = this is CommandExperienceSource
    fun isSidemod() = this is SidemodExperienceSource
}

open class SidemodExperienceSource(
    val sidemodId: String
) : ExperienceSource

open class CandyExperienceSource(
    val player: ServerPlayerEntity,
    val stack: ItemStack
) : ExperienceSource

open class CommandExperienceSource(
    val source: CommandSource
) : ExperienceSource

open class BattleExperienceSource(
    val battle: PokemonBattle,
    val facedPokemon: List<BattlePokemon>
) : ExperienceSource