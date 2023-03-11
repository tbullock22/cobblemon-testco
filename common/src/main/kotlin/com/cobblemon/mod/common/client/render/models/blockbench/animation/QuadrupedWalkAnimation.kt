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
import com.cobblemon.mod.common.client.render.models.blockbench.frame.QuadrupedFrame
import net.minecraft.entity.Entity
import net.minecraft.util.math.MathHelper

/**
 * A quadruped animation that will have zero-rotations on all legs at
 * stateless and otherwise does simple predictable walking like Minecraft
 * quadrupeds.
 *
 * @author Hiroku
 * @since December 4th, 2021
 */
class QuadrupedWalkAnimation<T : Entity>(
    frame: QuadrupedFrame,
    /** The multiplier to apply to the cosine movement of the legs. The smaller this value, the quicker the legs move. */
    val periodMultiplier: Float = 0.6662F,
    /** The multiplier to apply to the stride of the entity. The larger this is, the further the legs move. */
    val amplitudeMultiplier: Float = 1.4F
) : StatelessAnimation<T, QuadrupedFrame>(frame) {
    override val targetFrame: Class<QuadrupedFrame> = QuadrupedFrame::class.java
    override fun setAngles(entity: T?, model: PoseableEntityModel<T>, state: PoseableEntityState<T>?, limbSwing: Float, limbSwingAmount: Float, ageInTicks: Float, headYaw: Float, headPitch: Float) {
        frame.hindRightLeg.pitch += MathHelper.cos(limbSwing * periodMultiplier) * limbSwingAmount * amplitudeMultiplier
        frame.hindLeftLeg.pitch += MathHelper.cos(limbSwing * periodMultiplier + Math.PI.toFloat()) * limbSwingAmount * amplitudeMultiplier
        frame.foreRightLeg.pitch += MathHelper.cos(limbSwing * periodMultiplier + Math.PI.toFloat()) * limbSwingAmount * amplitudeMultiplier
        frame.foreLeftLeg.pitch += MathHelper.cos(limbSwing * periodMultiplier) * limbSwingAmount * amplitudeMultiplier
    }
}