/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.battle

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier

class BattleTransformPacket(val transformerPnx: String, val species: Species, val aspects: Set<String>) : NetworkPacket<BattleTransformPacket> {

    override val id: Identifier = ID

    override fun encode(buffer: PacketByteBuf) {
        buffer.writeString(this.transformerPnx)
        buffer.writeIdentifier(this.species.resourceIdentifier)
        buffer.writeCollection(this.aspects, PacketByteBuf::writeString)
    }

    companion object {
        val ID = cobblemonResource("battle_transform")
        fun decode(buffer: PacketByteBuf) = BattleTransformPacket(buffer.readString(), PokemonSpecies.getByIdentifier(buffer.readIdentifier())!!, buffer.readList(PacketByteBuf::readString).toSet())
    }
}