/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common

import com.cobblemon.mod.common.registry.CompletableRegistry
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.sound.SoundEvent
import net.minecraft.util.registry.Registry

object CobblemonSounds : CompletableRegistry<SoundEvent>(Registry.SOUND_EVENT_KEY) {
    private fun queue(name: String) = queue(name) { SoundEvent(cobblemonResource(name)) }

    val GUI_CLICK = queue("gui.click")

    val PC_ON = queue("pc.on")
    val PC_OFF = queue("pc.off")
    val PC_GRAB = queue("pc.grab")
    val PC_DROP = queue("pc.drop")
    val PC_RELEASE = queue("pc.release")

    val POKE_BALL_CAPTURE_STARTED = queue("poke_ball.capture_started")
    val POKE_BALL_CAPTURE_SUCCEEDED = queue("poke_ball.capture_succeeded")
    val POKE_BALL_SHAKE = queue("poke_ball.shake")
    val POKE_BALL_OPEN = queue("poke_ball.open")
    val POKE_BALL_HIT = queue("poke_ball.hit")
    val POKE_BALL_SEND_OUT = queue("poke_ball.send_out")
    val POKE_BALL_RECALL = queue("poke_ball.recall")

    val ITEM_USE = queue("item.use")
}