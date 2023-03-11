/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.storage

import com.cobblemon.mod.common.Cobblemon.LOGGER
import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.api.moves.Moves
import com.cobblemon.mod.common.api.storage.PokemonStore
import com.cobblemon.mod.common.api.storage.pc.link.PCLinkManager
import com.cobblemon.mod.common.net.messages.client.storage.pc.ClosePCPacket
import com.cobblemon.mod.common.net.messages.server.BenchMovePacket
import com.cobblemon.mod.common.net.serverhandling.ServerPacketHandler
import com.cobblemon.mod.common.util.party
import net.minecraft.server.network.ServerPlayerEntity

object BenchMoveHandler : ServerPacketHandler<BenchMovePacket> {
    override fun invokeOnServer(packet: BenchMovePacket, ctx: CobblemonNetwork.NetworkContext, player: ServerPlayerEntity) {
        val pokemonStore: PokemonStore<*> = if (packet.isParty) {
            player.party()
        } else {
            PCLinkManager.getPC(player) ?: return run { ClosePCPacket().sendToPlayer(player) }
        }

        val pokemon = pokemonStore[packet.uuid] ?: return
        val oldMove = Moves.getByName(packet.oldMove) ?: return
        val newMove = Moves.getByName(packet.newMove) ?: return

        if (pokemon.moveSet.none { it.template == oldMove } || pokemon.moveSet.any { it.template == newMove }) {
            // Something inconsistent in the information they're sending, better give them an update on their moveset
            // in case they're just out of date somehow.
            pokemon.moveSet.update()
            return
        }

        if (newMove !in pokemon.allAccessibleMoves) {
            LOGGER.warn("${player.name} tried to bench ${oldMove.name} for ${newMove.name} but it doesn't have ${newMove.name} learned. Could be a hacker!")
            return
        }

        pokemon.exchangeMove(oldMove, newMove)
    }
}