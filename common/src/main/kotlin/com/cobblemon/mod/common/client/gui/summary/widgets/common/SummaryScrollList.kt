/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.summary.widgets.common

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.util.cobblemonResource
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.MutableText

abstract class SummaryScrollList<T : AlwaysSelectedEntryListWidget.Entry<T>>(
    val x: Int,
    val y: Int,
    val label: MutableText,
    slotHeight: Int
) : AlwaysSelectedEntryListWidget<T>(
    MinecraftClient.getInstance(),
    WIDTH, // width
    HEIGHT, // height
    0, // top
    HEIGHT, // bottom
    slotHeight
) {
    companion object {
        const val WIDTH = 108
        const val HEIGHT = 114
        const val SLOT_WIDTH = 91

        private val backgroundResource = cobblemonResource("ui/summary/summary_scroll_background.png")
        private val scrollOverlayResource = cobblemonResource("ui/summary/summary_scroll_overlay.png")
    }

    override fun getRowWidth(): Int {
        return SLOT_WIDTH
    }

    init {
        correctSize()
        setRenderHorizontalShadows(false)
        setRenderBackground(false)
        setRenderSelection(false)
    }

    override fun getScrollbarPositionX(): Int {
        return left + width - 3
    }

    override fun render(poseStack: MatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
        correctSize()

        blitk(
            matrixStack = poseStack,
            texture = backgroundResource,
            x = left,
            y = top,
            height = HEIGHT,
            width = WIDTH
        )

        RenderSystem.enableScissor(
            scaleIt(left),
            scaleIt(client.window.scaledHeight - (top + HEIGHT + 1)),
            scaleIt(width),
            scaleIt(height + 1)
        )
        super.render(poseStack, mouseX, mouseY, partialTicks)
        RenderSystem.disableScissor()

        // Scroll Overlay
        val scrollOverlayOffset = 4
        blitk(
            matrixStack = poseStack,
            texture = scrollOverlayResource,
            x = left,
            y = top - (scrollOverlayOffset / 2),
            height = HEIGHT + scrollOverlayOffset,
            width = WIDTH
        )

        // Label
        drawScaledText(
            matrixStack = poseStack,
            font = CobblemonResources.DEFAULT_LARGE,
            text = label.bold(),
            x = left + 32.5,
            y = top - 13.5,
            centered = true,
            shadow = true
        )
    }

    private fun correctSize() {
        updateSize(WIDTH, HEIGHT, y + 1, (y + 1) + (HEIGHT - 2))
        setLeftPos(x)
    }

    private fun scaleIt(i: Int): Int {
        return (client.window.scaleFactor * i).toInt()
    }
}