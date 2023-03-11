/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.storage

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonNetwork.NetworkContext
import com.cobblemon.mod.common.api.storage.pc.link.PCLinkManager
import com.cobblemon.mod.common.net.messages.client.storage.pc.ClosePCPacket
import com.cobblemon.mod.common.net.messages.server.storage.SwapPCPartyPokemonPacket
import com.cobblemon.mod.common.net.serverhandling.ServerPacketHandler
import net.minecraft.server.network.ServerPlayerEntity

object SwapPCPartyPokemonHandler : ServerPacketHandler<SwapPCPartyPokemonPacket> {
    override fun invokeOnServer(packet: SwapPCPartyPokemonPacket, ctx: NetworkContext, player: ServerPlayerEntity) {
        val party = Cobblemon.storage.getParty(player)
        val pc = PCLinkManager.getPC(player) ?: return run { ClosePCPacket().sendToPlayer(player) }

        val partyPokemon = party[packet.partyPosition] ?: return
        val pcPokemon = pc[packet.pcPosition] ?: return

        if (partyPokemon.uuid != packet.partyPokemonID || pcPokemon.uuid != packet.pcPokemonID) {
            return
        }

        party[packet.partyPosition] = pcPokemon
        pc[packet.pcPosition] = partyPokemon
    }
}