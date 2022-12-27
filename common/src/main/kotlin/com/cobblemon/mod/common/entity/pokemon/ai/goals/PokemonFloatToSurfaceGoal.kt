/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.ai.goals

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import java.util.EnumSet
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.tag.FluidTags

class PokemonFloatToSurfaceGoal(val pokemonEntity: PokemonEntity) : Goal() {
    companion object {
        private val controls = EnumSet.of(Control.JUMP)
    }

    override fun getControls(): EnumSet<Control> = Companion.controls

    override fun canStart(): Boolean {
        val canSwimInWater = pokemonEntity.behaviour.moving.swim.canSwimInWater
        val canSwimInLava = pokemonEntity.behaviour.moving.swim.canSwimInLava
        val canBreatheUnderlava = pokemonEntity.behaviour.moving.swim.canBreatheUnderlava
        val canBreatheUnderwater = pokemonEntity.behaviour.moving.swim.canBreatheUnderwater

        if (!this.pokemonEntity.navigation.isIdle) {
            return false
        }

        if (this.pokemonEntity.isInLava && !canBreatheUnderlava) {
            return true
        } else if (canSwimInWater && !canBreatheUnderwater && this.pokemonEntity.isTouchingWater && this.pokemonEntity.getFluidHeight(FluidTags.WATER) > this.pokemonEntity.swimHeight) {
            return true
        }

        return false
    }

    override fun shouldRunEveryTick() = true
    override fun tick() {
        if (this.pokemonEntity.random.nextFloat() < 0.8f) {
            this.pokemonEntity.jumpControl.setActive()
        }
    }
}