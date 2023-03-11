/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.keybind

import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil

/**
 * An extensions for Minecraft's [KeyBinding]
 * When creating a new [CobblemonKeyBinding] [onPress] will be called when the key is pressed.
 *
 * @author Qu
 * @since 2022-02-17
 */
abstract class CobblemonKeyBinding(
    name: String,
    type: InputUtil.Type = InputUtil.Type.KEYSYM,
    key: Int,
    category: String
): KeyBinding(name, type, key, category) {

    abstract fun onPress()

    open fun onTick() {
        if (this.wasPressed()) {
            onPress()
        }
    }
}