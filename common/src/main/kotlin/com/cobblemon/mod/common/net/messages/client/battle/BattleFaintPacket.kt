/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.battle

import com.cobblemon.mod.common.api.net.NetworkPacket
import net.minecraft.network.PacketByteBuf
import net.minecraft.text.MutableText

/**
 * Reports that a specific Pokémon fainted. This triggers a specific tile animation.
 *
 * Handled by [com.cobblemon.mod.common.client.net.battle.BattleFaintHandler].
 *
 * @author Hiroku
 * @since May 22nd, 2022
 */
class BattleFaintPacket() : NetworkPacket {
    lateinit var pnx: String
    lateinit var message: MutableText
    constructor(pnx: String, message: MutableText): this() {
        this.pnx = pnx
        this.message = message
    }
    override fun encode(buffer: PacketByteBuf) {
        buffer.writeString(pnx)
        buffer.writeText(message)
    }

    override fun decode(buffer: PacketByteBuf) {
        pnx = buffer.readString()
        message = buffer.readText().copy()
    }
}