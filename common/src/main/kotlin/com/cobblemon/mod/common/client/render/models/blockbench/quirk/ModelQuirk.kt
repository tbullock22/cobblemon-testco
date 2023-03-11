/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.quirk

import com.cobblemon.mod.common.client.render.models.blockbench.PoseableEntityModel
import com.cobblemon.mod.common.client.render.models.blockbench.PoseableEntityState
import net.minecraft.entity.Entity

abstract class ModelQuirk<T : Entity, D : QuirkData<T>>(val name: String) {
    abstract fun createData(): D
    protected abstract fun tick(state: PoseableEntityState<T>, data: D)
    fun tick(entity: T?, model: PoseableEntityModel<T>, state: PoseableEntityState<T>, limbSwing: Float, limbSwingAmount: Float, ageInTicks: Float, headYaw: Float, headPitch: Float) {
        val data = getOrCreateData(state)
        tick(state, data)
        data.run(entity, model, state, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch)
    }
    fun getOrCreateData(state: PoseableEntityState<T>): D {
        return state.quirks.getOrPut(this, this::createData) as D
    }
}