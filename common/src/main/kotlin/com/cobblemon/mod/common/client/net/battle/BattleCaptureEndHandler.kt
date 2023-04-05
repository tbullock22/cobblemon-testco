/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.battle

import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.api.scheduling.after
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.battle.animations.MoveTileOffscreenAnimation
import com.cobblemon.mod.common.client.net.ClientPacketHandler
import com.cobblemon.mod.common.net.messages.client.battle.BattleCaptureEndPacket

object BattleCaptureEndHandler : ClientPacketHandler<BattleCaptureEndPacket> {
    override fun invokeOnClient(packet: BattleCaptureEndPacket, ctx: CobblemonNetwork.NetworkContext) {
        val battle = CobblemonClient.battle ?: return
        val (_, activeBattlePokemon) = battle.getPokemonFromPNX(packet.targetPNX)
        if (packet.succeeded) {
            activeBattlePokemon.animations.add(MoveTileOffscreenAnimation().also { after(seconds = it.duration) { activeBattlePokemon.ballCapturing = null } })
        } else {
            activeBattlePokemon.ballCapturing?.finish()
        }
        activeBattlePokemon.ballCapturing = null
    }
}