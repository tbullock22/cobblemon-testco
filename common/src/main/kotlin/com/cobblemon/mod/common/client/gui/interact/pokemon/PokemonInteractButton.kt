/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.interact.pokemon

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.client.gui.normalizeNumber
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.sound.SoundManager
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec2f
import org.joml.Vector2f
import kotlin.math.sign

class PokemonInteractButton(
    var x: Double, var y: Double,
    private val iconResource: Identifier? = null,
    private val textureResource: Identifier,
    private val enabled: Boolean = true,
    private val container: PokemonInteractGUI,
    val xDirMod: Double,
    val yDirMod: Double,
    onPress: PressAction
) : ButtonWidget(x.toInt(), y.toInt(), SIZE, SIZE, Text.literal("Interact"), onPress, DEFAULT_NARRATION_SUPPLIER) {

    companion object {
        const val SIZE = 69
        const val ICON_SIZE = 32
        const val ICON_SCALE = 0.5F
    }
    private var xOffset: Double = x
    private var yOffset: Double = y
    init {
        xOffset = x
        yOffset = y
        x += 10*xDirMod
        y += 10*yDirMod
    }

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        x = MathHelper.lerp(0.15,x, xOffset)
        y = MathHelper.lerp(0.15,y, yOffset)
        blitk(
            matrixStack = matrices,
            texture = textureResource,
            x = x,
            y = y,
            width = SIZE,
            height = SIZE,
            vOffset = if (isHovered(mouseX.toDouble(), mouseY.toDouble()) && enabled) SIZE else 0,
            textureHeight = SIZE * 2,
            alpha = if (enabled) 1F else 0.4F
        )

        if (iconResource != null) {
            blitk(
                matrixStack = matrices,
                texture = iconResource,
                x = (x + 26.5) / ICON_SCALE,
                y = (y + 26.5) / ICON_SCALE,
                width = ICON_SIZE,
                height = ICON_SIZE,
                alpha = if (enabled) 1F else 0.4F,
                scale = ICON_SCALE
            )
        }
    }

    override fun playDownSound(pHandler: SoundManager) {
    }

    fun isHovered(mouseX: Double, mouseY: Double) = (mouseX.toFloat() in (x.toFloat()..(x.toFloat() + SIZE)) && mouseY.toFloat() in (y.toFloat()..(y.toFloat() + SIZE))) && !container.isMouseInCenter(mouseX, mouseY)
}