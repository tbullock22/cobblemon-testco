/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.evolution

import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.net.PacketHandler
import com.cobblemon.mod.common.net.messages.server.pokemon.update.evolution.EvolutionDisplayUpdatePacket
import com.cobblemon.mod.common.util.party
import com.cobblemon.mod.common.util.runOnServer
class EvolutionDisplayUpdatePacketHandler<T : EvolutionDisplayUpdatePacket> : PacketHandler<T> {

    override fun invoke(packet: T, ctx: CobblemonNetwork.NetworkContext) {
        runOnServer {
            // We only accept party evolutions
            ctx.player?.party()?.get(packet.pokemonID)?.let { pokemon -> packet.applyToPokemon(pokemon) }
        }
    }

}