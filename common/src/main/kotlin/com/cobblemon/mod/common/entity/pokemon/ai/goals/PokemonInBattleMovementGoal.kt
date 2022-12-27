/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.ai.goals

import com.cobblemon.mod.common.battles.BattleRegistry
import com.cobblemon.mod.common.battles.BattleSide
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import dev.architectury.event.events.common.TickEvent.Player
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.TargetPredicate
import net.minecraft.entity.ai.goal.Goal

class PokemonInBattleMovementGoal(val entity: PokemonEntity, val range: Int) : Goal() {
    override fun canStart(): Boolean {
        return entity.isBattling && getClosestPokemonEntity() != null
    }

    private fun getClosestPokemonEntity(): PokemonEntity? {
        entity.battleId.get().orElse(null)?.let { BattleRegistry.getBattle(it) }?.let { battle ->
            return battle.sides.find { it -> it.activePokemon.any { it.battlePokemon?.effectedPokemon == entity.pokemon } }?.
            getOppositeSide()?.activePokemon?.mapNotNull { it.battlePokemon?.entity }?.minByOrNull { it.distanceTo(entity) }
        }
        return null
    }

    override fun tick() {
        val closestPokemonEntity = getClosestPokemonEntity()
        if (closestPokemonEntity != null) {
            entity.navigation.stop()
            entity.lookAtEntity(closestPokemonEntity, 30.0f, 30.0f)
            entity.lookControl.lookAt(closestPokemonEntity.x, closestPokemonEntity.y, closestPokemonEntity.z)
        }
    }
}