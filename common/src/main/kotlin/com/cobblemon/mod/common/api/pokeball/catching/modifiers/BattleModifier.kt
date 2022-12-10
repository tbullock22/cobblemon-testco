/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokeball.catching.modifiers

import com.cobblemon.mod.common.api.pokeball.catching.CatchRateModifier
import com.cobblemon.mod.common.battles.ActiveBattlePokemon
import com.cobblemon.mod.common.battles.BattleRegistry
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.entity.LivingEntity
import net.minecraft.server.network.ServerPlayerEntity

/**
 * A [CatchRateModifier] that resolves the catch rate based on an ongoing battle.
 *
 * @property calculator Responsible for resolving the catch rate dynamically based on the given params.
 *
 * @author Licious
 * @since May 7th, 2022
 */
open class BattleModifier(
    private val calculator: (currentCatchRate: Float, player: ServerPlayerEntity, playerPokemon: Iterable<ActiveBattlePokemon>, pokemon: Pokemon) -> Float
) : CatchRateModifier {

    final override fun modifyCatchRate(
        currentCatchRate: Float,
        thrower: LivingEntity,
        pokemon: Pokemon,
        host: Pokemon?
    ): Float {
        val player = thrower as? ServerPlayerEntity ?: return currentCatchRate
        val team = BattleRegistry
            .getBattleByParticipatingPlayer(player)
            ?.actors?.firstOrNull { actor -> actor is PlayerBattleActor && actor.uuid == player.uuid }?.activePokemon
            ?: return currentCatchRate
        return this.modifyCatchRate(currentCatchRate, player, team, pokemon)
    }

    open fun modifyCatchRate(currentCatchRate: Float, player: ServerPlayerEntity, playerPokemon: Iterable<ActiveBattlePokemon>, pokemon: Pokemon): Float = this.calculator.invoke(currentCatchRate, player, playerPokemon, pokemon)

}
