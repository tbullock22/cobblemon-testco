/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.battle.animations

import com.cobblemon.mod.common.client.battle.ActiveClientBattlePokemon
class HealthChangeAnimation(val newHealthRatio: Float, val newHealth: Int, val duration: Float = 1F) : TileAnimation {
    var passedSeconds = 0F
    var initialHealthRatio = -1F
    var ratioDifference = 0F
    var initialHealth = -1
    var healthDifference = 0

    override fun shouldHoldUntilNextAnimation() = false
    override fun invoke(activeBattlePokemon: ActiveClientBattlePokemon, deltaTicks: Float): Boolean {
        val pokemon = activeBattlePokemon.battlePokemon ?: return true
        if (initialHealthRatio == -1F) {
            initialHealthRatio = pokemon.hpRatio
            ratioDifference = newHealthRatio - initialHealthRatio
        }

        if (initialHealth == -1) {
            healthDifference = newHealth - initialHealth
        }

        passedSeconds += deltaTicks / 20
        passedSeconds = passedSeconds.coerceAtMost(duration)
        pokemon.hpRatio = initialHealthRatio + (passedSeconds / duration) * ratioDifference
        return passedSeconds == duration
    }
}