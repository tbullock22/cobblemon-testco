/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.server.storage.pc

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.storage.pc.PCPosition
import com.cobblemon.mod.common.api.storage.pc.PCPosition.Companion.readPCPosition
import com.cobblemon.mod.common.api.storage.pc.PCPosition.Companion.writePCPosition
import com.cobblemon.mod.common.net.serverhandling.storage.pc.SwapPCPokemonHandler
import java.util.UUID
import net.minecraft.network.PacketByteBuf

/**
 * Tells the server to swap two Pokémon in the PC linked to the player.
 *
 * Handled by [SwapPCPokemonHandler].
 *
 * @author Hiroku
 * @since June 20th, 2022
 */
class SwapPCPokemonPacket() : NetworkPacket {
    lateinit var pokemon1ID: UUID
    lateinit var position1: PCPosition
    lateinit var pokemon2ID: UUID
    lateinit var position2: PCPosition

    constructor(pokemon1ID: UUID, position1: PCPosition, pokemon2ID: UUID, position2: PCPosition): this() {
        this.pokemon1ID = pokemon1ID
        this.position1 = position1
        this.pokemon2ID = pokemon2ID
        this.position2 = position2
    }

    override fun encode(buffer: PacketByteBuf) {
        buffer.writeUuid(pokemon1ID)
        buffer.writePCPosition(position1)
        buffer.writeUuid(pokemon2ID)
        buffer.writePCPosition(position2)
    }

    override fun decode(buffer: PacketByteBuf) {
        pokemon1ID = buffer.readUuid()
        position1 = buffer.readPCPosition()
        pokemon2ID = buffer.readUuid()
        position2 = buffer.readPCPosition()
    }
}