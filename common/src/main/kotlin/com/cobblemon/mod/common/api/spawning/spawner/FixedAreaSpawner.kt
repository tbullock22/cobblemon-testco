/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.spawner

import com.cobblemon.mod.common.api.spawning.SpawnCause
import com.cobblemon.mod.common.api.spawning.SpawnerManager
import com.cobblemon.mod.common.api.spawning.detail.SpawnPool
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

/**
 * A spawner that works within a fixed area, and ticks on its own. The location
 * and size of the area is necessary for construction, but after that this spawner
 * can be registered in a [SpawnerManager] and left to its own devices.
 *
 * @author Hiroku
 * @since February 5th, 2022
 */
open class FixedAreaSpawner(
    name: String,
    spawns: SpawnPool,
    manager: SpawnerManager,
    val world: World,
    val position: BlockPos,
    val horizontalRadius: Int,
    val verticalRadius: Int
) : AreaSpawner(name, spawns, manager) {
    override fun getArea(cause: SpawnCause): SpawningArea? {
        val basePos = position.add(-horizontalRadius, -verticalRadius, -horizontalRadius)

        return SpawningArea(
            cause = cause,
            world = world,
            baseX = basePos.x,
            baseY = basePos.y,
            baseZ = basePos.z,
            length = horizontalRadius * 2 + 1,
            height = verticalRadius * 2 + 1,
            width = horizontalRadius * 2 + 1
        )
    }
}