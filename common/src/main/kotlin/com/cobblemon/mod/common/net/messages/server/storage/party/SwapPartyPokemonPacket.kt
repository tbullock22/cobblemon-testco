/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.server.storage.party

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.storage.party.PartyPosition
import com.cobblemon.mod.common.api.storage.party.PartyPosition.Companion.readPartyPosition
import com.cobblemon.mod.common.api.storage.party.PartyPosition.Companion.writePartyPosition
import com.cobblemon.mod.common.net.serverhandling.storage.party.SwapPartyPokemonHandler
import java.util.UUID
import net.minecraft.network.PacketByteBuf

/**
 * Tells the server to swap two Pokémon in the player's party.
 *
 * Handled by [SwapPartyPokemonHandler].
 *
 * @author Hiroku
 * @since June 20th, 2022
 */
class SwapPartyPokemonPacket() : NetworkPacket {
    lateinit var pokemon1ID: UUID
    lateinit var position1: PartyPosition
    lateinit var pokemon2ID: UUID
    lateinit var position2: PartyPosition

    constructor(pokemon1ID: UUID, position1: PartyPosition, pokemon2ID: UUID, position2: PartyPosition): this() {
        this.pokemon1ID = pokemon1ID
        this.position1 = position1
        this.pokemon2ID = pokemon2ID
        this.position2 = position2
    }

    override fun encode(buffer: PacketByteBuf) {
        buffer.writeUuid(pokemon1ID)
        buffer.writePartyPosition(position1)
        buffer.writeUuid(pokemon2ID)
        buffer.writePartyPosition(position2)
    }

    override fun decode(buffer: PacketByteBuf) {
        pokemon1ID = buffer.readUuid()
        position1 = buffer.readPartyPosition()
        pokemon2ID = buffer.readUuid()
        position2 = buffer.readPartyPosition()
    }
}