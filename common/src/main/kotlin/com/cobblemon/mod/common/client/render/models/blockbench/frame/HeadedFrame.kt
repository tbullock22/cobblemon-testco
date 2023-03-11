/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.frame

import com.cobblemon.mod.common.client.render.models.blockbench.animation.SingleBoneLookAnimation
import net.minecraft.client.model.ModelPart
import net.minecraft.entity.Entity

interface HeadedFrame : ModelFrame {
    val head: ModelPart

    fun <T : Entity> singleBoneLook(invertX: Boolean = false, invertY: Boolean = false) = SingleBoneLookAnimation<T>(this, invertX, invertY)
}