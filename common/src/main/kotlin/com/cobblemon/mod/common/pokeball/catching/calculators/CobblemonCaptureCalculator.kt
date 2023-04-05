/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokeball.catching.calculators

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.pokeball.catching.CaptureContext
import com.cobblemon.mod.common.api.pokeball.catching.calculators.CaptureCalculator
import com.cobblemon.mod.common.api.pokeball.catching.calculators.CriticalCaptureProvider
import com.cobblemon.mod.common.api.pokeball.catching.calculators.PokedexProgressCaptureMultiplierProvider
import com.cobblemon.mod.common.battles.BattleRegistry
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor
import com.cobblemon.mod.common.pokeball.PokeBall
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.status.statuses.*
import net.minecraft.entity.LivingEntity
import net.minecraft.server.network.ServerPlayerEntity
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random

object CobblemonCaptureCalculator: CaptureCalculator, CriticalCaptureProvider, PokedexProgressCaptureMultiplierProvider {

    override fun id(): String = "cobblemon"

    /**
     * Calculates catch rates based on the following mechanics:
     *
     * Gen 8: https://bulbapedia.bulbagarden.net/wiki/Catch_rate#Capture_method_.28Generation_VIII.29
     * Gen 9: https://bulbapedia.bulbagarden.net/wiki/Catch_rate#Capture_method_.28Generation_IX.30
     *
     * This is an almost exact copy of how Generation 9 handles capture mechanics, with a change to the difficulty
     * modifier copied over from Gen 8.
     *
     * The difficulty modifier makes it so that every level higher the wild Pokémon is than the highest level Pokémon
     * in your party, you will receive a 2% reduction in your overall catch rate of that Pokémon. For example,
     * if your highest level is 15 and the wild is 20 you will be hit with a 10% reduction. This reduction maxes out at
     * 90%. This 90% reduction would take place at a 45 level difference or higher.
     */
    override fun processCapture(thrower: LivingEntity, pokeBall: PokeBall, target: Pokemon): CaptureContext {
//        return Gen3And4CaptureCalculator.processCapture(thrower, pokeBall, target)
        if (pokeBall.catchRateModifier.isGuaranteed()) {
            return CaptureContext.successful()
        }
        // We don't have dark grass so we're just gonna pretend everything is that. Scratch that, without the pokedex it has issues.
        val darkGrass = 1F //if (thrower is ServerPlayerEntity) this.caughtMultiplierFor(thrower).roundToInt() else 1
        val inBattleModifier = if (target.entity?.battleId?.get()?.isPresent == true) 1F else 0.5F
        val catchRate = target.form.catchRate.toFloat()
        val validModifier = pokeBall.catchRateModifier.isValid(thrower, target)
        val bonusStatus = when (target.status?.status) {
            is SleepStatus, is FrozenStatus -> 2.5F
            is ParalysisStatus, is BurnStatus, is PoisonStatus, is PoisonBadlyStatus -> 1.5F
            else -> 1F
        }
        val bonusLevel = if (target.level < 13) max((36 - (2 * target.level)) / 10, 1) else 1
        // ToDo implement badgePenalty when we have a system for obedience
        // ToDo implement bonusMisc when we have sandwich powers
        val ballBonus = if (validModifier) pokeBall.catchRateModifier.value(thrower, target).roundToInt().coerceAtLeast(1) else 1
        var modifiedCatchRate = pokeBall.catchRateModifier
            .behavior(thrower, target)
            .mutator((3F * target.hp - 2F * target.currentHealth) * darkGrass * catchRate * inBattleModifier, ballBonus.toFloat()) / (3F * target.hp)
        modifiedCatchRate *= bonusStatus * bonusLevel
        if (thrower is ServerPlayerEntity) {
            val highestLevelThrower = this.findHighestThrowerLevel(thrower, target)
            if (highestLevelThrower != null && highestLevelThrower < target.level) {
                val config = Cobblemon.config
                modifiedCatchRate *= max(0.1F, min(1F, 1F - ((target.level - highestLevelThrower) / (config.maxPokemonLevel / 2))))
            }
        }
        val critical = if (thrower is ServerPlayerEntity) this.shouldHaveCriticalCapture(thrower, modifiedCatchRate) else false
        val shakeProbability = (65536F / (255F / modifiedCatchRate).pow(0.1875F)).roundToInt()
        var shakes = 0
        repeat(4) {
            val n = Random.nextInt(65537)
            if (n < shakeProbability) {
                shakes++
            }
            if (it == 0 && critical) {
                return CaptureContext(numberOfShakes = 1, isSuccessfulCapture = shakes == 1, isCriticalCapture = true)
            }
        }
        // ToDo once pokedex is implemented if the target is registered and it's a success shorten to 1 shake and become critical capture
        return CaptureContext(numberOfShakes = shakes, isSuccessfulCapture = shakes == 4, isCriticalCapture = false)
    }

    private fun findHighestThrowerLevel(player: ServerPlayerEntity, pokemon: Pokemon): Int? {
        val entity = pokemon.entity ?: return null
        val battleId = entity.battleId.get().orElse(null) ?: return null
        val battle = BattleRegistry.getBattle(battleId) ?: return null
        val actor = battle.actors.firstOrNull { actor ->
            actor is PlayerBattleActor && player.uuid == actor.uuid && actor.activePokemon.any { active ->
                active.battlePokemon?.effectedPokemon?.uuid == pokemon.uuid
            }
        } ?: return null
        return actor.getSide().getOppositeSide().activePokemon.maxOfOrNull { it.battlePokemon?.effectedPokemon?.level ?: 1 }
    }
}