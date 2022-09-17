/*
 * Copyright (C) 2022 Pokemon Cobbled Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cablemc.pokemoncobbled.common.client.net.battle

import com.cablemc.pokemoncobbled.common.CobbledNetwork
import com.cablemc.pokemoncobbled.common.api.text.lightPurple
import com.cablemc.pokemoncobbled.common.client.keybind.currentKey
import com.cablemc.pokemoncobbled.common.client.keybind.keybinds.PartySendBinding
import com.cablemc.pokemoncobbled.common.client.net.ClientPacketHandler
import com.cablemc.pokemoncobbled.common.net.messages.client.battle.ChallengeNotificationPacket
import com.cablemc.pokemoncobbled.common.util.lang
import net.minecraft.client.MinecraftClient

object ChallengeNotificationHandler : ClientPacketHandler<ChallengeNotificationPacket> {
    override fun invokeOnClient(packet: ChallengeNotificationPacket, ctx: CobbledNetwork.NetworkContext) {
        MinecraftClient.getInstance().player?.sendMessage(
            lang(
                "challenge.receiver",
                packet.challengerName,
                PartySendBinding.currentKey().localizedText
            ).lightPurple()
        )
    }
}