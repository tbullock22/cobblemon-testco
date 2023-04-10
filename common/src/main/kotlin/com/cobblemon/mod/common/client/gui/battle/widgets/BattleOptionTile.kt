/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.battle.widgets

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.gui.battle.BattleGUI
import com.cobblemon.mod.common.client.render.drawScaledText
import net.minecraft.client.gui.Drawable
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.Selectable
import net.minecraft.client.gui.Selectable.SelectionType.HOVERED
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.screen.narration.NarrationPart
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.MutableText
import net.minecraft.util.Identifier
class BattleOptionTile(
    val battleGUI: BattleGUI,
    val x: Int,
    val y: Int,
    val resource: Identifier,
    val text: MutableText,
    val onClick: () -> Unit
) : Drawable, Element, Selectable {
    companion object {
        const val  OPTION_WIDTH = 90
        const val OPTION_HEIGHT = 26
    }

    private var focused = false

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        val opacity = CobblemonClient.battleOverlay.opacityRatio
        if (opacity < 0.1) {
            return
        }
        blitk(
            matrixStack = matrices,
            texture = resource,
            x = x,
            y = y,
            alpha = opacity,
            width = OPTION_WIDTH,
            height = OPTION_HEIGHT,
            vOffset = if (isHovered(mouseX.toDouble(), mouseY.toDouble())) OPTION_HEIGHT else 0,
            textureHeight = OPTION_HEIGHT * 2
        )

        val scale = 1F
        drawScaledText(
            matrixStack = matrices,
            text = text,
            x = x + 6,
            y = y + 8,
            opacity = opacity,
            scale = scale,
            shadow = true
        )
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (mouseX < x || mouseY < y || mouseX > x + OPTION_WIDTH || mouseY > y + OPTION_HEIGHT) {
            return false
        }
        onClick()
        return true
    }

    override fun setFocused(focused: Boolean) {
        this.focused = focused
    }

    override fun isFocused() = focused

    fun isHovered(mouseX: Double, mouseY: Double) = mouseX > x && mouseY > y && mouseX < x + OPTION_WIDTH && mouseY < y + OPTION_HEIGHT

    override fun appendNarrations(builder: NarrationMessageBuilder) {
        builder.put(NarrationPart.TITLE, text)
    }

    override fun getType() = HOVERED


}