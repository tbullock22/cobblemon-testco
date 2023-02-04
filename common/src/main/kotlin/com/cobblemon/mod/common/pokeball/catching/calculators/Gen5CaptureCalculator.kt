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
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * An implementation of the capture calculator used in the generation 5 games.
 * For more information see the [Bulbapedia](https://bulbapedia.bulbagarden.net/wiki/Catch_rate#Capture_method_.28Generation_V.2B.29) page.
 *
 * @author Licious
 * @since January 29th, 2022
 */
object Gen5CaptureCalculator : CaptureCalculator, CriticalCaptureProvider, PokedexProgressCaptureMultiplierProvider {

    private val apricornPokeballs = setOf(
        PokeBalls.HEAVY_BALL,
        PokeBalls.LURE_BALL,
        PokeBalls.FRIEND_BALL,
        PokeBalls.LOVE_BALL,
        PokeBalls.LEVEL_BALL,
        PokeBalls.FAST_BALL,
        PokeBalls.MOON_BALL
    )

    override fun id(): String = "generation_5"

    // Note we skip passPower due to the feature not being a thing in Cobblemon
    override fun processCapture(thrower: LivingEntity, pokeBall: PokeBall, target: Pokemon): CaptureContext {
        if (pokeBall.catchRateModifier.isGuaranteed()) {
            return CaptureContext(numberOfShakes = 3, isSuccessfulCapture = true, isCriticalCapture = false)
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
        if (this.apricornPokeballs.contains(pokeBall)) {
            rate = if (validModifier) pokeBall.catchRateModifier.modifyCatchRate(catchRate, thrower, target) else 1F
            ballBonus = 1
        }
        else {
            rate = catchRate
            ballBonus = if (validModifier) pokeBall.catchRateModifier.value(thrower, target).roundToInt() else 1
        }
        val modifiedCatchRate = (pokeBall.catchRateModifier.behavior(thrower, target).mutator((3F * target.hp - 2F * target.currentHealth) * 4096F * darkGrass * rate, ballBonus.toFloat()) / 3F * target.hp) * bonusStatus
        val critical = if (thrower is ServerPlayerEntity) this.shouldHaveCriticalCapture(thrower, modifiedCatchRate) else false
        if (modifiedCatchRate >= 1044480) {
            return CaptureContext.successful(critical)
        }
        val shakeProbability = (65336F / sqrt(sqrt((1044480F / modifiedCatchRate).roundToInt().toDouble())).roundToInt()).roundToInt()
        var shakes = 0
        repeat(3) {
            var failed = true
            val n = Random.nextInt(65537)
            if (n < shakeProbability) {
                shakes++
                failed = false
            }
            if (critical && it == 0) {
                return CaptureContext(numberOfShakes = 1, isSuccessfulCapture = !failed, isCriticalCapture = true)
            }
            if (it == 0 && failed && !critical) {
                return CaptureContext(numberOfShakes = 0, isSuccessfulCapture = false, isCriticalCapture = false)
            }
            else if (it == 1 && failed && !critical) {
                return CaptureContext(numberOfShakes = 1, isSuccessfulCapture = false, isCriticalCapture = false)
            }
            else if (it == 2 && failed && !critical) {
                return CaptureContext(numberOfShakes = 3, isSuccessfulCapture = false, isCriticalCapture = false)
            }
        }
        return CaptureContext(numberOfShakes = shakes, isSuccessfulCapture = true, isCriticalCapture = false)
    }

}