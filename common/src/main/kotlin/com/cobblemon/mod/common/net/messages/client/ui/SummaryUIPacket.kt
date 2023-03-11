/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.ui

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.net.messages.PokemonDTO
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.network.PacketByteBuf
class SummaryUIPacket internal constructor(): NetworkPacket {
    constructor(vararg pokemon: Pokemon, editable: Boolean = true) : this() {
        pokemonArray.addAll(pokemon.map { PokemonDTO(it, toClient = true) })
        this.editable = editable
    }

    val pokemonArray = mutableListOf<PokemonDTO>()
    var editable = true

    override fun encode(buffer: PacketByteBuf) {
        buffer.writeBoolean(editable)
        buffer.writeInt(pokemonArray.size)
        pokemonArray.forEach {
            it.encode(buffer)
        }
    }

    override fun decode(buffer: PacketByteBuf) {
        editable = buffer.readBoolean()
        val amount = buffer.readInt()
        for (i in 0 until amount) {
            pokemonArray.add(PokemonDTO().also { it.decode(buffer) })
        }
    }
}