/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.pc

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.client.sound.SoundManager
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text

class NavigationButton(
    pX: Int, pY: Int,
    private val forward: Boolean,
    onPress: PressAction
): ButtonWidget(pX, pY, (WIDTH * SCALE).toInt(), (HEIGHT * SCALE).toInt(), Text.literal("Navigation"), onPress) {

    companion object {
        private const val WIDTH = 8F
        private const val HEIGHT = 10F
        private const val SCALE = 0.5F
        private val forwardButtonResource = cobblemonResource("ui/pc/pc_arrow_next.png")
        private val backwardsButtonResource = cobblemonResource("ui/pc/pc_arrow_previous.png")
    }

    override fun renderButton(pMatrixStack: MatrixStack, pMouseX: Int, pMouseY: Int, pPartialTicks: Float) {
        val hovered = (isHovered(pMouseX.toDouble(), pMouseY.toDouble()))
        blitk(
            matrixStack = pMatrixStack,
            x = x / SCALE,
            y = y / SCALE,
            texture = if (forward) forwardButtonResource else backwardsButtonResource,
            width = WIDTH,
            height = HEIGHT,
            vOffset = if (hovered) HEIGHT else 0,
            textureHeight = HEIGHT * 2,
            scale = SCALE
        )
    }

    override fun playDownSound(soundManager: SoundManager) {
        soundManager.play(PositionedSoundInstance.master(CobblemonSounds.GUI_CLICK.get(), 1.0F))
    }

    fun isHovered(mouseX: Double, mouseY: Double) = mouseX.toFloat() in (x.toFloat()..(x.toFloat() + (WIDTH * SCALE))) && mouseY.toFloat() in (y.toFloat()..(y.toFloat() + (HEIGHT * SCALE)))
}