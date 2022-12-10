/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokeball.catching.calculators

import com.cobblemon.mod.common.api.pokeball.catching.CaptureContext
import com.cobblemon.mod.common.pokeball.PokeBall
import com.cobblemon.mod.common.pokemon.Pokemon
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random
import net.minecraft.entity.LivingEntity

object CobblemonGen348CaptureCalculator : CaptureCalculator {

    override fun processCapture(thrower: LivingEntity, pokeBall: PokeBall, target: Pokemon, host: Pokemon?): CaptureContext {
        val catchRate = getCatchRate(thrower, pokeBall, target, host)
        return if (tryCriticalCapture(catchRate, thrower)) {
            CaptureContext(isSuccessfulCapture = true, isCriticalCapture = true, numberOfShakes = 1)
        } else {
            val shakeProbability = (65536 / (255 / catchRate.toDouble()).pow(3.0 / 16))

            var numShakes = 0
            for (i in 0..3) {
                if (Random.nextInt(65536) >= shakeProbability) {
                    break
                }
                numShakes++
            }

            CaptureContext(isSuccessfulCapture = numShakes == 4, isCriticalCapture = false, numberOfShakes = numShakes)
        }
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
     * * The pokemon's maximum HP at its current level
     * * The pokemon's current HP
     * * The base catch rate of the pokemon
     * * Modifiers which directly affect the catch rate
     * * A status inflicted on the pokemon
     *
     * These values are used in accordance with the Gen 3/4 modified catch rate formula, and are then
     * additionally ran through difficulty modifiers introduced in generation 8. These modifiers can be
     * described as the following:
     * * L: Based on the level of the target wild pokemon
     * * D: A difficulty factor, modified to scale with your own pokemon's level, which is directly affected
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
    private fun getCatchRate(thrower: LivingEntity, pokeBall: PokeBall, target: Pokemon, host: Pokemon?): Float {
        var catchRate = target.species.catchRate.toFloat()
        pokeBall.catchRateModifiers.forEach { catchRate = it.modifyCatchRate(catchRate, thrower, target, host) }

        val guaranteed = pokeBall.catchRateModifiers.any { it.isGuaranteed() }
        val statusBonus = this.getStatusBonus(target)

        val base = (((3 * target.hp - 2 * pokeBall.hpForCalculation(target)) * catchRate) / (3 * target.hp)) * statusBonus
        val l = max(1, (30 - target.level) / 10)

        val min = 33.0f/100.0f
        val d = if(host != null) {
            min(1.0f, max(min, host.level.toFloat() / target.level.toFloat()))
        } else {
            min
        }

        return if (guaranteed) 255.0f else (base * l * d)
    }

    private fun tryCriticalCapture(catchRate: Float, thrower: LivingEntity): Boolean {
        val critCaptureRate = (minOf(255f, catchRate) * getCriticalCaptureMultiplier(thrower) / 6).toInt()
        return Random.nextInt(256) < critCaptureRate
    }

    private fun getCriticalCaptureMultiplier(thrower: LivingEntity): Float {
        // TODO: Get pokedex, determine modifier based on how many pokemon player has caught
        return 0f
    }

    private fun getStatusBonus(pokemon: Pokemon): Float {
        // TODO: Get status from pokemon and get bonus (2 for sleep and freeze, 1.5 for paralyze, poison, or burn, and 1 otherwise).
        return 1f
    }
}