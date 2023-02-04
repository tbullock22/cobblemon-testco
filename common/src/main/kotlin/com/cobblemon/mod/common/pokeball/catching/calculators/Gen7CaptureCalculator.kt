/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokeball.catching.calculators

import com.cobblemon.mod.common.api.pokeball.PokeBalls
import com.cobblemon.mod.common.api.pokeball.catching.CaptureContext
import com.cobblemon.mod.common.api.pokeball.catching.calculators.CaptureCalculator
import com.cobblemon.mod.common.api.pokeball.catching.calculators.CriticalCaptureProvider
import com.cobblemon.mod.common.api.pokeball.catching.calculators.PokedexProgressCaptureMultiplierProvider
import com.cobblemon.mod.common.pokeball.PokeBall
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.status.statuses.*
import net.minecraft.entity.LivingEntity
import net.minecraft.server.network.ServerPlayerEntity
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * An implementation of the capture calculator used in the generation 7 games.
 * For more information see the [Bulbapedia](https://bulbapedia.bulbagarden.net/wiki/Catch_rate#Capture_method_.28Generation_VII.29) page.
 *
 * @author Licious
 * @since January 29th, 2022
 */
object Gen7CaptureCalculator : CaptureCalculator, CriticalCaptureProvider, PokedexProgressCaptureMultiplierProvider {

    override fun id(): String = "generation_7"

    override fun processCapture(thrower: LivingEntity, pokeBall: PokeBall, target: Pokemon): CaptureContext {
        if (pokeBall.catchRateModifier.isGuaranteed()) {
            return CaptureContext.successful()
        }
        // We don't have dark grass so we're just gonna pretend everything is that.
        val darkGrass = if (thrower is ServerPlayerEntity) this.caughtMultiplierFor(thrower).roundToInt() else 1
        val catchRate = target.form.catchRate.toFloat()
        val validModifier = pokeBall.catchRateModifier.isValid(thrower, target)
        val bonusStatus = when (target.status?.status) {
            is SleepStatus, is FrozenStatus -> 2.5F
            is ParalysisStatus, is BurnStatus, is PoisonStatus, is PoisonBadlyStatus -> 1.5F
            else -> 1F
        }
        val rate: Float
        val ballBonus: Int
        if (pokeBall == PokeBalls.HEAVY_BALL) {
            rate = if (validModifier) pokeBall.catchRateModifier.modifyCatchRate(catchRate, thrower, target).coerceAtLeast(1F) else 1F
            ballBonus = 1
        }
        else {
            rate = catchRate
            ballBonus = if (validModifier) pokeBall.catchRateModifier.value(thrower, target).roundToInt().coerceAtLeast(1) else 1
        }
        val modifiedCatchRate = (pokeBall.catchRateModifier.behavior(thrower, target).mutator((3F * target.hp - 2F * target.currentHealth) * 4096F * darkGrass * rate, ballBonus.toFloat()) / 3F * target.hp) * bonusStatus
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
        return CaptureContext(numberOfShakes = shakes, isSuccessfulCapture = shakes == 4, isCriticalCapture = false)
    }

}