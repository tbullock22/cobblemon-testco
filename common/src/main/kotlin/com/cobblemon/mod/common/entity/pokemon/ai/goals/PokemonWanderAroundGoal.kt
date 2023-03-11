/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.ai.goals

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.canFit
import com.cobblemon.mod.common.util.toVec3d
import net.minecraft.block.BlockState
import net.minecraft.block.Material
import net.minecraft.entity.ai.goal.WanderAroundGoal
import net.minecraft.fluid.Fluid
import net.minecraft.tag.FluidTags
import net.minecraft.tag.TagKey
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

/**
 * An override of the [WanderAroundGoal] so that Pokémon behaviours can be implemented.
 *
 * @author Hiroku
 * @since July 30th, 2022
 */
class PokemonWanderAroundGoal(val entity: PokemonEntity) : WanderAroundGoal(entity, entity.behaviour.moving.wanderSpeed) {
    init {
        chance = entity.behaviour.moving.wanderChance
    }

    fun canMove(): Boolean {
        val moving = entity.behaviour.moving
        return moving.walk.canWalk || moving.fly.canFly || (moving.swim.canBreatheUnderwater && mob.isSubmergedIn(FluidTags.WATER))
    }
    override fun canStart() = super.canStart() && canMove() && !entity.isBusy && entity.ownerUuid == null
    override fun shouldContinue() = super.shouldContinue() && canMove() && !entity.isBusy

    override fun getWanderTarget(): Vec3d? {
        val moving = entity.behaviour.moving
        if (entity.isSubmergedIn(FluidTags.WATER) && moving.swim.canBreatheUnderwater) {
            getFluidTarget(FluidTags.WATER)?.let { return it }
        } else if (entity.isSubmergedIn(FluidTags.LAVA) && moving.swim.canBreatheUnderlava) {
            return getFluidTarget(FluidTags.LAVA)
        } else if (entity.isTouchingWater && moving.swim.canWalkOnWater) {
            getFluidTarget(FluidTags.WATER)?.let { return it }
        } else if (entity.isInLava && moving.swim.canWalkOnLava) {
            getFluidTarget(FluidTags.LAVA)?.let { return it }
        }

        if (moving.walk.avoidsLand) {
            return null
        }

        return getLandTarget()
    }

    fun getLandTarget(): Vec3d? {
        val iterable: Iterable<BlockPos> = BlockPos.iterateRandomly(entity.random, 30, entity.blockX - 10, entity.blockY, entity.blockZ - 10, entity.blockX + 10, entity.blockY, entity.blockZ + 10)
        val condition: (BlockState, BlockPos) -> Boolean = { _, pos -> entity.canFit(pos) }
        val iterator = iterable.iterator()
        position@
        while (iterator.hasNext()) {
            val pos = iterator.next().mutableCopy()
            var blockState = entity.world.getBlockState(pos)

            val maxSteps = 16
            var steps = 0
            var good = false
            if (blockState.isAir) {
                pos.move(0, -1, 0)
                var previousWasAir = true
                while (steps++ < maxSteps && pos.y > entity.world.bottomY) {
                    if (pos.y <= entity.world.bottomY) {
                        continue@position
                    }
                    blockState = entity.world.getBlockState(pos)
                    if (blockState.material.isSolid && blockState.material != Material.LEAVES && previousWasAir) {
                        pos.move(0, 1, 0)
                        blockState = entity.world.getBlockState(pos)
                        good = true
                        break
                    } else {
                        previousWasAir = blockState.isAir
                    }
                    pos.move(0, -1, 0)
                }
            } else {
                var previousWasSolid = blockState.material.isSolid && blockState.material != Material.LEAVES
                pos.move(0, 1, 0)
                while (steps++ < maxSteps) {
                    if (pos.y >= entity.world.topY) {
                        continue@position
                    }
                    blockState = entity.world.getBlockState(pos)
                    if (blockState.isAir && previousWasSolid) {
                        good = true
                        break
                    }
                    previousWasSolid = blockState.material.isSolid && blockState.material != Material.LEAVES
                    pos.move(0, 1, 0)
                }
            }

            if (good && condition(blockState, pos)) {
                return pos.toVec3d()
            }
        }

        return null
    }

    fun getFluidTarget(fluidTag: TagKey<Fluid>): Vec3d? {
        val walksOnFloor = !entity.behaviour.moving.swim.canSwimInFluid(fluidTag)
        var iterable: Iterable<BlockPos> = BlockPos.iterateRandomly(entity.random, 32, entity.blockPos, 12)
        var condition: (BlockState, BlockPos) -> Boolean = { blockState, pos -> blockState.fluidState.isIn(fluidTag) && entity.canFit(pos) }
        if (walksOnFloor) {
            condition = { blockState, blockPos ->
                val down = blockPos.down()
                val below = entity.world.getBlockState(down)
                blockState.fluidState.isIn(fluidTag) && below.isSolidBlock(entity.world, down) && entity.canFit(blockPos)
            }
        }
        if (entity.world.isAir(entity.blockPos.up())) {
            if ((fluidTag == FluidTags.WATER && entity.behaviour.moving.swim.canWalkOnWater) ||
                    fluidTag == FluidTags.LAVA && entity.behaviour.moving.swim.canWalkOnLava) {
                iterable = BlockPos.iterateRandomly(entity.random, 16, entity.blockX - 16, entity.blockY, entity.blockZ - 16, entity.blockX + 16, entity.blockY, entity.blockZ + 16)
            }
        }
        val iterator = iterable.iterator()
        while (iterator.hasNext()) {
            val pos = iterator.next()
            val blockState = entity.world.getBlockState(pos)
            if (condition(blockState, pos)) {
                return pos.toVec3d()
            }
        }

        return null
    }
}