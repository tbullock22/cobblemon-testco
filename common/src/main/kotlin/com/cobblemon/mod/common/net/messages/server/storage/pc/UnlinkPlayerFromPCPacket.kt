/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.server.storage.pc

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.net.serverhandling.storage.pc.UnlinkPlayerFromPCHandler
import net.minecraft.network.PacketByteBuf

/**
 * Packet sent to remove the PC link for the player.
 *
 * Handled by [UnlinkPlayerFromPCHandler].
 *
 * @author Village
 * @since January 18th, 2023
 */
class UnlinkPlayerFromPCPacket() : NetworkPacket {

    override fun encode(buffer: PacketByteBuf) {}

    override fun decode(buffer: PacketByteBuf) {}
}