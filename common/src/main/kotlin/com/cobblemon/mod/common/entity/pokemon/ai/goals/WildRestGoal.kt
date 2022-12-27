/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.ai.goals

import com.cobblemon.mod.common.api.pokemon.status.Statuses
import com.cobblemon.mod.common.entity.pokemon.PokemonBehaviourFlag.EXCITED
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.status.PersistentStatusContainer
import java.util.EnumSet
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.util.registry.Registry

/**
 * AI goal for sleeping in the wild.
 *
 * @author Hiroku
 * @since July 18th, 2022
 */
class WildRestGoal(private val pokemonEntity: PokemonEntity) : Goal() {
    override fun getControls(): EnumSet<Control> = EnumSet.allOf(Control::class.java)
    override fun canStart(): Boolean {
        val rest = pokemonEntity.behaviour.resting

        if (!pokemonEntity.pokemon.isWild() || pokemonEntity.random.nextFloat() < 1 - rest.sleepChance || !canSleep() || pokemonEntity.isBusy || rest.depth.canSleep(pokemonEntity)) {
            return false
        }

        return true
    }

    private fun canSleep(): Boolean {
        val rest = pokemonEntity.behaviour.resting
        val worldTime = (pokemonEntity.world.timeOfDay % 24000).toInt()
        val light = pokemonEntity.world.getLightLevel(pokemonEntity.blockPos)
        val block = pokemonEntity.world.getBlockState(pokemonEntity.blockPos).block
        val biome = pokemonEntity.world.getBiome(pokemonEntity.blockPos).value()

        return rest.canSleep &&
                !pokemonEntity.getBehaviourFlag(EXCITED) &&
                worldTime in pokemonEntity.behaviour.resting.times &&
                light in rest.light &&
                (rest.blocks.isEmpty() || rest.blocks.any { it.fits(block, pokemonEntity.world.registryManager.get(Registry.BLOCK_KEY)) }) &&
                (rest.biomes.isEmpty() || rest.biomes.any { it.fits(biome, pokemonEntity.world.registryManager.get(Registry.BIOME_KEY)) })
    }

    override fun shouldContinue(): Boolean {
        return if (canSleep() && !pokemonEntity.behaviour.resting.depth.shouldWake(pokemonEntity)) {
            true
        } else {
            wake()
            false
        }
    }

    override fun start() {
        pokemonEntity.pokemon.status = PersistentStatusContainer(Statuses.SLEEP)
    }

    fun wake() {
        if (pokemonEntity.battleId.get().isEmpty) {
            pokemonEntity.pokemon.status = null
        }
    }
}