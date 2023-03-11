/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.pokemon.update

import net.minecraft.network.PacketByteBuf

/**
 * A specific type of update for a Pokémon which updates a single string value
 *
 * @author Deltric
 * @since January 13th, 2022
 */
abstract class StringUpdatePacket : SingleUpdatePacket<String>("") {
    override fun encodeValue(buffer: PacketByteBuf, value: String) {
        buffer.writeString(value)
    }

    override fun decodeValue(buffer: PacketByteBuf): String {
        return buffer.readString()
    }
}