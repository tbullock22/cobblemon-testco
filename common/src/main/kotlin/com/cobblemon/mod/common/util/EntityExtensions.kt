/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util

import net.minecraft.entity.Entity
import net.minecraft.util.function.BooleanBiFunction
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.util.shape.VoxelShapes

fun Entity.setPositionSafely(pos: Vec3d): Boolean {
    var result = pos
    val eyes = pos.withAxis(Direction.Axis.Y, pos.y + this.standingEyeHeight)

    var box = boundingBox.offset(pos)
    val conflicts = mutableSetOf<Direction>()

    if (!world.getBlockCollisions(this, box).iterator().hasNext()) {
        setPosition(pos)
        return true
    }

    val yChanges = listOf(1.0, -1.0, 2.0, -2.0)
    var previousChange = 0.0
    for (yChange in yChanges) {
        box = box.offset(0.0, yChange - previousChange, 0.0)
        val it = world.getBlockCollisions(this, box).iterator()
        previousChange = yChange
        if (it.hasNext()) {
            continue
        } else {
            val roundedY = (pos.y + yChange).toInt()
            box = box.offset(0.0, roundedY - pos.y, 0.0)
            // If the rounded position actually collides again, then don't round at all.
            if (world.getBlockCollisions(this, box).iterator().hasNext()) {
                setPosition(pos.add(0.0, yChange, 0.0))
                return true
            }
            setPosition(Vec3d(pos.x, roundedY.toDouble(), pos.z))
            return true
        }
    }

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