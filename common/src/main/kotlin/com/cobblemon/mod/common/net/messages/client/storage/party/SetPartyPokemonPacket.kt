/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.storage.party

import com.cobblemon.mod.common.api.storage.party.PartyPosition
import com.cobblemon.mod.common.api.storage.party.PartyPosition.Companion.readPartyPosition
import com.cobblemon.mod.common.api.storage.party.PartyPosition.Companion.writePartyPosition
import com.cobblemon.mod.common.net.messages.PokemonDTO
import com.cobblemon.mod.common.net.messages.client.storage.SetPokemonPacket
import com.cobblemon.mod.common.pokemon.Pokemon
import java.util.UUID
import net.minecraft.network.PacketByteBuf

/**
 * Adds the given Pokémon to a specific location in the client storage. This should be a new
 * Pokémon that the client doesn't know about yet.
 *
 * Handled by [com.cobblemon.mod.common.client.net.storage.party.SetPartyPokemonHandler]
 *
 * @author Hiroku
 * @since November 29th, 2021
*/
class SetPartyPokemonPacket : SetPokemonPacket<PartyPosition> {
    constructor() {
        this.pokemon = PokemonDTO()
    }

    constructor(storeID: UUID, storePosition: PartyPosition, pokemon: Pokemon) {
        this.storeID = storeID
        this.storePosition = storePosition
        this.pokemon = PokemonDTO(pokemon, toClient = true)
    }

    override fun encodePosition(buffer: PacketByteBuf) = buffer.writePartyPosition(storePosition)
    override fun decodePosition(buffer: PacketByteBuf) = buffer.readPartyPosition()
}