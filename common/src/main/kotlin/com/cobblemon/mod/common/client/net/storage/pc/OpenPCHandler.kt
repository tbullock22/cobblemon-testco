/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.storage.pc

import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.gui.pc.PCGUI
import com.cobblemon.mod.common.client.net.ClientPacketHandler
import com.cobblemon.mod.common.net.messages.client.storage.pc.OpenPCPacket
import net.minecraft.client.MinecraftClient

object OpenPCHandler : ClientPacketHandler<OpenPCPacket> {
    override fun invokeOnClient(packet: OpenPCPacket, ctx: CobblemonNetwork.NetworkContext) {
        val pc = CobblemonClient.storage.pcStores[packet.storeID] ?: return
        MinecraftClient.getInstance().setScreen(PCGUI(pc, CobblemonClient.storage.myParty))
    }
}