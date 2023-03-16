/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokeball.catching.calculators

import com.cobblemon.mod.common.api.pokeball.catching.CaptureContext
import com.cobblemon.mod.common.api.pokeball.catching.calculators.CaptureCalculator
import com.cobblemon.mod.common.pokeball.PokeBall
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.status.statuses.*
import net.minecraft.entity.LivingEntity
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * An implementation of the capture calculator used in the generation 1 games.
 * For more information see the [Bulbapedia](https://bulbapedia.bulbagarden.net/wiki/Catch_rate#Capture_method_.28Generation_II.29) page.
 *
 * @property bugsFixed Should the bug with the bonusStatus in the formula be fixed? Having this on false is how the games shipped out.
 *
 * @author Licious
 * @since January 29th, 2022
 */
class Gen2CaptureCalculator(val bugsFixed: Boolean) : CaptureCalculator {

    override fun id(): String = "generation_2" + if (this.bugsFixed) "_fixed" else ""

    override fun processCapture(thrower: LivingEntity, pokeBall: PokeBall, target: Pokemon): CaptureContext {
        if (pokeBall.catchRateModifier.isGuaranteed()) {
            return CaptureContext.successful()
        }
        val catchRate = target.form.catchRate.toFloat()
        val modifiedRate = if (pokeBall.catchRateModifier.isValid(thrower, target)) pokeBall.catchRateModifier.modifyCatchRate(catchRate, thrower, target) else catchRate
        val status = target.status?.status
        val bonusStatus = when {
            status is SleepStatus || status is FrozenStatus -> 10
            this.bugsFixed && (status is ParalysisStatus || status is BurnStatus || status is PoisonStatus || status is PoisonBadlyStatus) -> 5
            else -> 1
        }
        val modifiedCatchRate = max((((3F * target.hp - 2F * target.currentHealth) * modifiedRate) / (3F * target.hp)) + bonusStatus, 1F).coerceAtMost(255F).roundToInt()
        if (Random.nextInt(256) <= modifiedCatchRate) {
            return CaptureContext.successful()
        }
        val shakeProbability = when {
            modifiedCatchRate <= 1 -> 63
            modifiedCatchRate == 2 -> 75
            modifiedCatchRate == 3 -> 84
            modifiedCatchRate == 4 -> 90
            modifiedCatchRate == 5 -> 95
            modifiedCatchRate <= 7 -> 103
            modifiedCatchRate <= 10 -> 113
            modifiedCatchRate <= 15 -> 126
            modifiedCatchRate <= 20 -> 134
            modifiedCatchRate <= 30 -> 149
            modifiedCatchRate <= 40 -> 160
            modifiedCatchRate <= 50 -> 169
            modifiedCatchRate <= 60 -> 177
            modifiedCatchRate <= 80 -> 191
            modifiedCatchRate <= 100 -> 201
            modifiedCatchRate <= 120 -> 211
            modifiedCatchRate <= 140 -> 200
            modifiedCatchRate <= 160 -> 227
            modifiedCatchRate <= 180 -> 234
            modifiedCatchRate <= 200 -> 240
            modifiedCatchRate <= 220 -> 246
            modifiedCatchRate <= 240 -> 251
            modifiedCatchRate <= 254 -> 253
            else -> 255
        }
        var shakes = 0
        repeat(3) {
            if (Random.nextInt(256) >= shakeProbability) {
                return CaptureContext(numberOfShakes = shakes, isSuccessfulCapture = false, isCriticalCapture = false)
            }
            shakes++
        }
        return CaptureContext.successful()
    }

}