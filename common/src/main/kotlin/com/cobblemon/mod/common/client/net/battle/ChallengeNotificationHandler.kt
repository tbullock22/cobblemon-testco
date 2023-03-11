/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.battle

import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.api.text.lightPurple
import com.cobblemon.mod.common.client.keybind.boundKey
import com.cobblemon.mod.common.client.keybind.keybinds.PartySendBinding
import com.cobblemon.mod.common.client.net.ClientPacketHandler
import com.cobblemon.mod.common.net.messages.client.battle.ChallengeNotificationPacket
import com.cobblemon.mod.common.util.lang
import net.minecraft.client.MinecraftClient

object ChallengeNotificationHandler : ClientPacketHandler<ChallengeNotificationPacket> {
    override fun invokeOnClient(packet: ChallengeNotificationPacket, ctx: CobblemonNetwork.NetworkContext) {
        MinecraftClient.getInstance().player?.sendMessage(
            lang(
                "challenge.receiver",
                packet.challengerName,
                PartySendBinding.boundKey().localizedText
            ).lightPurple()
        )
    }
}