/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.additives

import com.cobblemon.mod.common.client.render.models.blockbench.PoseableEntityModel
import com.cobblemon.mod.common.client.render.models.blockbench.PoseableEntityState
import com.cobblemon.mod.common.client.render.models.blockbench.addRotation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.EaredFrame
import com.cobblemon.mod.common.client.render.models.blockbench.getRotation
import com.cobblemon.mod.common.client.render.pokemon.PokemonRenderer.Companion.DELTA_TICKS
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import java.lang.Float.min
import net.minecraft.util.math.MathHelper.PI
import net.minecraft.util.math.MathHelper.sin

/** Intensity is between 0 and 1, 1 being that it pushes the ear through to its low range of motion. */
class EarBounceAdditive(val intensity: Float, val durationTicks: Int = 20) : PosedAdditiveAnimation<PokemonEntity> {
    var initialized = false
    var leftStartAngle = 0F
    var rightStartAngle = 0F
    var passedTicks: Float = 0F
    var leftTotalMovement: Float = 0F
    var rightTotalMovement: Float = 0F

    override fun run(entity: PokemonEntity?, model: PoseableEntityModel<PokemonEntity>, state: PoseableEntityState<PokemonEntity>?): Boolean {
        if (model !is EaredFrame) {
            return false
        }

        if (!initialized) {
            leftStartAngle = model.leftEarJoint.modelPart.getRotation(model.leftEarJoint.axis)
            rightStartAngle = model.rightEarJoint.modelPart.getRotation(model.rightEarJoint.axis)
            leftTotalMovement = model.leftEarJoint.rangeOfMotion.low - leftStartAngle
            rightTotalMovement = model.rightEarJoint.rangeOfMotion.low - rightStartAngle
            initialized = true
        }

        passedTicks = min(passedTicks + DELTA_TICKS, durationTicks.toFloat())

        val ratioInRange = sin(passedTicks / durationTicks * PI)
        val leftApply = ratioInRange * leftTotalMovement * intensity
        val rightApply = ratioInRange * rightTotalMovement * intensity

        model.leftEarJoint.modelPart.addRotation(model.leftEarJoint.axis, leftApply)
        model.rightEarJoint.modelPart.addRotation(model.rightEarJoint.axis, rightApply)

        return passedTicks < durationTicks
    }
}