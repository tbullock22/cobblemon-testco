/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.battle

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.pokemon.status.PersistentStatus
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier

/**
 * Packet sent to change the status of a Pokémon in battle, such as paralysis or sleep.
 *
 * Handled by [com.cobblemon.mod.common.client.net.battle.BattlePersistentStatusHandler].
 *
 * @author Hiroku
 * @since November 5th, 2022
 */
class BattlePersistentStatusPacket() : NetworkPacket {
    lateinit var pnx: String
    var status: Identifier? = null

    constructor(pnx: String, status: PersistentStatus?): this() {
        this.pnx = pnx
        this.status = status?.name
    }

    override fun encode(buffer: PacketByteBuf) {
        buffer.writeString(pnx)
        buffer.writeNullable(status) { buf, value -> buf.writeIdentifier(value)}
    }

    override fun decode(buffer: PacketByteBuf) {
        pnx = buffer.readString()
        status = buffer.readNullable(PacketByteBuf::readIdentifier)
    }
}