/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.storage.pc

import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.net.ClientPacketHandler
import com.cobblemon.mod.common.client.storage.ClientBox
import com.cobblemon.mod.common.net.messages.client.storage.pc.SetPCBoxPokemonPacket

object SetPCBoxPokemonHandler : ClientPacketHandler<SetPCBoxPokemonPacket> {
    override fun invokeOnClient(packet: SetPCBoxPokemonPacket, ctx: CobblemonNetwork.NetworkContext) {
        CobblemonClient.storage.pcStores[packet.storeID]?.let { pc ->
            val boxNumber = packet.boxNumber
            while (pc.boxes.size <= boxNumber) { pc.boxes.add(ClientBox()) }
            pc.boxes[boxNumber] = ClientBox()
            packet.pokemon.forEach { (slot, pokemon) -> pc.boxes[packet.boxNumber].slots[slot] = pokemon.create() }
        }
    }
}