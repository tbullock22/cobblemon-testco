/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.ai

import net.minecraft.fluid.Fluid
import net.minecraft.tag.FluidTags
import net.minecraft.tag.TagKey

class SwimBehaviour {
    val avoidsWater = false
    val canSwimInWater = true
    val canSwimInLava = true
    val swimSpeed = 0.3F
    val canBreatheUnderwater = false
    val canBreatheUnderlava = false
    val canWalkOnWater = false
    val canWalkOnLava = false

    fun canWalkOnFluid(tag: TagKey<Fluid>) = if (tag == FluidTags.WATER) canWalkOnWater else if (tag == FluidTags.LAVA) canWalkOnLava else false
    fun canBreatheUnderFluid(tag: TagKey<Fluid>) = if (tag == FluidTags.WATER) canBreatheUnderwater else if (tag == FluidTags.LAVA) canBreatheUnderlava else false
    fun canSwimInFluid(tag: TagKey<Fluid>) = if (tag == FluidTags.WATER) canSwimInWater else if (tag == FluidTags.LAVA) canSwimInLava else false
}