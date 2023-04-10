/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.summary.widgets.type

import com.cobblemon.mod.common.api.gui.ColourLibrary
import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.client.render.drawScaledText
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
class SingleTypeWidget(
    pX: Int, pY: Int,
    pWidth: Int, pHeight: Int,
    private val type: ElementalType,
    private val renderText: Boolean = true
) : TypeWidget(pX, pY, pWidth, pHeight, Text.literal("SingleTypeWidget - ${type.name}")) {

    override fun renderButton(pMatrixStack: MatrixStack, pMouseX: Int, pMouseY: Int, pPartialTicks: Float) {
        pMatrixStack.push()
        pMatrixStack.translate(0.35, 0.0, 0.0)
        renderType(type, pMatrixStack)
        pMatrixStack.pop()
        // Render Type Name
        if (this.renderText) {
            pMatrixStack.push()
            drawScaledText(
                matrixStack = pMatrixStack,
                text = type.displayName,
                x = x + 35.5F, y = y + 3F,
                colour = ColourLibrary.WHITE, shadow = false,
                centered = true,
                maxCharacterWidth = 40,
                scale = 0.6F
            )
        }
    }
}