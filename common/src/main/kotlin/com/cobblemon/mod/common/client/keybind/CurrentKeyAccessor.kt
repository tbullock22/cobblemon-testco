/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.keybind

import com.cobblemon.mod.common.mixin.accessor.KeyBindingAccessor
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil

fun KeyBinding.boundKey(): InputUtil.Key {
    return (this as KeyBindingAccessor).boundKey()
}