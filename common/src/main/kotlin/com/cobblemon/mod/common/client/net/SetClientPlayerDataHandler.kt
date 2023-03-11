/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net

import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.starter.ClientPlayerData
import com.cobblemon.mod.common.net.messages.client.starter.SetClientPlayerDataPacket

object SetClientPlayerDataHandler : ClientPacketHandler<SetClientPlayerDataPacket> {
    override fun invokeOnClient(packet: SetClientPlayerDataPacket, ctx: CobblemonNetwork.NetworkContext) {
        CobblemonClient.clientPlayerData = ClientPlayerData(
            promptStarter = packet.promptStarter,
            starterLocked = packet.starterLocked,
            starterSelected = packet.starterSelected,
            starterUUID = packet.starterUUID
        )
    }
}