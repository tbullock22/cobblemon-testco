/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.data

import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.data.CobblemonDataProvider
import com.cobblemon.mod.common.net.PacketHandler
import com.cobblemon.mod.common.net.messages.client.data.UnlockReloadPacket

internal object UnlockReloadPacketHandler : PacketHandler<UnlockReloadPacket> {
    override fun invoke(packet: UnlockReloadPacket, ctx: CobblemonNetwork.NetworkContext) {
        CobblemonDataProvider.canReload = true
    }
}