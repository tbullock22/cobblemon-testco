/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net

import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.net.PacketHandler
import com.cobblemon.mod.common.util.ifClient
import net.minecraft.client.MinecraftClient


/*
 * A packet handler which will queue and safely execute the invocation on the physical client thread.
 *
 * @author Hiroku
 * @since November 28th, 2021
 */
interface ClientPacketHandler<T : NetworkPacket> : PacketHandler<T> {
    override fun invoke(packet: T, ctx: CobblemonNetwork.NetworkContext) {
        MinecraftClient.getInstance().submit { ifClient { invokeOnClient(packet, ctx) } }
    }

    fun invokeOnClient(packet: T, ctx: CobblemonNetwork.NetworkContext)
}