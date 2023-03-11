/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.storage.party

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.net.IntSize
import com.cobblemon.mod.common.util.readSizedInt
import com.cobblemon.mod.common.util.writeSizedInt
import java.util.UUID
import net.minecraft.network.PacketByteBuf

/**
 * Creates a party on the client side with the given UUID and slot count.
 *
 * This can be used for immediately telling the client that this is their party to use
 * in overlay rendering, but generally is just necessary before sending Pokémon updates
 * targeting this store.
 *
 * Handled by [com.cobblemon.mod.common.client.net.storage.party.InitializePartyHandler]
 *
 * @author Hiroku
 * @since November 29th, 2021
 */
class InitializePartyPacket() : NetworkPacket {
    /** Whether this should be set as the player's party for rendering immediately. */
    var isThisPlayerParty: Boolean = false
    /** The UUID of the party storage. Does not need to be the player's UUID. */
    var uuid = UUID.randomUUID()
    /** The number of slots in the party. Defaults to 6. */
    var slots: Int = 6

    constructor(isThisPlayerParty: Boolean, uuid: UUID, slots: Int): this() {
        this.isThisPlayerParty = isThisPlayerParty
        this.uuid = uuid
        this.slots = slots
    }

    override fun encode(buffer: PacketByteBuf) {
        buffer.writeBoolean(isThisPlayerParty)
        buffer.writeUuid(uuid)
        buffer.writeSizedInt(IntSize.U_BYTE, slots)
    }

    override fun decode(buffer: PacketByteBuf) {
        isThisPlayerParty = buffer.readBoolean()
        uuid = buffer.readUuid()
        slots = buffer.readSizedInt(IntSize.U_BYTE)
    }
}