/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.gui

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.client.gui.summary.Summary
import com.cobblemon.mod.common.client.net.ClientPacketHandler
import com.cobblemon.mod.common.net.messages.client.ui.SummaryUIPacket

object SummaryUIPacketHandler: ClientPacketHandler<SummaryUIPacket> {
    override fun invokeOnClient(packet: SummaryUIPacket, ctx: CobblemonNetwork.NetworkContext) {
        try {
            Summary.open(packet.pokemonArray.map { it.create() }, packet.editable, 0)
        } catch (e: Exception) {
            Cobblemon.LOGGER.debug("Failed to open the summary from the SummaryUI packet handler", e)
        }
    }
}