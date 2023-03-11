/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.pokemon.update

import com.cobblemon.mod.common.net.IntSize
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.readSizedInt
import com.cobblemon.mod.common.util.writeSizedInt
import net.minecraft.network.PacketByteBuf
class AspectsUpdatePacket() : SingleUpdatePacket<Set<String>>(emptySet()) {
    constructor(pokemon: Pokemon, aspects: Set<String>): this() {
        setTarget(pokemon)
        value = aspects
    }

    override fun encodeValue(buffer: PacketByteBuf, value: Set<String>) {
        buffer.writeSizedInt(IntSize.U_BYTE, value.size)
        value.forEach { buffer.writeString(it) }
    }

    override fun decodeValue(buffer: PacketByteBuf): Set<String> {
        val aspects = mutableSetOf<String>()
        repeat(times = buffer.readSizedInt(IntSize.U_BYTE)) {
            aspects.add(buffer.readString())
        }
        return aspects
    }

    override fun set(pokemon: Pokemon, value: Set<String>) {
        pokemon.aspects = value
    }
}