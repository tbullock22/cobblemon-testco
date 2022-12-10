/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.battle

import com.cobblemon.mod.common.api.net.NetworkPacket
import net.minecraft.network.PacketByteBuf

/**
 * Informs the client that a Pokémon's health has changed. Executes a tile animation.
 *
 * Handled by [com.cobblemon.mod.common.client.net.battle.BattleHealthChangeHandler].
 *
 * @author Hiroku
 * @since June 5th, 2022
 */
class BattleHealthChangePacket() : NetworkPacket {
    lateinit var pnx: String
    var newHealthRatio = 0F
    var newHealth = 0

    constructor(pnx: String, newHealthRatio: Float, newHealth: Int): this() {
        this.pnx = pnx
        this.newHealthRatio = newHealthRatio
        this.newHealth = newHealth
    }

    override fun encode(buffer: PacketByteBuf) {
        buffer.writeString(pnx)
        buffer.writeFloat(newHealthRatio)
        buffer.writeInt(newHealth)
    }

    override fun decode(buffer: PacketByteBuf) {
        pnx = buffer.readString()
        newHealthRatio = buffer.readFloat()
        newHealth = buffer.readInt()
    }
}