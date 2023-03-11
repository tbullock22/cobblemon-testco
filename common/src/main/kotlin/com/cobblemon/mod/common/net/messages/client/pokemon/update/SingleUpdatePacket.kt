/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.pokemon.update

import com.cobblemon.mod.common.net.messages.client.PokemonUpdatePacket
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.network.PacketByteBuf

/**
 * Base class for packets which update a single value of a Pokémon.
 *
 * Handled by [com.cobblemon.mod.client.net.pokemon.update.SingleUpdatePacketHandler]
 *
 * @author Hiroku
 * @since November 28th, 2021
 */
abstract class SingleUpdatePacket<T>(var value: T) : PokemonUpdatePacket() {
    override fun encode(buffer: PacketByteBuf) {
        super.encode(buffer)
        encodeValue(buffer, value)
    }

    override fun decode(buffer: PacketByteBuf) {
        super.decode(buffer)
        value = decodeValue(buffer)
    }

    override fun applyToPokemon(pokemon: Pokemon) {
        set(pokemon, value)
    }

    abstract fun encodeValue(buffer: PacketByteBuf, value: T)
    abstract fun decodeValue(buffer: PacketByteBuf): T

    /** Sets the value in the client-side Pokémon. */
    abstract fun set(pokemon: Pokemon, value: T)
}