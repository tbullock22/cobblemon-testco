/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.battle

import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.battle.SingleActionRequest
import com.cobblemon.mod.common.client.net.ClientPacketHandler
import com.cobblemon.mod.common.net.messages.client.battle.BattleQueueRequestPacket
import net.minecraft.client.MinecraftClient

object BattleQueueRequestHandler : ClientPacketHandler<BattleQueueRequestPacket> {
    override fun invokeOnClient(packet: BattleQueueRequestPacket, ctx: CobblemonNetwork.NetworkContext) {
        val battle = CobblemonClient.battle ?: return
        val actor = battle.side1.actors.find { it.uuid == MinecraftClient.getInstance().player?.uuid } ?: return
        CobblemonClient.battle?.pendingActionRequests = SingleActionRequest.composeFrom(actor, packet.request)
    }
}