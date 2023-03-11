/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.storage.pc

import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.client.net.ClientPacketHandler
import com.cobblemon.mod.common.net.messages.client.storage.pc.ClosePCPacket

object ClosePCHandler : ClientPacketHandler<ClosePCPacket> {
    override fun invokeOnClient(packet: ClosePCPacket, ctx: CobblemonNetwork.NetworkContext) {
        // TODO close the PC GUI if the UUID of the opened PC matches packet.storeID
    }
}