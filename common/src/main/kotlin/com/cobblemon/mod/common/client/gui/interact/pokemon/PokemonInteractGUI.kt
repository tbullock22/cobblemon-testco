/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.interact.pokemon

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.net.messages.server.pokemon.interact.InteractPokemonPacket
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import java.util.UUID

class PokemonInteractGUI(
    private val pokemonID: UUID,
    private val canMountShoulder: Boolean
) : Screen(Text.translatable("cobblemon.ui.interact.pokemon")) {
    companion object {
        const val SIZE = 138

        private val baseBackgroundResource = cobblemonResource("ui/interact/interact_base.png")
        private val topLeftResource = cobblemonResource("ui/interact/button_left_top.png")
        private val topRightResource = cobblemonResource("ui/interact/button_right_top.png")
        private val bottomLeftResource = cobblemonResource("ui/interact/button_left_bottom.png")
        private val bottomRightResource = cobblemonResource("ui/interact/button_right_bottom.png")

        private val iconShoulderResource = cobblemonResource("ui/interact/icon_shoulder.png")
        private val iconHeldItemResource = cobblemonResource("ui/interact/icon_held_item.png")
    }

    override fun init() {
        val x = (width - SIZE) / 2
        val y = (height - SIZE) / 2

        // Mount/Shoulder Button
        this.addDrawableChild(PokemonInteractButton(
            x = x,
            y = y,
            iconResource = iconShoulderResource,
            textureResource = topLeftResource,
            enabled = canMountShoulder,
            container = this
        ) {
            if (canMountShoulder) {
                InteractPokemonPacket(pokemonID, true).sendToServer()
                MinecraftClient.getInstance().setScreen(null)
            }
        });

        // Give Item Button
        this.addDrawableChild(PokemonInteractButton(
            x = x + PokemonInteractButton.SIZE,
            y = y,
            iconResource = iconHeldItemResource,
            textureResource = topRightResource,
            container = this
        ) {
            InteractPokemonPacket(pokemonID, false).sendToServer()
            MinecraftClient.getInstance().setScreen(null)
        });

        // ToDo Moves
        this.addDrawableChild(PokemonInteractButton(
            x = x,
            y = y + PokemonInteractButton.SIZE,
            textureResource = bottomLeftResource,
            enabled = false,
            container = this
        ) {});

        // ToDo something else
        this.addDrawableChild(PokemonInteractButton(
            x = x + PokemonInteractButton.SIZE,
            y = y + PokemonInteractButton.SIZE,
            textureResource = bottomRightResource,
            enabled = false,
            container = this
        ) {});
    }

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        val x = (width - SIZE) / 2
        val y = (height - SIZE) / 2

        // Render Background
        blitk(
            matrixStack = matrices,
            texture = baseBackgroundResource,
            x = x,
            y = y,
            width = SIZE,
            height = SIZE
        )

        super.render(matrices, mouseX, mouseY, delta)
    }

    override fun shouldPause(): Boolean {
        return false
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (isMouseInCenter(mouseX, mouseY)) return false
        return super.mouseClicked(mouseX, mouseY, button)
    }

    fun isMouseInCenter(mouseX: Double, mouseY: Double): Boolean {
        val x = ((width - SIZE) / 2) + 44
        val y = ((height - SIZE) / 2) + 44
        return mouseX.toFloat() in (x.toFloat()..(x.toFloat() + 50))
            && mouseY.toFloat() in (y.toFloat()..(y.toFloat() + 50))
    }
}