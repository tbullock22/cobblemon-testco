/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.keybind

import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.InputUtil

/**
 * An extension for the [CobblemonKeyBinding] to prevent the [onPress] from rapidly triggering when holding down the associated key
 *
 * @author Qu
 * @since 2022-02-23
 */
abstract class CobblemonBlockingKeyBinding(
    name: String,
    type: InputUtil.Type = InputUtil.Type.KEYSYM,
    key: Int,
    category: String
) : CobblemonKeyBinding(name, type, key, category) {
    var wasDown = false
    var timeDown = 0F

    open fun onRelease() {}

    override fun onTick() {
        if (isPressed && !wasDown) {
            wasDown = true
            timeDown = 0F
            onPress()
        } else if (!isPressed && wasDown) {
            onRelease()
            wasDown = false
        } else if (!isPressed) {
            wasDown = false
        } else if (wasDown) {
            timeDown += MinecraftClient.getInstance().tickDelta
        }
    }
}