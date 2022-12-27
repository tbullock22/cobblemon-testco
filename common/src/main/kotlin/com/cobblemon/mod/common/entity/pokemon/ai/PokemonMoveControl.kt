/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.ai

import com.cobblemon.mod.common.api.pokemon.status.Statuses
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokemon.PokemonBehaviourFlag
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.getWaterAndLavaIn
import com.cobblemon.mod.common.util.math.geometry.toDegrees
import com.cobblemon.mod.common.util.math.geometry.toRadians
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sqrt
import net.minecraft.entity.ai.control.MoveControl
import net.minecraft.entity.ai.pathing.PathNodeType
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.tag.BlockTags
import net.minecraft.tag.FluidTags
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
class PokemonMoveControl(val pokemonEntity: PokemonEntity) : MoveControl(pokemonEntity) {
    companion object {
        const val VERY_CLOSE = 2.500000277905201E-3
    }

    override fun tick() {
        if (pokemonEntity.pokemon.status?.status == Statuses.SLEEP || pokemonEntity.isDead) {
            pokemonEntity.movementSpeed = 0F
            pokemonEntity.upwardSpeed = 0F
            return
        }
        val behaviour = pokemonEntity.behaviour
        val mediumSpeed = if (pokemonEntity.getPoseType() in setOf(PoseType.FLY, PoseType.HOVER)) {
            behaviour.moving.fly.flySpeedHorizontal
        } else if (pokemonEntity.isSubmergedIn(FluidTags.WATER) || pokemonEntity.isSubmergedIn(FluidTags.LAVA)) {
            behaviour.moving.swim.swimSpeed
        } else {
            behaviour.moving.walk.walkSpeed
        }

        val baseSpeed = entity.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED).toFloat() * this.speed.toFloat()
        val adjustedSpeed = baseSpeed * mediumSpeed

        if (state == State.STRAFE) {
            var movingDistanceTotal = MathHelper.sqrt(forwardMovement * forwardMovement + sidewaysMovement * sidewaysMovement)
            if (movingDistanceTotal < 1.0f) {
                movingDistanceTotal = 1.0f
            }

            movingDistanceTotal = adjustedSpeed / movingDistanceTotal

            val adjustedForward = forwardMovement * movingDistanceTotal
            val adjustedStrafe = sidewaysMovement * movingDistanceTotal

            val xComponent = MathHelper.sin(entity.yaw.toRadians())
            val zComponent = MathHelper.cos(entity.yaw.toRadians())
            val xMovement = adjustedForward * zComponent - adjustedStrafe * xComponent
            val zMovement = adjustedStrafe * zComponent + adjustedForward * xComponent
            if (!isWalkable(xMovement, zMovement)) {
                forwardMovement = 1.0f
                sidewaysMovement = 0.0f
            }
            entity.movementSpeed = adjustedSpeed
            entity.setForwardSpeed(forwardMovement)
            entity.setSidewaysSpeed(sidewaysMovement)
            state = State.WAIT
        } else if (state == State.MOVE_TO) {
            val xDist = targetX - entity.x
            val zDist = targetZ - entity.z
            val yDist = targetY - entity.y

            val horizontalDistanceFromTarget = xDist * xDist + zDist * zDist
            val closeHorizontally = horizontalDistanceFromTarget < VERY_CLOSE
            if (!closeHorizontally) {
                val angleToTarget = MathHelper.atan2(zDist, xDist).toDegrees() - 90.0f
                val currentMovingAngle = entity.yaw
                val steppedAngle = MathHelper.stepUnwrappedAngleTowards(currentMovingAngle, angleToTarget,  if (entity.isOnGround) 20F else 5F)
                entity.yaw = steppedAngle
            }

            val (inWater, inLava) = entity.world.getWaterAndLavaIn(entity.boundingBox)
            val inFluid = inWater || inLava
            var verticalHandled = false
            val blockPos = entity.blockPos
            val blockState = entity.world.getBlockState(blockPos)
            val voxelShape = blockState.getCollisionShape(entity.world, blockPos)

            if (pokemonEntity.getBehaviourFlag(PokemonBehaviourFlag.FLYING) || inFluid) {
                verticalHandled = true
                entity.upwardSpeed = 0F
                entity.movementSpeed = 0F
                val refine: (Double) -> Double = { if (abs(it) < 0.05) 0.0 else it }

                val fullDistance = Vec3d(
                    refine(xDist),
                    if (inFluid || pokemonEntity.getBehaviourFlag(PokemonBehaviourFlag.FLYING)) refine(yDist + 0.05) else 0.0,
                    refine(zDist)
                )

                val direction = fullDistance.normalize()

                val scale = min(adjustedSpeed.toDouble(), fullDistance.length())

                entity.velocity = direction.multiply(scale)
            } else {
                val forwardSpeed = min(adjustedSpeed, sqrt(horizontalDistanceFromTarget).toFloat())
                entity.movementSpeed = forwardSpeed
            }

            if (!verticalHandled) {
                val tooBigToStep = yDist > entity.stepHeight.toDouble()

                val closeEnoughToJump = sqrt(xDist * xDist + zDist * zDist) < 1.0f.coerceAtLeast(entity.width).toDouble() + 1

                if (tooBigToStep &&
                    closeEnoughToJump ||
                    !voxelShape.isEmpty && entity.y < voxelShape.getMax(Direction.Axis.Y) + blockPos.y.toDouble() &&
                    !blockState.isIn(BlockTags.DOORS) &&
                    !blockState.isIn(BlockTags.FENCES)
                ) {
                    entity.jumpControl.setActive()
                    state = State.JUMPING
                }
            }

            if (closeHorizontally && abs(yDist) < VERY_CLOSE) {
                state = State.WAIT
            }
        } else if (state == State.JUMPING) {
            entity.movementSpeed = adjustedSpeed
            entity.upwardSpeed = 0F
            if (entity.isOnGround || pokemonEntity.getBehaviourFlag(PokemonBehaviourFlag.FLYING)) {
                state = State.WAIT
            }
        } else {
            entity.setForwardSpeed(0.0f)
            entity.upwardSpeed = 0F
        }

        if (state == State.WAIT) {
            if (entity.isOnGround && behaviour.moving.walk.canWalk && pokemonEntity.getBehaviourFlag(PokemonBehaviourFlag.FLYING)) {
                pokemonEntity.setBehaviourFlag(PokemonBehaviourFlag.FLYING, false)
            }

            if (entity.isSubmergedIn(FluidTags.WATER) && behaviour.moving.swim.canSwimInWater) {
                pokemonEntity.setUpwardSpeed(0.2F)
            }
        }
    }

    private fun isWalkable(xMovement: Float, zMovement: Float): Boolean {
        val entityNavigation = entity.navigation
        if (entityNavigation != null) {
            val pathNodeMaker = entityNavigation.nodeMaker
            if (pathNodeMaker != null &&
                pathNodeMaker.getDefaultNodeType(
                    entity.world,
                    MathHelper.floor(entity.x + xMovement.toDouble()),
                    entity.blockY,
                    MathHelper.floor(entity.z + zMovement.toDouble())
                ) != PathNodeType.WALKABLE
            ) {
                return false
            }
        }
        return true
    }
}