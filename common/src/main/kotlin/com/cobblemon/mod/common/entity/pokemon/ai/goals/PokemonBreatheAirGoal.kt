/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.ai.goals

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.closestPosition
import net.minecraft.block.Blocks
import net.minecraft.entity.ai.goal.BreatheAirGoal
import net.minecraft.entity.ai.pathing.NavigationType
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.world.WorldView

class PokemonBreatheAirGoal(val pokemonEntity: PokemonEntity) : BreatheAirGoal(pokemonEntity) {
    override fun canStart(): Boolean {
        val canSwimUnderwater = pokemonEntity.behaviour.moving.swim.canBreatheUnderwater
        return if (pokemonEntity.isInLava && pokemonEntity.behaviour.moving.swim.canSwimInLava) {
            true
        } else if (pokemonEntity.isTouchingWater && (pokemonEntity.behaviour.moving.swim.avoidsWater || (!canSwimUnderwater && pokemonEntity.random.nextFloat() < 0.1)) && pokemonEntity.ownerUuid == null) {
            true
        } else {
            super.canStart()
        }
    }

    override fun start() {
        moveToBreathable()
    }

    override fun tick() {
        if (pokemonEntity.navigation.isIdle) {
            moveToBreathable()
        }
    }

    fun moveToBreathable() {
        val behaviour = pokemonEntity.behaviour
        if (!behaviour.moving.swim.canBreatheUnderwater) {
            val iterable = BlockPos.iterate(
                MathHelper.floor(pokemonEntity.x - 8.0),
                pokemonEntity.blockY,
                MathHelper.floor(pokemonEntity.z - 8.0),
                MathHelper.floor(pokemonEntity.x + 8.0),
                MathHelper.floor(pokemonEntity.y + 2.0),
                MathHelper.floor(pokemonEntity.z + 8.0)
            )
            val blockPos = pokemonEntity.closestPosition(iterable) { isAirPos(pokemonEntity.world, it) }
                ?: return
            pokemonEntity.navigation.startMovingTo(blockPos.x.toDouble(), (blockPos.y + 1).toDouble(), blockPos.z.toDouble(), 1.0)
        }
    }

    private fun isAirPos(world: WorldView, pos: BlockPos): Boolean {
        val blockState = world.getBlockState(pos)
        val aboveState = world.getBlockState(pos.up())
        val notFluid = world.getFluidState(pos.up()).isEmpty || blockState.isOf(Blocks.BUBBLE_COLUMN)
        val canPathfindThroughAbove = aboveState.canPathfindThrough(world, pos.up(), NavigationType.LAND)
        val solidBelow = !blockState.canPathfindThrough(world, pos, NavigationType.LAND)

        return notFluid && canPathfindThroughAbove && solidBelow
    }
}