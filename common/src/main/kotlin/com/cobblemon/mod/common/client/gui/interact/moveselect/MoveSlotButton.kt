/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.interact.moveselect

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.moves.Move
import com.cobblemon.mod.common.api.moves.Moves
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.api.text.gold
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.gui.MoveCategoryIcon
import com.cobblemon.mod.common.client.gui.TypeIcon
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.math.toRGB
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.sound.SoundManager
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper

class MoveSlotButton(
    x: Int, y: Int,
    val move: Move,
    val enabled: Boolean = true,
    onPress: PressAction
) : ButtonWidget(x, y, WIDTH, HEIGHT, Text.literal("Move"), onPress) {

    companion object {
        private val moveResource = cobblemonResource("ui/summary/summary_move.png")
        private val moveOverlayResource = cobblemonResource("ui/summary/summary_move_overlay.png")

        const val WIDTH = 108
        const val HEIGHT = 22
    }

    override fun render(pMatrixStack: MatrixStack, pMouseX: Int, pMouseY: Int, pPartialTicks: Float) {
        hovered = pMouseX >= x && pMouseY >= y && pMouseX < x + width && pMouseY < y + height

        val moveTemplate = Moves.getByNameOrDummy(move.name)
        val rgb = moveTemplate.elementalType.hue.toRGB()

        blitk(
            matrixStack = pMatrixStack,
            texture = moveResource,
            x = x,
            y = y,
            width = WIDTH,
            height = HEIGHT,
            vOffset = if (isHovered) HEIGHT else 0,
            textureHeight = HEIGHT * 2,
            red = rgb.first,
            green = rgb.second,
            blue = rgb.third
        )

        blitk(
            matrixStack = pMatrixStack,
            texture = moveOverlayResource,
            x = x,
            y = y,
            width = WIDTH,
            height = HEIGHT
        )

        var movePPText = Text.literal("${move.currentPp}/${move.maxPp}").bold()

        if (move.currentPp <= MathHelper.floor(move.maxPp / 2F)) {
            movePPText = if (move.currentPp == 0) movePPText.red() else movePPText.gold()
        }

        drawScaledText(
            matrixStack = pMatrixStack,
            font = CobblemonResources.DEFAULT_LARGE,
            text = movePPText,
            x = x + 93,
            y = y + 13,
            centered = true
        )

        // Type Icon
        TypeIcon(
            x = x + 2,
            y = y + 2,
            type = moveTemplate.elementalType
        ).render(pMatrixStack)

        // Move Category
        MoveCategoryIcon(
            x = x + 66,
            y = y + 13.5,
            category = move.damageCategory
        ).render(pMatrixStack)

        // Move Name
        drawScaledText(
            matrixStack = pMatrixStack,
            font = CobblemonResources.DEFAULT_LARGE,
            text = move.displayName.bold(),
            x = x + 28,
            y = y + 2,
            shadow = true
        )
    }
}
