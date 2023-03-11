/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.animation

import com.cobblemon.mod.common.client.render.models.blockbench.PoseableEntityModel
import com.cobblemon.mod.common.client.render.models.blockbench.PoseableEntityState
import com.cobblemon.mod.common.client.render.models.blockbench.addRotation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BiWingedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.wavefunction.WaveFunction
import net.minecraft.entity.Entity
class WingFlapIdleAnimation<T : Entity>(
    frame: BiWingedFrame,
    val flapFunction: WaveFunction,
    val timeVariable: (state: PoseableEntityState<T>?, limbSwing: Float, ageInTicks: Float) -> Float?,
    val axis: Int
) : StatelessAnimation<T, BiWingedFrame>(frame) {
    override val targetFrame = BiWingedFrame::class.java

    override fun setAngles(entity: T?, model: PoseableEntityModel<T>, state: PoseableEntityState<T>?, limbSwing: Float, limbSwingAmount: Float, ageInTicks: Float, headYaw: Float, headPitch: Float) {
        val time = timeVariable(state, limbSwing, ageInTicks) ?: 0F
        frame.leftWing.addRotation(axis, model.scaleForPart(frame.leftWing, flapFunction(time)))
        frame.rightWing.addRotation(axis, model.scaleForPart(frame.rightWing, -flapFunction(time)))
    }
}