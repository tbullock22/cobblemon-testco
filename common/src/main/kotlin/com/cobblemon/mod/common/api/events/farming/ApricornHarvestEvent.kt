/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events.farming

import net.minecraft.block.BlockState
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos

/**
 * Event fired when an Apricorn is harvested.
 */
class ApricornHarvestEvent(
    val player: ServerPlayerEntity,
    val world: ServerWorld,
    val pos: BlockPos
) {
    fun getBlock(): BlockState {
        return world.getBlockState(pos)
    }
}