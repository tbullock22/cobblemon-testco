/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.gui

import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.client.gui.summary.Summary
import com.cobblemon.mod.common.client.net.ClientPacketHandler
import com.cobblemon.mod.common.net.messages.client.ui.SummaryUIPacket
import net.minecraft.client.MinecraftClient

object SummaryUIPacketHandler: ClientPacketHandler<SummaryUIPacket> {
    override fun invokeOnClient(packet: SummaryUIPacket, ctx: CobblemonNetwork.NetworkContext) {
        MinecraftClient.getInstance().setScreen(
            Summary(
                pokemon = packet.pokemonArray.map { it.create() }.toTypedArray(),
                editable = packet.editable
            )
        )
    }
}