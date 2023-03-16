/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.battle

import com.cobblemon.mod.common.api.reactive.SettableObservable
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.api.scheduling.after
import com.cobblemon.mod.common.api.scheduling.lerp
import com.cobblemon.mod.common.client.render.pokeball.PokeBallPoseableState
import com.cobblemon.mod.common.entity.pokeball.EmptyPokeBallEntity
import com.cobblemon.mod.common.pokeball.PokeBall

/**
 * Handles the state for a capture PokéBall in a battle on the client side.
 *
 * @author Hiroku
 * @since July 2nd, 2022
 */
class ClientBallDisplay(val pokeBall: PokeBall, val aspects: Set<String>) : PokeBallPoseableState() {
    override val stateEmitter = SettableObservable(EmptyPokeBallEntity.CaptureState.FALL)
    override val shakeEmitter = SimpleObservable<Unit>()

    var scale = 1F

    fun start() {
        initSubscriptions()

        after(seconds = 1F) {
            lerp(seconds = 0.3F) { scale = 1 - it }
            after(seconds = 0.3F) {
                stateEmitter.set(EmptyPokeBallEntity.CaptureState.SHAKE)
                lerp(seconds = 0.3F) { scale = it }
            }
        }
    }

    fun finish() {
        lerp(seconds = 0.3F) { scale = 1 - it }
        after(seconds = 0.3F) {
            lerp(seconds = 0.3F) { scale = it }
            after(seconds = 0.3F) {

            }
        }
    }
}