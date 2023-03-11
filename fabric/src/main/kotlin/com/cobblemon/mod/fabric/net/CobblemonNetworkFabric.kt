/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.fabric.net

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.NetworkDelegate
import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.net.PacketHandler
import com.cobblemon.mod.common.util.cobblemonResource
import io.netty.buffer.Unpooled
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.network.ServerPlayerEntity

abstract class PreparedFabricMessage<T : NetworkPacket>(protected val registeredMessage: RegisteredMessage<T>) : CobblemonNetwork.PreparedMessage<T> {
    override fun registerMessage() {}
}
class PreparedClientBoundFabricMessage<T : NetworkPacket>(registeredMessage: RegisteredMessage<T>) : PreparedFabricMessage<T>(registeredMessage) {
    override fun registerHandler(handler: PacketHandler<T>) {
        ClientPlayNetworking.registerGlobalReceiver(
            registeredMessage.identifier
        ) { _, _, buf, _ ->
            val packet = registeredMessage.packetClass.getDeclaredConstructor().newInstance().also { it.decode(buf) }
            val context = FabricClientNetworkContext()
            handler(packet, context)
        }
    }
}
class PreparedServerBoundFabricMessage<T : NetworkPacket>(registeredMessage: RegisteredMessage<T>) : PreparedFabricMessage<T>(registeredMessage) {
    override fun registerHandler(handler: PacketHandler<T>) {
        ServerPlayNetworking.registerGlobalReceiver(
            registeredMessage.identifier
        ) { _, player, _, buf, _ ->
            try {
                val packet = registeredMessage.packetClass.getDeclaredConstructor().newInstance().also { it.decode(buf) }
                val context = FabricServerNetworkContext(player)
                handler(packet, context)
            } catch (exception: Exception) {
                Cobblemon.LOGGER.error("There was an exception while decoding a packet of type ${registeredMessage.packetClass.simpleName}")
                exception.printStackTrace()
                throw exception
            }
        }
    }
}
class FabricServerNetworkContext(override val player: ServerPlayerEntity) : CobblemonNetwork.NetworkContext
class FabricClientNetworkContext(override val player: ServerPlayerEntity? = null) : CobblemonNetwork.NetworkContext
class RegisteredMessage<T : NetworkPacket>(
    val packetClass: Class<T>
) {
    val identifier = cobblemonResource(packetClass.simpleName.lowercase())
    fun encode(packet: NetworkPacket): PacketByteBuf? {
        if (packetClass.isInstance(packet)) {
            return PacketByteBuf(Unpooled.buffer()).also { packet.encode(it) }
        }
        return null
    }
}

object CobblemonFabricNetworkDelegate : NetworkDelegate {
    val registeredMessages = mutableListOf<RegisteredMessage<*>>()

    override fun sendPacketToPlayer(player: ServerPlayerEntity, packet: NetworkPacket) {
        val registeredMessage = registeredMessages.find { it.packetClass.isInstance(packet) }
            ?: throw IllegalStateException("There was no registered message for packet of type ${packet::class.simpleName}!")
        ServerPlayNetworking.send(player, registeredMessage.identifier, registeredMessage.encode(packet))
    }

    override fun sendPacketToServer(packet: NetworkPacket) {
        val registeredMessage = registeredMessages.find { it.packetClass.isInstance(packet) }
            ?: throw IllegalStateException("There was no registered message for packet of type ${packet::class.simpleName}!")
        ClientPlayNetworking.send(registeredMessage.identifier, registeredMessage.encode(packet))
    }

    override fun <T : NetworkPacket> buildMessage(
        packetClass: Class<T>,
        toServer: Boolean
    ): CobblemonNetwork.PreparedMessage<T> {
        val registeredMessage = RegisteredMessage(packetClass)
        registeredMessages.add(registeredMessage)

        return if (toServer) {
            PreparedServerBoundFabricMessage(registeredMessage)
        } else {
            PreparedClientBoundFabricMessage(registeredMessage)
        }
    }
}