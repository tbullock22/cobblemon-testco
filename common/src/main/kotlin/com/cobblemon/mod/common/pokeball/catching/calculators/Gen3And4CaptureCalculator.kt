/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokeball.catching.calculators

import com.cobblemon.mod.common.api.pokeball.PokeBalls
import com.cobblemon.mod.common.api.pokeball.catching.CaptureContext
import com.cobblemon.mod.common.api.pokeball.catching.calculators.CaptureCalculator
import com.cobblemon.mod.common.pokeball.PokeBall
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.status.statuses.*
import net.minecraft.entity.LivingEntity
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * An implementation of the capture calculator used in the generation 3 and 4 games.
 * For more information see the [Bulbapedia](https://bulbapedia.bulbagarden.net/wiki/Catch_rate#Capture_method_.28Generation_III-IV.29) page.
 *
 * @author Licious
 * @since January 29th, 2022
 */
object Gen3And4CaptureCalculator : CaptureCalculator {

    override fun id(): String = "generation_3_and_4"

    private val apricornPokeballs = setOf(
        PokeBalls.HEAVY_BALL,
        PokeBalls.LURE_BALL,
        PokeBalls.FRIEND_BALL,
        PokeBalls.LOVE_BALL,
        PokeBalls.LEVEL_BALL,
        PokeBalls.FAST_BALL,
        PokeBalls.MOON_BALL
    )

    override fun processCapture(thrower: LivingEntity, pokeBall: PokeBall, target: Pokemon): CaptureContext {
        if (pokeBall.catchRateModifier.isGuaranteed()) {
            return CaptureContext.successful()
        }
        val catchRate = target.form.catchRate.toFloat()
        val validModifier = pokeBall.catchRateModifier.isValid(thrower, target)
        val bonusStatus = when (target.status?.status) {
            is SleepStatus, is FrozenStatus -> 2F
            is ParalysisStatus, is BurnStatus, is PoisonStatus, is PoisonBadlyStatus -> 1.5F
            else -> 1F
        }
        val rate: Float
        val ballBonus: Float
        if (this.apricornPokeballs.contains(pokeBall)) {
            rate = if (validModifier) pokeBall.catchRateModifier.modifyCatchRate(catchRate, thrower, target) else 1F
            ballBonus = 1F
        }
        else {
            rate = catchRate
            ballBonus = if (validModifier) pokeBall.catchRateModifier.value(thrower, target) else 1F
        }
        val modifiedCatchRate = (pokeBall.catchRateModifier.behavior(thrower, target).mutator((3F * target.hp - 2F * target.currentHealth) * rate, ballBonus) / (3F * target.hp)) * bonusStatus
        val shakeProbability = (1048560F / sqrt(sqrt((16711680F / modifiedCatchRate).roundToInt().toDouble())).roundToInt()).roundToInt()
        var shakes = 0
        repeat(4) {
            val n = Random.nextInt(65537)
            if (n < shakeProbability) {
                shakes++
            }
        }
        return CaptureContext(numberOfShakes = shakes, isSuccessfulCapture = shakes == 4, isCriticalCapture = false)
    }

}