/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.summary.widgets

import com.cobblemon.mod.common.client.gui.drawProfilePokemon
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonFloatingState
import com.cobblemon.mod.common.pokemon.RenderablePokemon
import com.cobblemon.mod.common.util.math.fromEulerXYZDegrees
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import org.joml.Quaternionf
import org.joml.Vector3f

class ModelWidget(
    pX: Int, pY: Int,
    pWidth: Int, pHeight: Int,
    var pokemon: RenderablePokemon,
    val baseScale: Float = 2.7F,
    var rotationY: Float = 35F,
    var offsetY: Double = 0.0
): SoundlessWidget(pX, pY, pWidth, pHeight, Text.literal("Summary - ModelWidget")) {

    companion object {
        var render = true
    }

    var state = PokemonFloatingState()
    private val minecraft = MinecraftClient.getInstance()
    val rotVec = Vector3f(13F, rotationY, 0F)

    override fun renderButton(pMatrixStack: MatrixStack, pMouseX: Int, pMouseY: Int, pPartialTicks: Float) {
        if (!render) {
            return
        }
        hovered = pMouseX >= x && pMouseY >= y && pMouseX < x + width && pMouseY < y + height
        renderPKM(pMatrixStack)
    }

    private fun renderPKM(poseStack: MatrixStack) {
        poseStack.push()

        DrawableHelper.enableScissor(
            x,
            y,
            x + width,
            y +  height
        )

        poseStack.translate(x + width * 0.5, y.toDouble() + offsetY, 0.0)
        poseStack.scale(baseScale, baseScale, baseScale)
        poseStack.push()

        drawProfilePokemon(
            renderablePokemon = pokemon,
            matrixStack = poseStack,
            rotation = Quaternionf().fromEulerXYZDegrees(rotVec),
            state = state
        )

        poseStack.pop()
        DrawableHelper.disableScissor()

        poseStack.pop()
    }

    override fun onClick(pMouseX: Double, pMouseY: Double) {
    }
}