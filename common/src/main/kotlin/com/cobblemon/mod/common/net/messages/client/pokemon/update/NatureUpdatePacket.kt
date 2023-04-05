/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.pokemon.update

import com.cobblemon.mod.common.Cobblemon.LOGGER
import com.cobblemon.mod.common.api.pokemon.Natures
import com.cobblemon.mod.common.pokemon.Nature
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier
import net.minecraft.util.InvalidIdentifierException

class NatureUpdatePacket(
    private var minted : Boolean = false
) : SingleUpdatePacket<Nature?>(null) {
    constructor(pokemon: Pokemon, value: Nature?, minted: Boolean): this() {
        this.setTarget(pokemon)
        this.value = value
        this.minted = minted
    }

    override fun set(pokemon: Pokemon, value: Nature?) {
        // Check for removing mint
        if (minted && value == null) {
            pokemon.mintedNature = null
            return
        }

        try {
            // Validate the nature locally
            if (value == null) {
                LOGGER.warn("A null nature was attempted to be put onto: '$pokemon'")
                return
            }

            // Check which nature to modify
            if (!minted) {
                pokemon.nature = value
            } else {
                pokemon.mintedNature = value
            }
        } catch (e: InvalidIdentifierException) {
            // This should never happen
            LOGGER.error("Failed to resolve nature value in NatureUpdatePacket", e)
        }
    }

    override fun encodeValue(buffer: PacketByteBuf, value: Nature?) {
        buffer.writeNullable(value) { _, v -> buffer.writeIdentifier(v.name) }
        buffer.writeBoolean(minted)
    }

    override fun decodeValue(buffer: PacketByteBuf): Nature? {
        this.value = buffer.readNullable { Natures.getNature(buffer.readIdentifier()) }
        this.minted = buffer.readBoolean()

        return this.value
    }
}