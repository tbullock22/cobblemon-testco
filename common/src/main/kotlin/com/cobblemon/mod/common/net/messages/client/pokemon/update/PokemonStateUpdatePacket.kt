/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.pokemon.update

import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.activestate.InactivePokemonState
import com.cobblemon.mod.common.pokemon.activestate.PokemonState
import net.minecraft.network.PacketByteBuf
class PokemonStateUpdatePacket(): SingleUpdatePacket<PokemonState>(InactivePokemonState()) {
    constructor(pokemon: Pokemon, state: PokemonState): this() {
        setTarget(pokemon)
        value = state
    }
    override fun encodeValue(buffer: PacketByteBuf, value: PokemonState) { value.writeToBuffer(buffer) }
    override fun decodeValue(buffer: PacketByteBuf) = PokemonState.fromBuffer(buffer)
    override fun set(pokemon: Pokemon, value: PokemonState) { pokemon.state = value }
}