/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.ai.goals

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.entity.ai.goal.FollowOwnerGoal

/**
 * An override of the [FollowOwnerGoal] so that Pokémon behaviours can be implemented.
 *
 * @author Hiroku
 * @since July 30th, 2022
 */
class PokemonFollowOwnerGoal(
    val entity: PokemonEntity,
    speed: Double,
    minDistance: Float,
    maxDistance: Float,
    leavesAllowed: Boolean
) : FollowOwnerGoal(entity, speed, minDistance, maxDistance, leavesAllowed) {
    fun canMove() = entity.behaviour.moving.walk.canWalk || entity.behaviour.moving.fly.canFly// TODO probably depends on whether we're underwater or not
    override fun canStart() = super.canStart() && canMove()
    override fun shouldContinue() = super.shouldContinue() && canMove()
}