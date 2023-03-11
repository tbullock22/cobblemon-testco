/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.storage

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.net.messages.server.RequestMoveSwapPacket
import com.cobblemon.mod.common.net.serverhandling.ServerPacketHandler
import net.minecraft.server.network.ServerPlayerEntity

object RequestMoveSwapHandler: ServerPacketHandler<RequestMoveSwapPacket> {
    override fun invokeOnServer(packet: RequestMoveSwapPacket, ctx: CobblemonNetwork.NetworkContext, player: ServerPlayerEntity) {
        val pokemon = Cobblemon.storage.getParty(player).get(packet.slot) ?: return
        val move1 = pokemon.moveSet[packet.move1] ?: return
        val move2 = pokemon.moveSet[packet.move2] ?: return
        if (move1 == move2) {
            return
        }
        pokemon.moveSet.swapMove(packet.move1, packet.move2)
    }
}