/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.forge.net

import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.NetworkManager
import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.util.cobblemonResource
import kotlin.reflect.KClass
import net.minecraft.network.packet.Packet
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraftforge.network.NetworkDirection
import net.minecraftforge.network.NetworkRegistry
import net.minecraftforge.network.PacketDistributor


object CobblemonForgeNetworkManager : NetworkManager {

    private const val PROTOCOL_VERSION = "1"
    private var id = 0

    private val channel = NetworkRegistry.newSimpleChannel(
        cobblemonResource("main"),
        { PROTOCOL_VERSION },
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    )

    override fun registerClientBound() {
        CobblemonNetwork.registerClientBound()
    }

    override fun registerServerBound() {
        CobblemonNetwork.registerServerBound()
    }

    @Suppress("INACCESSIBLE_TYPE")
    override fun <T : NetworkPacket<T>> createClientBound(
        identifier: Identifier,
        kClass: KClass<T>,
        encoder: (T, PacketByteBuf) -> Unit,
        decoder: (PacketByteBuf) -> T,
        handler: ClientNetworkPacketHandler<T>
    ) {
        this.channel.registerMessage(this.id++, kClass.java, encoder::invoke, decoder::invoke) { msg, ctx ->
            val context = ctx.get()
            handler.handleOnNettyThread(msg)
            context.packetHandled = true
        }
    }

    @Suppress("INACCESSIBLE_TYPE")
    override fun <T : NetworkPacket<T>> createServerBound(
        identifier: Identifier,
        kClass: KClass<T>,
        encoder: (T, PacketByteBuf) -> Unit,
        decoder: (PacketByteBuf) -> T,
        handler: ServerNetworkPacketHandler<T>
    ) {
        this.channel.registerMessage(this.id++, kClass.java, encoder::invoke, decoder::invoke) { msg, ctx ->
            val context = ctx.get()
            handler.handleOnNettyThread(msg, context.sender!!.server, context.sender!!)
            context.packetHandled = true
        }
    }

    override fun sendPacketToPlayer(player: ServerPlayerEntity, packet: NetworkPacket<*>) {
        this.channel.send(PacketDistributor.PLAYER.with { player }, packet)
    }

    override fun sendPacketToServer(packet: NetworkPacket<*>) {
        this.channel.sendToServer(packet)
    }

    override fun <T : NetworkPacket<*>> asVanillaClientBound(packet: T): Packet<ClientPlayPacketListener> {
        return this.channel.toVanillaPacket(packet, NetworkDirection.PLAY_TO_CLIENT) as Packet<ClientPlayPacketListener>
    }
}