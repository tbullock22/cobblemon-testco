/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.pc

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.client.gui.drawProfilePokemon
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.client.render.renderScaledGuiItemIcon
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.lang
import com.cobblemon.mod.common.util.math.fromEulerXYZDegrees
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.sound.SoundManager
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import org.joml.Quaternionf
import org.joml.Vector3f

open class StorageSlot(
    x: Int, y: Int,
    private val parent: StorageWidget,
    onPress: PressAction
) : ButtonWidget(x, y, SIZE, SIZE, Text.literal("StorageSlot"), onPress, DEFAULT_NARRATION_SUPPLIER) {

    companion object {
        const val SIZE = 25

        private val genderIconMale = cobblemonResource("textures/gui/pc/gender_icon_male.png")
        private val genderIconFemale = cobblemonResource("textures/gui/pc/gender_icon_female.png")
        private val selectPointerResource = cobblemonResource("textures/gui/pc/pc_pointer.png")
    }

    override fun playDownSound(soundManager: SoundManager) {
    }

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        if (shouldRender()) {
            renderSlot(matrices, x, y)
        }
    }

    fun renderSlot(matrices: MatrixStack, posX: Int, posY: Int) {
        val pokemon = getPokemon() ?: return

        DrawableHelper.enableScissor(
            posX - 2,
            posY + 2,
            posX + SIZE + 4,
            posY + SIZE + 4
        )

        // Render Pokémon
        matrices.push()
        matrices.translate(posX + (SIZE / 2.0), posY + 1.0, 0.0)
        matrices.scale(2.5F, 2.5F, 1F)
        drawProfilePokemon(
            renderablePokemon = pokemon.asRenderablePokemon(),
            matrixStack = matrices,
            rotation = Quaternionf().fromEulerXYZDegrees(Vector3f(13F, 35F, 0F)),
            state = null,
            scale = 4.5F
        )
        matrices.pop()

        DrawableHelper.disableScissor()

        // Ensure elements are not hidden behind Pokémon render
        matrices.push()
        matrices.translate(0.0, 0.0, 100.0)
        // Level
        drawScaledText(
            matrixStack = matrices,
            text = lang("ui.lv.number", pokemon.level),
            x = posX + 1,
            y = posY + 1,
            shadow = true,
            scale = PCGUI.SCALE
        )

        if (pokemon.gender != Gender.GENDERLESS) {
            blitk(
                matrixStack = matrices,
                texture = if (pokemon.gender == Gender.MALE) genderIconMale else genderIconFemale,
                x = (posX + 21) / PCGUI.SCALE,
                y = (posY + 1) / PCGUI.SCALE,
                width = 6,
                height = 8,
                scale = PCGUI.SCALE
            )
        }
        matrices.pop()

        if (isSelected) {
            blitk(
                matrixStack = matrices,
                texture = selectPointerResource,
                x = (posX + 10) / PCGUI.SCALE,
                y = ((posY - 3) / PCGUI.SCALE) - parent.pcGui.selectPointerOffsetY,
                width = 11,
                height = 8,
                scale = PCGUI.SCALE
            )
        }

        // Held Item
        val heldItem = pokemon.heldItemNoCopy()
        if (!heldItem.isEmpty) {
            renderScaledGuiItemIcon(
                itemStack = heldItem,
                x = posX + 16.0,
                y = posY + 16.0,
                scale = 0.5,
                matrixStack = matrices
            )
        }
    }

    open fun getPokemon(): Pokemon? {
        return null
    }

    override fun isSelected(): Boolean {
        return getPokemon() == parent.pcGui.previewPokemon
    }

    open fun shouldRender(): Boolean {
        return true
    }

    fun isHovered(mouseX: Int, mouseY: Int) = mouseX.toFloat() in (x.toFloat()..(x.toFloat() + SIZE)) && mouseY.toFloat() in (y.toFloat()..(y.toFloat() + SIZE))
}