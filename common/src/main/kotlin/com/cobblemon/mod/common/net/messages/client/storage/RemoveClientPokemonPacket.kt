/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.storage

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.storage.PokemonStore
import com.cobblemon.mod.common.api.storage.party.PartyStore
import java.util.UUID
import net.minecraft.network.PacketByteBuf

/**
 * Removes a Pokémon from a particular store on the client side, working for both parties and PCs.
 *
 * Handled by [com.cobblemon.mod.common.client.net.storage.RemoveClientPokemonHandler]
 *
 * @author Hiroku
 * @since June 18th, 2022
 */
class RemoveClientPokemonPacket() : NetworkPacket {
    var storeIsParty = false
    lateinit var storeID: UUID
    lateinit var pokemonID: UUID

    constructor(store: PokemonStore<*>, pokemonID: UUID): this() {
        this.storeIsParty = store is PartyStore
        this.storeID = store.uuid
        this.pokemonID = pokemonID
    }

    override fun encode(buffer: PacketByteBuf) {
        buffer.writeBoolean(storeIsParty)
        buffer.writeUuid(storeID)
        buffer.writeUuid(pokemonID)
    }

    override fun decode(buffer: PacketByteBuf) {
        this.storeIsParty = buffer.readBoolean()
        this.storeID = buffer.readUuid()
        this.pokemonID = buffer.readUuid()
    }
}