/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.keybind

import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.net.messages.server.starter.RequestStarterScreenPacket
import com.cobblemon.mod.common.util.lang
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.InputUtil

/**
 * Util class for preventing clicks until the player has a party member, this will automatically handle starter selection
 */
abstract class CobblemonPartyLockedKeyBinding(
    name: String,
    type: InputUtil.Type,
    key: Int,
    category: String
) : CobblemonKeyBinding(name, type, key, category) {

    override fun onTick() {
        if (this.wasPressed() && this.hasPartyMembers()) {
            this.onPress()
        }
    }

    private fun hasPartyMembers(): Boolean {
        val havePokemon = CobblemonClient.storage.myParty.slots.any { it != null }
        val starterSelected = CobblemonClient.clientPlayerData.starterSelected
        val startersLocked = CobblemonClient.clientPlayerData.starterLocked
        if (!starterSelected && !havePokemon) {
            if (startersLocked) {
                MinecraftClient.getInstance().player?.sendMessage(lang("ui.starter.cannotchoose").red(), false)
            } else {
                RequestStarterScreenPacket().sendToServer()
            }
            return false
        }
        return true
    }

}