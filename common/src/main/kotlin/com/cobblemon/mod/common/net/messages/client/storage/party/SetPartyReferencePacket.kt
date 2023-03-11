/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.storage.party

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.storage.PokemonStore
import java.util.UUID
import net.minecraft.network.PacketByteBuf

/**
 * Sets the given party store as the player's active party. This will change what the overlay
 * is showing, but will fail if the store supplied in this packet is one unknown to the client.
 * To inform the player about a store before doing this, [PokemonStore.sendTo] will serve this
 * purpose. If you have previously used [InitializePartyPacket] then that won't be necessary.
 *
 * Handled by [com.cobblemon.mod.common.client.net.storage.party.SetPartyReferenceHandler]
 *
 * @author Hiroku
 * @since November 29th, 2021
 */
class SetPartyReferencePacket() : NetworkPacket {
    lateinit var storeID: UUID

    constructor(storageID: UUID): this() {
        this.storeID = storageID
    }

    override fun encode(buffer: PacketByteBuf) {
        buffer.writeUuid(this.storeID)
    }

    override fun decode(buffer: PacketByteBuf) {
        this.storeID = buffer.readUuid()
    }
}