/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.summary.widgets.screens.moves

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.moves.MoveTemplate
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.client.sound.SoundManager
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text

/**
 * This Button is specifically made for the Summary to change the order of the Moves
 *
 * The blocked var was added to prevent the switching the order of Buttons triggering another switch
 */
class SwapMoveButton(
    val pX: Int, val pY: Int,
    var move: MoveTemplate,
    var movesWidget: MovesWidget,
    onPress: PressAction
): ButtonWidget((pX + OFFSET_X).toInt(), (pY + OFFSET_Y).toInt(), (WIDTH * SCALE).toInt(), (HEIGHT * SCALE).toInt(), Text.literal(""), onPress) {

    override fun mouseDragged(d: Double, e: Double, i: Int, f: Double, g: Double): Boolean {
        return false
    }

    companion object {
        private const val WIDTH = 12
        private const val HEIGHT = 18
        private const val OFFSET_X = 114.5F
        private const val OFFSET_Y = 6.5F
        private const val SCALE = 0.5F
        private val switchMoveButtonResource = cobblemonResource("ui/summary/summary_move_swap.png")
    }

    override fun renderButton(poseStack: MatrixStack, pMouseX: Int, pMouseY: Int, pPartialTicks: Float) {
        val swapScreen = movesWidget.summary.sideScreen
        var selected = if (swapScreen is MoveSwapScreen) swapScreen.replacedMove?.template == move else false
        blitk(
            matrixStack = poseStack,
            texture = switchMoveButtonResource,
            x = (pX + OFFSET_X) / SCALE,
            y = (pY + OFFSET_Y) / SCALE,
            width = WIDTH,
            height = HEIGHT,
            vOffset = if (isHovered(pMouseX.toDouble(), pMouseY.toDouble()) || selected) HEIGHT else 0,
            textureHeight = HEIGHT * 2,
            scale = SCALE
        )
    }

    override fun playDownSound(soundManager: SoundManager) {
        soundManager.play(PositionedSoundInstance.master(CobblemonSounds.GUI_CLICK.get(), 1.0F))
    }

    fun isHovered(mouseX: Double, mouseY: Double) = mouseX.toFloat() in ((pX + OFFSET_X)..((pX + OFFSET_X) + (WIDTH * SCALE))) && mouseY.toFloat() in ((pY + OFFSET_Y)..((pY + OFFSET_Y) + (HEIGHT * SCALE) - 0.5F))
}
