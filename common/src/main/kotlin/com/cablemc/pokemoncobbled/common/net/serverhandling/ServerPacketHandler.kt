/*
 * Copyright (C) 2022 Pokemon Cobbled Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cablemc.pokemoncobbled.common.net.serverhandling

import com.cablemc.pokemoncobbled.common.CobbledNetwork.NetworkContext
import com.cablemc.pokemoncobbled.common.api.net.NetworkPacket
import com.cablemc.pokemoncobbled.common.net.PacketHandler
import com.cablemc.pokemoncobbled.common.util.runOnServer
import net.minecraft.server.network.ServerPlayerEntity

/*
 * A packet handler which will queue and safely execute the invocation on the main server thread.
 *
 * @author Hiroku
 * @since June 20th, 2022
 */
interface ServerPacketHandler<T : NetworkPacket> : PacketHandler<T> {
    override fun invoke(packet: T, ctx: NetworkContext) {
        runOnServer { invokeOnServer(packet, ctx, ctx.player ?: return@runOnServer) }
    }

    fun invokeOnServer(packet: T, ctx: NetworkContext, player: ServerPlayerEntity)
}