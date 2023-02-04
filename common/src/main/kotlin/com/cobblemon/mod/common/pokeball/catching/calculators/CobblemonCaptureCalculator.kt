/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokeball.catching.calculators

import com.cobblemon.mod.common.api.pokeball.catching.CaptureContext
import com.cobblemon.mod.common.api.pokeball.catching.calculators.CaptureCalculator
import com.cobblemon.mod.common.api.pokeball.catching.calculators.CriticalCaptureProvider
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

object CobblemonCaptureCalculator : CaptureCalculator, CriticalCaptureProvider {

    override fun id(): String = "cobblemon"

    override fun processCapture(thrower: LivingEntity, pokeBall: PokeBall, target: Pokemon): CaptureContext {
        if (pokeBall.catchRateModifier.isGuaranteed()) {
            return CaptureContext.successful()
        }
        val modifiedCatchRate = this.getCatchRate(thrower, pokeBall, target)
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

    /**
     * Calculates catch rates based on the following mechanics:
     *
     * Gen 3/4: https://bulbapedia.bulbagarden.net/wiki/Catch_rate#Capture_method_.28Generation_III-IV.29
     * Gen 8: https://bulbapedia.bulbagarden.net/wiki/Catch_rate#Capture_method_.28Generation_VIII.29
     *
     * Due to pokemon making pokemon captures on Gen 5 and higher much easier, this implementation
     * calculates catch rate in a modified manner to increase difficulty. First, it calculates the base capture
     * rate via the Gen 3/4 mechanics. This involves the following variables:
     * - The pokemon's maximum HP at its current level
     * - The pokemon's current HP
     * - The base catch rate of the pokemon
     * - Modifiers which directly affect the catch rate
     * - A status inflicted on the pokemon
     *
     * These values are used in accordance with the Gen 3/4 modified catch rate formula, and are then
     * additionally ran through difficulty modifiers introduced in generation 8. These modifiers can be
     * described as the following:
     * - bonusLevel: Based on the level of the target wild pokemon, this value only applies to Pokémon bellow level 20.
     * - difficulty: A difficulty factor, modified to scale with your own pokemon's level, which is directly affected
     * by the level of the wild pokemon against your own.
     *
     * Normally, the difficulty factor simply checks if the level of your pokemon is less than the target wild
     * pokemon. If so, your capture rate is immediately reduced by 90%, making it far more difficult for a
     * pokeball to succeed in capture. In this implementation, the mechanic scales to your level, so the closer
     * your pokemon is to the opponent's level, the better the odds. So, if the target pokemon is level 100 and
     * your pokemon is level 95, you'd only face a 10% reduction. Now, in the event your pokemon is level 5 and
     * the target is level 10, you'd have a 50% reduction in catch rate potential. This should see a better
     * difficulty factor for users who simply start and catch the highest level pokemon they can find immediately,
     * effectively skipping the entire starting phase.
     *
     * In the event you throw a pokeball out of battle, the difficulty factor will automatically default to the base
     * 90% reduction.
     *
     * Finally, if your pokemon has a level higher than that of the target, a maximum multiplier of 1 will be applied,
     * which simply forces this modifier to act as a no-op.
     */
    private fun getCatchRate(thrower: LivingEntity, pokeBall: PokeBall, target: Pokemon): Float {
        val catchRate = target.form.catchRate.toFloat()
        val validModifier = pokeBall.catchRateModifier.isValid(thrower, target)
        val bonusStatus = when (target.status?.status) {
            is SleepStatus, is FrozenStatus -> 2F
            is ParalysisStatus, is BurnStatus, is PoisonStatus, is PoisonBadlyStatus -> 1.5F
            else -> 1F
        }
        val ballBonus: Float = if (validModifier) pokeBall.catchRateModifier.value(thrower, target) else 1F
        var modifiedCatchRate = (pokeBall.catchRateModifier.behavior(thrower, target).mutator((3F * target.hp - 2F * target.currentHealth) * catchRate, ballBonus) / 3F * target.hp) * bonusStatus
        val bonusLevel = max((30 - target.level) / 10, 1)
        modifiedCatchRate *= bonusLevel
        if (thrower is ServerPlayerEntity) {
            val highestLevelThrower = this.findHighestThrowerLevel(thrower, target) ?: return modifiedCatchRate
            val difficulty = min(1F, max(33F / 100F, highestLevelThrower.toFloat() / target.level))
            modifiedCatchRate *= difficulty
        }
        return modifiedCatchRate
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