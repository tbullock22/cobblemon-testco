/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.forge.net

import com.cobblemon.mod.common.Cobblemon.LOGGER
import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.NetworkDelegate
import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.net.PacketHandler
import com.cobblemon.mod.common.util.cobblemonResource
import java.util.function.Supplier
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraftforge.network.NetworkDirection
import net.minecraftforge.network.NetworkEvent
import net.minecraftforge.network.NetworkRegistry
import net.minecraftforge.network.PacketDistributor
import net.minecraftforge.network.simple.SimpleChannel

class PreparedForgeMessage<T : NetworkPacket>(private val messageBuilder: SimpleChannel.MessageBuilder<T>) : CobblemonNetwork.PreparedMessage<T> {
    override fun registerMessage() {
        messageBuilder.add()
    }

    override fun registerHandler(handler: PacketHandler<T>) {
        messageBuilder.consumer(SimpleChannel.MessageBuilder.ToBooleanBiFunction<T, Supplier<NetworkEvent.Context>> { packet, ctx ->
            handler(packet, ForgeNetworkContext(ctx.get()))
            return@ToBooleanBiFunction true
        })
    }
}
class ForgeNetworkContext(val ctx: NetworkEvent.Context) : CobblemonNetwork.NetworkContext {
    override val player = ctx.sender
}

object CobblemonForgeNetworkDelegate : NetworkDelegate {
    var discriminator = 0

    val channel = NetworkRegistry.newSimpleChannel(
        cobblemonResource("main"),
        { CobblemonNetwork.PROTOCOL_VERSION },
        CobblemonNetwork.PROTOCOL_VERSION::equals,
        CobblemonNetwork.PROTOCOL_VERSION::equals
    )

    override fun sendPacketToPlayer(player: ServerPlayerEntity, packet: NetworkPacket) {
        channel.send(PacketDistributor.PLAYER.with { player }, packet)
    }

    override fun sendPacketToServer(packet: NetworkPacket) {
        channel.sendToServer(packet)
    }

    override fun <T : NetworkPacket> buildMessage(
        packetClass: Class<T>,
        toServer: Boolean
    ): CobblemonNetwork.PreparedMessage<T> {
        return PreparedForgeMessage(
            channel.messageBuilder(
                packetClass,
                discriminator++,
                if (toServer) NetworkDirection.PLAY_TO_SERVER else NetworkDirection.PLAY_TO_CLIENT
            )
                .encoder { packet, buffer -> packet.encode(buffer) }
                .decoder { buffer ->
                    try {
                        packetClass.getDeclaredConstructor().newInstance().also { it.decode(buffer) }
                    } catch (exception: Exception) {
                        LOGGER.error("There was an exception while decoding a packet of type ${packetClass.simpleName}")
                        exception.printStackTrace()
                        throw exception
                    }
                }
        )
    }
}