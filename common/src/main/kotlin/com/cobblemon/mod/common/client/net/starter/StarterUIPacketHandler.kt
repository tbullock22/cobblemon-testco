/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.starter

import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.gui.startselection.StarterSelectionScreen
import com.cobblemon.mod.common.client.net.ClientPacketHandler
import com.cobblemon.mod.common.net.messages.client.starter.OpenStarterUIPacket
import net.minecraft.client.MinecraftClient

object StarterUIPacketHandler : ClientPacketHandler<OpenStarterUIPacket> {
    override fun invokeOnClient(packet: OpenStarterUIPacket, ctx: CobblemonNetwork.NetworkContext) {
        CobblemonClient.checkedStarterScreen = true
        MinecraftClient.getInstance().setScreen(StarterSelectionScreen(categories = packet.categories))
    }
}