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
import com.cobblemon.mod.common.client.render.models.blockbench.frame.ModelFrame
import net.minecraft.entity.Entity

/**
 * An animation that can run without an entity associated. These are
 * locked to a specific frame, and CAN be given an entity along with
 * limb swing and age information, but might not.
 *
 * @author Hiroku
 * @since December 4th, 2021
 */
abstract class StatelessAnimation<T : Entity, F : ModelFrame>(val frame: F) {
    abstract val targetFrame: Class<F>
    protected abstract fun setAngles(entity: T?, model: PoseableEntityModel<T>, state: PoseableEntityState<T>?, limbSwing: Float, limbSwingAmount: Float, ageInTicks: Float, headYaw: Float, headPitch: Float)

    fun apply(entity: T?, model: PoseableEntityModel<T>, state: PoseableEntityState<T>?, limbSwing: Float, limbSwingAmount: Float, ageInTicks: Float, headYaw: Float, headPitch: Float) {
        setAngles(entity, model, state, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch)
    }
}