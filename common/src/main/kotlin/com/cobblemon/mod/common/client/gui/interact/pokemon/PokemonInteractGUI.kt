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
import java.util.UUID
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import org.joml.Math

class PokemonInteractGUI(
    private val pokemonID: UUID,
    private val canMountShoulder: Boolean
) : Screen(Text.translatable("cobblemon.ui.interact.pokemon")) {
    var alpha = 0.0F
    companion object {
        const val SIZE = 138

        private val baseBackgroundResource = cobblemonResource("textures/gui/interact/interact_base.png")
        private val topLeftResource = cobblemonResource("textures/gui/interact/button_left_top.png")
        private val topRightResource = cobblemonResource("textures/gui/interact/button_right_top.png")
        private val bottomLeftResource = cobblemonResource("textures/gui/interact/button_left_bottom.png")
        private val bottomRightResource = cobblemonResource("textures/gui/interact/button_right_bottom.png")

        private val iconShoulderResource = cobblemonResource("textures/gui/interact/icon_shoulder.png")
        private val iconHeldItemResource = cobblemonResource("textures/gui/interact/icon_held_item.png")
    }

    override fun init() {
        val x = (width - SIZE) / 2.0
        val y = (height - SIZE) / 2.0

        // Mount/Shoulder Button
        this.addDrawableChild(PokemonInteractButton(
            x = x,
            y = y,
            iconResource = iconShoulderResource,
            textureResource = topLeftResource,
            enabled = canMountShoulder,
            container = this,
            xDirMod =-1.0,
            yDirMod = -2.0
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
            container = this,
            xDirMod = 2.0,
            yDirMod = -1.0
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
            container = this,
            xDirMod = -1.0,
            yDirMod = 2.0
        ) {});

        // ToDo something else
        this.addDrawableChild(PokemonInteractButton(
            x = x + PokemonInteractButton.SIZE,
            y = y + PokemonInteractButton.SIZE,
            textureResource = bottomRightResource,
            enabled = false,
            container = this,
            xDirMod = 2.0,
            yDirMod = 1.0
        ) {});
    }

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        val x = (width - SIZE) / 2
        val y = (height - SIZE) / 2
        alpha = Math.lerp(alpha.toDouble(), 1.0, 0.15).toFloat()
        // Render Background
        blitk(
            matrixStack = matrices,
            texture = baseBackgroundResource,
            x = x,
            y = y,
            width = SIZE,
            height = SIZE,
            alpha = alpha
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