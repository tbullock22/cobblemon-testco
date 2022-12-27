/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util

import net.minecraft.entity.Entity
import net.minecraft.util.function.BooleanBiFunction
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.util.shape.VoxelShapes

fun Entity.setPositionSafely(pos: Vec3d): Boolean {
    var result = pos
    val width = this.width * 0.8F
    val eyes = pos.withAxis(Direction.Axis.Y, pos.y + this.standingEyeHeight)

    val box = Box.of(eyes, width.toDouble(), 1.0E-6, width.toDouble())
    val conflicts = mutableSetOf<Direction>()

    for (target in BlockPos.stream(box)) {
        val blockState = this.world.getBlockState(target)
        val collides = !blockState.isAir &&
                blockState.shouldSuffocate(this.world, target) &&
                VoxelShapes.matchesAnywhere(blockState.getCollisionShape(this.world, target)
                    .offset(target.x.toDouble(), target.y.toDouble(), target.z.toDouble()),
                    VoxelShapes.cuboid(box),
                    BooleanBiFunction.AND
                )
        if (collides) {
            val x = BlockPos(eyes)
            for (direction in Direction.values()) {
                if (conflicts.contains(direction)) continue

                val conflict = target.toVec3d()
                if (x.add(direction.vector) == target) {
                    conflicts.add(direction)
                    when (direction) {
                        Direction.UP -> return false
                        Direction.NORTH -> {
                            result = result.add(Vec3d(0.0, 0.0, 1 + (conflict.z - box.minZ + (1.0 / 8.0))))
                        }
                        Direction.SOUTH -> {
                            result = result.add(Vec3d(0.0, 0.0, -1 * (1 - (conflict.z - box.minZ) + (1.0 / 8.0))))
                        }
                        Direction.WEST -> {
                            result = result.add(Vec3d(1 + (conflict.x - box.minX  + (1.0 / 8.0)), 0.0, 0.0))
                        }
                        Direction.EAST -> {
                            result = result.add(Vec3d(-1 * (1 - (conflict.x - box.minX) + (1.0 / 8.0)), 0.0, 0.0))
                        }
                        else -> {}
                    }
                }
            }

        }
    }

    if (conflicts.size >= 3) {
        this.setPosition(pos)
    }

    this.setPosition(result)
    return true
}

fun Entity.distanceTo(pos: BlockPos): Double {
    val difference = pos.toVec3d().subtract(this.pos)
    return difference.length()
}

fun Entity.closestPosition(positions: Iterable<BlockPos>, filter: (BlockPos) -> Boolean = { true }): BlockPos? {
    var closest: BlockPos? = null
    var closestDistance = Double.MAX_VALUE

    val iterator = positions.iterator()
    while (iterator.hasNext()) {
        val position = iterator.next()
        if (filter(position)) {
            val distance = distanceTo(position)
            if (distance < closestDistance) {
                closest = BlockPos(position)
                closestDistance = distance
            }
        }
    }

    return closest
}