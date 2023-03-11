/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.startselection.widgets

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.client.gui.startselection.StarterSelectionScreen
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.config.starter.RenderableStarterCategory
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text

class CategoryList(
    private val paneWidth: Int,
    private val paneHeight: Int,
    topOffset: Int,
    bottomOffset: Int,
    private val entryWidth: Int,
    entryHeight: Int,
    private val categories: List<RenderableStarterCategory>,
    val x: Int, val y: Int,
    private val minecraft: MinecraftClient = MinecraftClient.getInstance(),
    private val starterSelectionScreen: StarterSelectionScreen
) : AlwaysSelectedEntryListWidget<CategoryList.Category>(
    minecraft,
    paneWidth,
    paneHeight,
    topOffset,
    bottomOffset,
    entryHeight
) {

    companion object {
        private const val CATEGORY_BUTTON_WIDTH = 51.5f
        private const val CATEGORY_BUTTON_HEIGHT = 16f
        private const val ENTRY_X_OFFSET = 10f
        private val categoryResource = cobblemonResource("ui/starterselection/starterselection_slot.png")
    }

    init {
        this.correctSize()
        this.setRenderHorizontalShadows(false)
        this.setRenderBackground(false)
        this.setRenderSelection(false)
    }

    private var entriesCreated = false

    private fun createEntries() = categories.map {
        Category(it)
    }

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        if (!entriesCreated) {
            createEntries().forEach { addEntry(it) }
            entriesCreated = true
        }
        DrawableHelper.enableScissor(
            x,
            y,
            x + width,
            y + height
        )
        super.render(matrices, mouseX, mouseY, delta)
        DrawableHelper.disableScissor()
    }

    private fun correctSize() {
        this.updateSize(this.paneWidth, this.paneHeight, this.y, this.y + this.paneHeight)
        this.setLeftPos(this.x)
    }

    private fun scale(n: Int): Int = (this.client.window.scaleFactor * n).toInt()
    override fun getRowWidth() = this.entryWidth
    override fun getScrollbarPositionX(): Int {
        return this.left + this.width - 5
    }


    inner class Category(private val category: RenderableStarterCategory) : Entry<Category>() {

        override fun render(
            matrices: MatrixStack,
            index: Int,
            y: Int,
            x: Int,
            entryWidth: Int,
            entryHeight: Int,
            mouseX: Int,
            mouseY: Int,
            hovered: Boolean,
            tickDelta: Float
        ) {
            val isHovered = mouseX >= x && mouseY >= y && mouseX < x + entryWidth && mouseY < y + (entryHeight - 1)
            if (isHovered) {
                blitk(
                    matrixStack = matrices,
                    x = x + 2.5f, y = y,
                    texture = categoryResource,
                    width = CATEGORY_BUTTON_WIDTH, height = CATEGORY_BUTTON_HEIGHT,
                    red = 0.75f, green = 0.75f, blue = 0.75f
                )
            } else
                blitk(
                    matrixStack = matrices,
                    x = x + 2.5f, y = y,
                    texture = categoryResource,
                    width = CATEGORY_BUTTON_WIDTH, height = CATEGORY_BUTTON_HEIGHT
                )
            drawScaledText(
                matrixStack = matrices,
                text = category.displayNameText,
                x = x + 28,
                y = y + 4.5F,
                scale = 1F,
                maxCharacterWidth = 50,
                shadow = true,
                centered = true
            )
        }

        override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
            starterSelectionScreen.changeCategory(category = category)
            return true
        }

        override fun getNarration(): Text {
            return Text.of("Yes")
        }
    }
}