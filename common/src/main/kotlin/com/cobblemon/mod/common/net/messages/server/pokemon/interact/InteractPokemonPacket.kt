/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.server.pokemon.interact

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.net.serverhandling.pokemon.interact.InteractPokemonHandler
import java.util.UUID
import net.minecraft.network.PacketByteBuf

/**
 * Tells the server to handle Pokémon interaction.
 *
 * Handled by [InteractPokemonHandler].
 *
 * @author Village
 * @since January 7th, 2023
 */
class InteractPokemonPacket() : NetworkPacket {
    lateinit var pokemonID: UUID
    var mountShoulder: Boolean = false

    constructor(pokemonID: UUID, mountShoulder: Boolean): this() {
        this.pokemonID = pokemonID
        this.mountShoulder = mountShoulder
    }

    override fun encode(buffer: PacketByteBuf) {
        buffer.writeUuid(pokemonID)
        buffer.writeBoolean(mountShoulder)
    }

    override fun decode(buffer: PacketByteBuf) {
        pokemonID = buffer.readUuid()
        mountShoulder = buffer.readBoolean()
    }
}