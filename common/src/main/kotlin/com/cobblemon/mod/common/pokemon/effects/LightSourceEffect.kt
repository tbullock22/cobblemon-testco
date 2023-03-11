/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.effects

import com.cobblemon.mod.common.api.pokemon.effect.ShoulderEffect
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.server.network.ServerPlayerEntity
class LightSourceEffect: ShoulderEffect {
    private var test = "AAA"

    override fun applyEffect(pokemon: Pokemon, player: ServerPlayerEntity, isLeft: Boolean) {
        println("Applying effect... $test")
    }

    override fun removeEffect(pokemon: Pokemon, player: ServerPlayerEntity, isLeft: Boolean) {
        println("Removing effect...")
    }
}