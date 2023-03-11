/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.battle.animations

import com.cobblemon.mod.common.client.battle.ActiveClientBattlePokemon
class MoveTileOffscreenAnimation(val duration: Float = 0.75F) : TileAnimation {
    var passedSeconds = 0F
    override fun shouldHoldUntilNextAnimation() = true
    override fun invoke(activeBattlePokemon: ActiveClientBattlePokemon, deltaTicks: Float): Boolean {
        passedSeconds += deltaTicks / 20
        passedSeconds = passedSeconds.coerceAtMost(duration)
        val ratio = passedSeconds / duration
        val totalMovement = activeBattlePokemon.invisibleX - activeBattlePokemon.xDisplacement
        val currentMovement = totalMovement * ratio
        activeBattlePokemon.xDisplacement += currentMovement
        return passedSeconds == duration
    }
}