/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.storage.party

import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.storage.ReleasePokemonEvent
import com.cobblemon.mod.common.api.storage.pc.link.PCLinkManager
import com.cobblemon.mod.common.net.messages.server.storage.pc.ReleasePCPokemonPacket
import com.cobblemon.mod.common.net.serverhandling.ServerPacketHandler
import net.minecraft.server.network.ServerPlayerEntity

object ReleasePCPokemonHandler : ServerPacketHandler<ReleasePCPokemonPacket> {
    override fun invokeOnServer(packet: ReleasePCPokemonPacket, ctx: CobblemonNetwork.NetworkContext, player: ServerPlayerEntity) {
        val pc = PCLinkManager.getPC(player) ?: return
        val pokemon = pc[packet.position] ?: return
        if (pokemon.uuid != packet.pokemonID) {
            return // Not what we expected
        }

        CobblemonEvents.POKEMON_RELEASED_EVENT_PRE.postThen(
                event = ReleasePokemonEvent.Pre(player, pokemon, pc),
                ifSucceeded = { preEvent ->
                    pc.remove(packet.position)
                    CobblemonEvents.POKEMON_RELEASED_EVENT_POST.post(ReleasePokemonEvent.Post(player, pokemon, pc))
                },
                ifCanceled = { preEvent ->
                    pc[packet.position] = pokemon
                }
        )
    }
}