/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.storage.pc

import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.api.storage.pc.link.PCLinkManager
import com.cobblemon.mod.common.client.settings.ServerSettings
import com.cobblemon.mod.common.net.messages.server.storage.party.ReleasePartyPokemonPacket
import com.cobblemon.mod.common.net.messages.server.storage.pc.UnlinkPlayerFromPCPacket
import com.cobblemon.mod.common.net.serverhandling.ServerPacketHandler
import com.cobblemon.mod.common.util.party
import net.minecraft.server.network.ServerPlayerEntity

object UnlinkPlayerFromPCHandler : ServerPacketHandler<UnlinkPlayerFromPCPacket> {
    override fun invokeOnServer(packet: UnlinkPlayerFromPCPacket, ctx: CobblemonNetwork.NetworkContext, player: ServerPlayerEntity) {
        PCLinkManager.removeLink(player.uuid)
    }
}