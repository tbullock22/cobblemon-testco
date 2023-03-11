/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.server

import com.cobblemon.mod.common.api.net.NetworkPacket
import net.minecraft.network.PacketByteBuf
class RequestMoveSwapPacket(): NetworkPacket {

    var move1 = -1
    var move2 = -1
    var slot = -1

    constructor(move1: Int, move2: Int, slot: Int): this() {
        this.move1 = move1
        this.move2 = move2
        this.slot = slot
    }

    override fun encode(buffer: PacketByteBuf) {
        buffer.writeInt(move1)
        buffer.writeInt(move2)
        buffer.writeInt(slot)
    }

    override fun decode(buffer: PacketByteBuf) {
        move1 = buffer.readInt()
        move2 = buffer.readInt()
        slot = buffer.readInt()
    }
}