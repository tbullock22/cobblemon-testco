/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.gui

import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.client.gui.interact.pokemon.PokemonInteractGUI
import com.cobblemon.mod.common.client.net.ClientPacketHandler
import com.cobblemon.mod.common.net.messages.client.ui.InteractPokemonUIPacket
import net.minecraft.client.MinecraftClient

object InteractPokemonUIPacketHandler: ClientPacketHandler<InteractPokemonUIPacket> {
    override fun invokeOnClient(packet: InteractPokemonUIPacket, ctx: CobblemonNetwork.NetworkContext) {
        MinecraftClient.getInstance().setScreen(
            PokemonInteractGUI(
                pokemonID = packet.pokemonID,
                canMountShoulder = packet.canMountShoulder
            )
        )
    }
}