/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.starter

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.net.messages.server.starter.RequestStarterScreenPacket
import com.cobblemon.mod.common.net.serverhandling.ServerPacketHandler
import com.cobblemon.mod.common.util.lang
import net.minecraft.server.network.ServerPlayerEntity

object RequestStarterScreenHandler : ServerPacketHandler<RequestStarterScreenPacket> {
    override fun invokeOnServer(packet: RequestStarterScreenPacket, ctx: CobblemonNetwork.NetworkContext, player: ServerPlayerEntity) {
        val playerData = Cobblemon.playerData.get(player)

        if (playerData.starterSelected) {
            return player.sendMessage(lang("ui.starter.alreadyselected").red())
        } else if (playerData.starterLocked) {
            return player.sendMessage(lang("ui.starter.cannotchoose").red())
        } else {
            Cobblemon.starterHandler.requestStarterChoice(player)
        }
    }
}