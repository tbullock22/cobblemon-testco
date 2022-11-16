/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.keybind.keybinds

import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.gui.summary.Summary
import com.cobblemon.mod.common.client.keybind.CobblemonPartyLockedKeyBinding
import com.cobblemon.mod.common.client.keybind.KeybindCategories
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.InputUtil

object SummaryBinding : CobblemonPartyLockedKeyBinding(
    "key.cobblemon.summary",
    InputUtil.Type.KEYSYM,
    InputUtil.GLFW_KEY_M,
    KeybindCategories.COBBLEMON_CATEGORY
) {
    override fun onPress() {
        MinecraftClient.getInstance().setScreen(Summary(CobblemonClient.storage.myParty))
    }
}