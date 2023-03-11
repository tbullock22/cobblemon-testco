/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.storage.pc

import com.cobblemon.mod.common.api.net.NetworkPacket
import java.util.UUID
import net.minecraft.network.PacketByteBuf

/**
 * Notifies a player that they must close their PC GUI. If the [storeID] property is non-null, then
 * it will only close the PC GUI if it was opened for that specific PC.
 *
 * Handled by [com.cobblemon.mod.common.client.net.storage.pc.ClosePCHandler]
 *
 * @author Hiroku
 * @since June 20th, 2022
 */
class ClosePCPacket() : NetworkPacket {
    var storeID: UUID? = null

    constructor(storeID: UUID?): this() {
        this.storeID = storeID
    }

    override fun encode(buffer: PacketByteBuf) {
        buffer.writeBoolean(storeID != null)
        storeID?.let(buffer::writeUuid)
    }

    override fun decode(buffer: PacketByteBuf) {
        if (buffer.readBoolean()) {
            storeID = buffer.readUuid()
        }
    }
}