/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.spawner

import com.cobblemon.mod.common.Cobblemon.config
import com.cobblemon.mod.common.api.spawning.SpawnCause
import com.cobblemon.mod.common.api.spawning.SpawnerManager
import com.cobblemon.mod.common.api.spawning.detail.SpawnPool
import com.cobblemon.mod.common.util.getPlayer
import com.cobblemon.mod.common.util.nextBetween
import java.util.UUID
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.MathHelper.PI
import net.minecraft.util.math.MathHelper.ceil

/**
 * A spawner that works around a single player. It will do basic tracking of a player's speed
 * and project that into the future and spawn in that direction.
 *
 * @author Hiroku
 * @since February 14th, 2022
 */
class PlayerSpawner(player: ServerPlayerEntity, spawns: SpawnPool, manager: SpawnerManager) : AreaSpawner(player.name.string, spawns, manager) {
    val uuid: UUID = player.uuid

    override fun getCauseEntity() = uuid.getPlayer()
    override fun getArea(cause: SpawnCause): SpawningArea? {
        val player = uuid.getPlayer() ?: return null
        val sliceDiameter = config.worldSliceDiameter
        val sliceHeight = config.worldSliceHeight

        val rand = Random.Default

        val center = player.pos

        val r = rand.nextBetween(config.minimumSliceDistanceFromPlayer, config.maximumSliceDistanceFromPlayer)
        val thetatemp = atan(player.velocity.z / player.velocity.x) + rand.nextBetween(-PI/2, PI/2 )
        val theta = if (player.velocity.horizontalLength() < 0.1) {
            rand.nextDouble() * 2 * PI
        } else if (player.velocity.x < 0) {
            PI - thetatemp
        } else {
            thetatemp
        }
        val x = center.x + r * cos(theta)
        val z = center.z + r * sin(theta)

        return SpawningArea(
            cause = cause,
            world = player.world,
            baseX = ceil(x - sliceDiameter / 2F),
            baseY = ceil(center.y - sliceHeight / 2F),
            baseZ = ceil(z - sliceDiameter / 2F),
            length = sliceDiameter,
            height = sliceHeight,
            width = sliceDiameter
        )
    }
}