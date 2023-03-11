/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.server.storage

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.storage.party.PartyPosition
import com.cobblemon.mod.common.api.storage.party.PartyPosition.Companion.readPartyPosition
import com.cobblemon.mod.common.api.storage.party.PartyPosition.Companion.writePartyPosition
import com.cobblemon.mod.common.api.storage.pc.PCPosition
import com.cobblemon.mod.common.api.storage.pc.PCPosition.Companion.readPCPosition
import com.cobblemon.mod.common.api.storage.pc.PCPosition.Companion.writePCPosition
import com.cobblemon.mod.common.net.serverhandling.storage.SwapPCPartyPokemonHandler
import java.util.UUID
import net.minecraft.network.PacketByteBuf

/**
 * Tells the server to swap Pokémon between the party and the currently linked PC. The positions are sent
 * along with the IDs to validate that the client is making a synchronized request.
 *
 * Handled by [SwapPCPartyPokemonHandler].
 *
 * @author Hiroku
 * @since June 20th, 2022
 */
class SwapPCPartyPokemonPacket() : NetworkPacket {
    lateinit var partyPokemonID: UUID
    lateinit var partyPosition: PartyPosition
    lateinit var pcPokemonID: UUID
    lateinit var pcPosition: PCPosition

    constructor(partyPokemonID: UUID, partyPosition: PartyPosition, pcPokemonID: UUID, pcPosition: PCPosition) : this() {
        this.partyPokemonID = partyPokemonID
        this.partyPosition = partyPosition
        this.pcPokemonID = pcPokemonID
        this.pcPosition = pcPosition
    }

    override fun encode(buffer: PacketByteBuf) {
        buffer.writeUuid(partyPokemonID)
        buffer.writePartyPosition(partyPosition)
        buffer.writeUuid(pcPokemonID)
        buffer.writePCPosition(pcPosition)
    }

    override fun decode(buffer: PacketByteBuf) {
        partyPokemonID = buffer.readUuid()
        partyPosition = buffer.readPartyPosition()
        pcPokemonID = buffer.readUuid()
        pcPosition = buffer.readPCPosition()
    }
}