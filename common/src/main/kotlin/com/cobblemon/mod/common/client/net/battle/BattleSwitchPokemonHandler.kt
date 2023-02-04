/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.battle

import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.battle.ClientBattlePokemon
import com.cobblemon.mod.common.client.battle.animations.MoveTileOffscreenAnimation
import com.cobblemon.mod.common.client.battle.animations.SwapAndMoveTileOnscreenAnimation
import com.cobblemon.mod.common.client.net.ClientPacketHandler
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonFloatingState
import com.cobblemon.mod.common.net.messages.client.battle.BattleSwitchPokemonPacket

object BattleSwitchPokemonHandler : ClientPacketHandler<BattleSwitchPokemonPacket> {
    override fun invokeOnClient(packet: BattleSwitchPokemonPacket, ctx: CobblemonNetwork.NetworkContext) {
        val battle = CobblemonClient.battle ?: return
        val (actor, activeBattlePokemon) = battle.getPokemonFromPNX(packet.pnx)

        val lastAnimation = activeBattlePokemon.animations.lastOrNull()
        if (lastAnimation !is MoveTileOffscreenAnimation) {
            activeBattlePokemon.animations.add(MoveTileOffscreenAnimation())
        }

        activeBattlePokemon.animations.add(
            SwapAndMoveTileOnscreenAnimation(
                with(packet.newPokemon) {
                    ClientBattlePokemon(
                        uuid = uuid,
                        displayName = displayName,
                        properties = properties,
                        aspects = aspects,
                        hpRatio = hpRatio,
                        status = status,
                        statChanges = statChanges
                    ).also {
                        it.actor = actor
                        it.state = PokemonFloatingState()
                    }
                }
            )
        )
    }
}