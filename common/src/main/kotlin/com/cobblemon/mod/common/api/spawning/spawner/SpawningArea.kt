/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.spawner

import com.cobblemon.mod.common.api.spawning.SpawnCause
import com.cobblemon.mod.common.api.spawning.WorldSlice
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

/**
 * An area in which to slice out a [WorldSlice].
 *
 * @author Hiroku
 * @since February 5th, 2022
 */
data class SpawningArea(
    val cause: SpawnCause,
    val world: World,
    val baseX: Int,
    val baseY: Int,
    val baseZ: Int,
    val length: Int,
    val height: Int,
    val width: Int
) {
    fun getCenter(): Vec3d = Vec3d(
        baseX + length / 2.0,
        baseY + height / 2.0,
        baseZ + width / 2.0
    )
}