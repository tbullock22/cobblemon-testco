/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.summary.widgets.screens

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.client.sound.SoundManager
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.MutableText

class SummaryTab(
    pX: Int, pY: Int,
    val label: MutableText,
    onPress: PressAction
): ButtonWidget(pX, pY, 50, 13, label, onPress) {
    private var isActive = false

    override fun renderButton(pMatrixStack: MatrixStack, pMouseX: Int, pMouseY: Int, pPartialTicks: Float) {
        if (isActive) {
            blitk(
                matrixStack = pMatrixStack,
                texture = cobblemonResource("ui/summary/summary_tab.png"),
                x = x,
                y = y,
                width = width,
                height = height
            )
        }

        drawScaledText(
            matrixStack = pMatrixStack,
            font = CobblemonResources.DEFAULT_LARGE,
            text = label.bold(),
            x = x + 25,
            y = y + 3,
            centered = true,
            shadow = true
        )
    }

    override fun playDownSound(soundManager: SoundManager) {
    }

    fun toggleTab(state: Boolean = true) {
        isActive = state
    }
}
