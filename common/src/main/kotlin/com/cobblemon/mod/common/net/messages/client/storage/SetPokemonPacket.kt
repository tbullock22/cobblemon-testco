/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.storage

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.storage.StorePosition
import com.cobblemon.mod.common.net.messages.PokemonDTO
import com.cobblemon.mod.common.pokemon.Pokemon
import java.util.UUID
import net.minecraft.network.PacketByteBuf

/**
 * Base packet class for creating a new Pokémon in one of the client's stores. This
 * is for when the Pokémon is currently unrecognized by that specific store on the
 * client. Implementations target specific store types.
 *
 * @author Hiroku
 * @since November 29th, 2021
 */
abstract class SetPokemonPacket<T : StorePosition> : NetworkPacket {
    lateinit var pokemon: PokemonDTO
    var storeID = UUID.randomUUID()
    lateinit var storePosition: T

    abstract fun encodePosition(buffer: PacketByteBuf)
    override fun encode(buffer: PacketByteBuf) {
        pokemon.encode(buffer)
        buffer.writeUuid(storeID)
        encodePosition(buffer)
    }

    abstract fun decodePosition(buffer: PacketByteBuf): T
    override fun decode(buffer: PacketByteBuf) {
        pokemon.decode(buffer)
        storeID = buffer.readUuid()
        storePosition = decodePosition(buffer)
    }
}