/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.net

import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.util.getServer
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.registry.RegistryKey
import net.minecraft.world.World

/*
 * Simple packet interface to make a more traditional layout for netcode
 *
 * @author Hiroku
 * @since November 27th, 2021
 */
interface NetworkPacket {
    fun encode(buffer: PacketByteBuf)
    fun decode(buffer: PacketByteBuf)

    fun sendToPlayer(player: ServerPlayerEntity) = CobblemonNetwork.sendToPlayer(player, this)
    fun sendToPlayers(players: Iterable<ServerPlayerEntity>) {
        if (players.any()) {
            CobblemonNetwork.sendToPlayers(players, this)
        }
    }
    fun sendToAllPlayers() = CobblemonNetwork.sendToAllPlayers(this)
    fun sendToServer() = CobblemonNetwork.sendToServer(this)
    // A copy from PlayerManager#sendToAround to work with our packets
    fun sendToPlayersAround(x: Double, y: Double, z: Double, distance: Double, worldKey: RegistryKey<World>, exclusionCondition: (ServerPlayerEntity) -> Boolean = { false }) {
        val server = getServer() ?: return
        server.playerManager.playerList.filter { player ->
            if (exclusionCondition.invoke(player))
                return@filter false
            val xDiff = x - player.x
            val yDiff = y - player.y
            val zDiff = z - player.z
            return@filter (xDiff * xDiff + yDiff * yDiff + zDiff) < distance * distance
        }
        .forEach { player -> CobblemonNetwork.sendToPlayer(player, this) }
    }
}