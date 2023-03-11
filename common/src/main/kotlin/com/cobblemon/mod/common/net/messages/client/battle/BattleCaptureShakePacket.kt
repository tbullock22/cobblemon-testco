/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.battle

import com.cobblemon.mod.common.api.net.NetworkPacket
import net.minecraft.network.PacketByteBuf

/**
 * Packet sent when a PokéBall capturing a battle Pokémon just shook. The direction is included.
 *
 * Handled by [com.cobblemon.mod.common.client.net.battle.BattleCaptureShakeHandler].
 *
 * @author Hiroku
 * @since July 3rd, 2022
 */
class BattleCaptureShakePacket() : NetworkPacket {
    lateinit var targetPNX: String
    var direction = true

    constructor(targetPNX: String, direction: Boolean): this() {
        this.targetPNX = targetPNX
        this.direction = direction
    }

    override fun encode(buffer: PacketByteBuf) {
        buffer.writeString(targetPNX)
        buffer.writeBoolean(direction)
    }

    override fun decode(buffer: PacketByteBuf) {
        targetPNX = buffer.readString()
        direction = buffer.readBoolean()
    }
}