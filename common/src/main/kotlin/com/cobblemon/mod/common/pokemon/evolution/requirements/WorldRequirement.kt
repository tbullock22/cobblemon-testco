/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.evolution.requirements

import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.evolution.requirements.template.EntityQueryRequirement
import net.minecraft.entity.LivingEntity
import net.minecraft.util.Identifier
import net.minecraft.world.World

/**
 * A [EntityQueryRequirement] for when a [Pokemon] is expected to be in a [World].
 *
 * @property identifier The [Identifier] of the [World] the queried entity is expected to be in.
 * @author Licious
 * @since March 21st, 2022
 */
class WorldRequirement : EntityQueryRequirement {
    companion object {
        const val ADAPTER_VARIANT = "world"
    }
    val identifier: Identifier = Identifier("minecraft:the_overworld")
    override fun check(pokemon: Pokemon, queriedEntity: LivingEntity) = queriedEntity.world.registryKey.value == this.identifier
}