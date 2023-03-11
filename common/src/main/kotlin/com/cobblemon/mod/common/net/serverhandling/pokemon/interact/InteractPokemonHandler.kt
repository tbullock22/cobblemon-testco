/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.pokemon.interact

import com.cobblemon.mod.common.CobblemonNetwork.NetworkContext
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.server.pokemon.interact.InteractPokemonPacket
import com.cobblemon.mod.common.net.serverhandling.ServerPacketHandler
import net.minecraft.server.network.ServerPlayerEntity

object InteractPokemonHandler : ServerPacketHandler<InteractPokemonPacket> {
    override fun invokeOnServer(packet: InteractPokemonPacket, ctx: NetworkContext, player: ServerPlayerEntity) {
        val pokemonEntity = player.getWorld().getEntity(packet.pokemonID);
        if (pokemonEntity is PokemonEntity) {
            if (packet.mountShoulder) {
                if (!pokemonEntity.isReadyToSitOnPlayer) {
                    return
                }
                pokemonEntity.tryMountingShoulder(player)
            } else {
                pokemonEntity.offerHeldItem(player, player.mainHandStack)
            }
        }
    }
}