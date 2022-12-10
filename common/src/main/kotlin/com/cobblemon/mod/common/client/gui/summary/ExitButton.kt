/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.summary

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text

class ExitButton(
    pX: Int, pY: Int,
    onPress: PressAction
): ButtonWidget(pX, pY, (WIDTH * SCALE).toInt(), (HEIGHT * SCALE).toInt(), Text.literal(""), onPress) {

    companion object {
        private const val WIDTH = 52F
        private const val HEIGHT = 26F
        private const val SCALE = 0.5F
        private val exitButtonResource = cobblemonResource("ui/summary/summary_back_button.png")
    }

    override fun renderButton(pMatrixStack: MatrixStack, pMouseX: Int, pMouseY: Int, pPartialTicks: Float) {
        blitk(
            matrixStack = pMatrixStack,
            texture = exitButtonResource,
            x = x * 2,
            y = y * 2,
            width = WIDTH,
            height = HEIGHT,
            vOffset = if (isHovered(pMouseX.toDouble(), pMouseY.toDouble())) HEIGHT else 0,
            textureHeight = HEIGHT * 2,
            scale = SCALE
        )
    }

    fun isHovered(mouseX: Double, mouseY: Double) = mouseX.toFloat() in (x.toFloat()..(x.toFloat() + (WIDTH * SCALE))) && mouseY.toFloat() in (y.toFloat()..(y.toFloat() + (HEIGHT * SCALE)))
}
