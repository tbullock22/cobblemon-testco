/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.sound

import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.client.net.ClientPacketHandler
import com.cobblemon.mod.common.net.messages.client.sound.UnvalidatedPlaySoundS2CPacket
import net.minecraft.client.MinecraftClient
import net.minecraft.sound.SoundEvent

internal object UnvalidatedPlaySoundS2CPacketHandler : ClientPacketHandler<UnvalidatedPlaySoundS2CPacket> {
    override fun invokeOnClient(packet: UnvalidatedPlaySoundS2CPacket, ctx: CobblemonNetwork.NetworkContext) {
        val client = MinecraftClient.getInstance()
        client.executeSync {
            // This prevents clients that might not have the required resources installed from getting spammed about missing sound
            if (client.soundManager.get(packet.sound) != null) {
                client.world?.playSound(client.player, packet.x, packet.y, packet.z, SoundEvent(packet.sound), packet.category, packet.volume, packet.pitch)
            }
        }
    }
}