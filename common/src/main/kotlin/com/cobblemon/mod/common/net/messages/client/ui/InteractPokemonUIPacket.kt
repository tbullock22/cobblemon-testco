/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.ui

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.client.net.gui.InteractPokemonUIPacketHandler
import net.minecraft.network.PacketByteBuf
import java.util.UUID

/**
 * Tells the client to open the Pokémon interaction interface.
 *
 * Handled by [InteractPokemonUIPacketHandler].
 *
 * @author Village
 * @since January 7th, 2023
 */
class InteractPokemonUIPacket internal constructor(): NetworkPacket {
    constructor(pokemonID: UUID, canMountShoulder: Boolean) : this() {
        this.pokemonID = pokemonID
        this.canMountShoulder = canMountShoulder
    }

    lateinit var pokemonID: UUID
    var canMountShoulder: Boolean = false

    override fun encode(buffer: PacketByteBuf) {
        buffer.writeUuid(pokemonID)
        buffer.writeBoolean(canMountShoulder)
    }

    override fun decode(buffer: PacketByteBuf) {
        pokemonID = buffer.readUuid()
        canMountShoulder = buffer.readBoolean()
    }
}