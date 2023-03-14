/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.battle

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.battle.ClientBattlePokemon
import com.cobblemon.mod.common.net.messages.client.battle.BattleTransformPacket
import net.minecraft.client.MinecraftClient

object BattleTransformPacketHandler : ClientNetworkPacketHandler<BattleTransformPacket> {

    override fun handle(packet: BattleTransformPacket, client: MinecraftClient) {
        client.execute {
            val battle = CobblemonClient.battle ?: return@execute
            val (_, activePokemon) = battle.getPokemonFromPNX(packet.transformerPnx)
            activePokemon.battlePokemon?.transformation = ClientBattlePokemon.Transformation(packet.species, packet.aspects)
        }
    }

}