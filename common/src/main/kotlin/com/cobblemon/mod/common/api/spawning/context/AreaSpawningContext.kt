/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.context

import com.cobblemon.mod.common.api.spawning.SpawnCause
import com.cobblemon.mod.common.api.spawning.WorldSlice
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

/**
 * A [SpawningContext] that is for a particular area, and therefore has spatial properties.
 *
 * @author Hiroku
 * @since January 31st, 2022
 */
open class AreaSpawningContext(
    override val cause: SpawnCause,
    override val world: World,
    override val position: BlockPos,
    override val light: Int,
    override val canSeeSky: Boolean,
    override val influences: MutableList<SpawningInfluence>,
    /** Space horizontally (diameter) */
    val width: Int,
    /** Space above. */
    val height: Int,
    val nearbyBlocks: List<BlockState>,
    val slice: WorldSlice
) : SpawningContext() {
    val nearbyBlockTypes: List<Block> by lazy { nearbyBlocks.mapNotNull { it.block }.distinct() }
}