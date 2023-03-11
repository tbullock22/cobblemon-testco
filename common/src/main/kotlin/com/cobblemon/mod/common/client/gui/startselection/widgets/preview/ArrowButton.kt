/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.startselection.widgets.preview

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.gui.widget.TexturedButtonWidget
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
class ArrowButton(
    pX: Int, pY: Int,
    pWidth: Int, pHeight: Int,
    pXTexStart: Int = 0, pYTexStart: Int = 0, pYDiffText: Int = 0,
    right: Boolean,
    private val texture: Identifier = if (right) RIGHT_ARROW_BUTTON_RESOURCE else LEFT_ARROW_BUTTON_RESOURCE,
    onPress: PressAction
): TexturedButtonWidget(pX, pY, pWidth, pHeight, pXTexStart, pYTexStart, pYDiffText,
    texture, onPress) {

    companion object {
        private val RIGHT_ARROW_BUTTON_RESOURCE = cobblemonResource("ui/starterselection/starterselection_arrow_right.png")
        private val LEFT_ARROW_BUTTON_RESOURCE = cobblemonResource("ui/starterselection/starterselection_arrow_left.png")

        private const val ARROW_BUTTON_WIDTH = 9f
        private const val ARROW_BUTTON_HEIGHT = 14f
    }

    override fun renderButton(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height
        if (isHovered) {
            blitk(
                matrixStack = matrices,
                x = x + 2.4F, y = y - 0.0F,
                texture = texture,
                width = ARROW_BUTTON_WIDTH, height = ARROW_BUTTON_HEIGHT,
                red = 0.75f, green = 0.75f, blue = 0.75f
            )
        } else {
            blitk(
                matrixStack = matrices,
                x = x + 2.4F, y = y - 0.0F,
                texture = texture,
                width = ARROW_BUTTON_WIDTH, height = ARROW_BUTTON_HEIGHT
            )
        }
    }
}