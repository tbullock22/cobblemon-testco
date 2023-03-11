/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.world.gamerules

import net.minecraft.world.GameRules

object CobblemonGameRules {
    lateinit var DO_POKEMON_SPAWNING: GameRules.Key<GameRules.BooleanRule>

    fun register() {
        DO_POKEMON_SPAWNING = GameRules.register("doPokemonSpawning", GameRules.Category.SPAWNING, GameRules.BooleanRule.create(true))
    }
}