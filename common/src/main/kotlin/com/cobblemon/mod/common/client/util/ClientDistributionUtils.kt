/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.util

import net.minecraft.client.MinecraftClient
import net.minecraft.util.Identifier

/**
 * Checks if a resource exists at this location
 */
fun Identifier.exists(): Boolean {
    return MinecraftClient.getInstance().resourceManager.getResource(this).isPresent
}

fun runOnRender(action: () -> Unit) {
    MinecraftClient.getInstance().execute(action)
}