/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.interact.moveselect

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.moves.Move
import com.cobblemon.mod.common.client.gui.ExitButton
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.sound.SoundEvent
import net.minecraft.text.Text

class MoveSelectGUI(
    pokemon: Pokemon
) : Screen(Text.translatable("cobblemon.ui.interact.moveselect")) {
    companion object {
        const val WIDTH = 122
        const val HEIGHT = 133

        private val baseBackgroundResource = cobblemonResource("ui/interact/move_select.png")
    }

    private val moves = pokemon.moveSet.getMoves()

    override fun init() {
        val x = (width - WIDTH) / 2
        val y = (height - HEIGHT) / 2

        moves.forEachIndexed { index, move ->
            addDrawableChild(
                MoveSlotButton(
                    x = x + 7,
                    y = y + ((MoveSlotButton.HEIGHT + 3) * index),
                    move = move,
                    enabled = shouldBeEnabled(move)
                ) { onPress(move) }
            )
        }

        // Add Exit Button
        addDrawableChild(
            ExitButton(
                pX = x + 92,
                pY = y + 115
            ) {
                playSound(CobblemonSounds.GUI_CLICK.get())
                MinecraftClient.getInstance().setScreen(null)
            }
        )

        super.init()
    }

    override fun render(matrixStack: MatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
        val x = (width - WIDTH) / 2
        val y = (height - HEIGHT) / 2

        blitk(
            matrixStack = matrixStack,
            texture = baseBackgroundResource,
            x = x,
            y = y,
            width = WIDTH,
            height = HEIGHT
        )

        // Render all added Widgets
        super.render(matrixStack, mouseX, mouseY, partialTicks)
    }

    private fun onPress(move: Move) {
    }

    private fun shouldBeEnabled(move: Move): Boolean {
        return true
    }

    fun playSound(soundEvent: SoundEvent) {
        MinecraftClient.getInstance().soundManager.play(PositionedSoundInstance.master(soundEvent, 1.0F))
    }
}