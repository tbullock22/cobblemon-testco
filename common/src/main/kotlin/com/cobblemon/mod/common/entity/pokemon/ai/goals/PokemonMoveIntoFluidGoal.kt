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
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.fluid.Fluid
import net.minecraft.tag.FluidTags
import net.minecraft.tag.TagKey
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper

class PokemonMoveIntoFluidGoal(private val mob: PokemonEntity) : Goal() {
    override fun canStart(): Boolean {
        val avoidsLand = mob.behaviour.moving.walk.avoidsLand
        if (!avoidsLand || mob.owner != null) {
            return false
        }

        val allowsWater = mob.behaviour.moving.swim.canSwimInWater
        val allowsLava = mob.behaviour.moving.swim.canSwimInLava

        val pos = mob.blockPos

        val fluid = mob.world.getFluidState(pos)

        val allowedFluids = mutableListOf<TagKey<Fluid>>()
        if (allowsWater) {
            allowedFluids.add(FluidTags.WATER)
        }
        if (allowsLava) {
            allowedFluids.add(FluidTags.LAVA)
        }

        return mob.isOnGround && allowedFluids.none { fluid.isIn(it) } && allowedFluids.isNotEmpty()
    }

    override fun start() {
        val appropriateFluids = mutableListOf<TagKey<Fluid>>()
        val swim = mob.behaviour.moving.swim
        if (swim.canSwimInLava) {
            appropriateFluids.add(FluidTags.LAVA)
        }
        if (swim.canSwimInWater) {
            appropriateFluids.add(FluidTags.WATER)
        }
        val iterable = BlockPos.iterate(
            MathHelper.floor(mob.x - 8.0),
            MathHelper.floor(mob.y - 8.0),
            MathHelper.floor(mob.z - 8.0),
            MathHelper.floor(mob.x + 8.0),
            mob.blockY,
            MathHelper.floor(mob.z + 2.0)
        )
        val blockPos = mob.closestPosition(iterable) { pos ->
            val fluid = mob.world.getFluidState(pos)
            return@closestPosition appropriateFluids.any { fluid.isIn(it) } && mob.world.isSpaceEmpty(mob.boundingBox.offset(pos.subtract(mob.blockPos)))
        }
        if (blockPos != null) {
            mob.navigation.startMovingTo(blockPos.x.toDouble(), blockPos.y.toDouble(), blockPos.z.toDouble(), 1.0)
        }
    }
}
