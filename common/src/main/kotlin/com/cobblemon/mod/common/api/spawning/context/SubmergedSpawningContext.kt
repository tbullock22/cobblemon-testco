/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.context

import com.cobblemon.mod.common.api.spawning.SpawnCause
import com.cobblemon.mod.common.api.spawning.WorldSlice
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

/**
 * A type of area based spawning context with a fluid base block.
 *
 * @author Hiroku
 * @since February 7th, 2022
 */
open class SubmergedSpawningContext(
    cause: SpawnCause,
    world: World,
    position: BlockPos,
    light: Int,
    canSeeSky: Boolean,
    influences: MutableList<SpawningInfluence>,
    width: Int,
    height: Int,
    val depth: Int,
    nearbyBlocks: List<BlockState>,
    slice: WorldSlice
) : AreaSpawningContext(cause, world, position, light, canSeeSky, influences, width, height, nearbyBlocks, slice) {
    val fluid = slice.getBlockState(position.x, position.y, position.z).fluidState
}