/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.summary.widgets.screens.info

import com.cobblemon.mod.common.api.gui.ColourLibrary
import com.cobblemon.mod.common.api.gui.MultiLineLabelK
import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.api.text.plus
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.gui.summary.widgets.SoundlessWidget
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.asTranslated
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.lang
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text

class InfoWidget(
    pX: Int,
    pY: Int,
    private val pokemon: Pokemon
): SoundlessWidget(pX, pY, WIDTH, HEIGHT, Text.literal("InfoWidget")) {
    companion object {
        private const val WIDTH = 134
        private const val HEIGHT = 148
        private val infoBaseResource = cobblemonResource("textures/gui/summary/summary_info_base.png")
    }

    override fun renderButton(pMatrixStack: MatrixStack, pMouseX: Int, pMouseY: Int, pPartialTicks: Float) {
        // Base texture
        blitk(
            matrixStack = pMatrixStack,
            texture = infoBaseResource,
            x= x,
            y = y,
            width = width,
            height = height
        )

        // Pokédex Number
        drawScaledText(
            matrixStack = pMatrixStack,
            font = CobblemonResources.DEFAULT_LARGE,
            text = lang("ui.info.pokedex_number").bold(),
            x = x + 8,
            y = y + 6,
            shadow = true
        )

        // Add preceding zeroes if Pokédex number is less than 3 digits
        var dexNo = pokemon.species.nationalPokedexNumber.toString()
        while(dexNo.length < 3) {
            dexNo = "0$dexNo";
        }

        drawScaledText(
            matrixStack = pMatrixStack,
            font = CobblemonResources.DEFAULT_LARGE,
            text = dexNo.text().bold(),
            x = x + 53,
            y = y + 6,
            shadow = true
        )

        // Species
        drawScaledText(
            matrixStack = pMatrixStack,
            font = CobblemonResources.DEFAULT_LARGE,
            text = lang("ui.info.species").bold(),
            x = x + 8,
            y = y + 21,
            shadow = true
        )

        drawScaledText(
            matrixStack = pMatrixStack,
            font = CobblemonResources.DEFAULT_LARGE,
            text = pokemon.species.translatedName.bold(),
            x = x + 53,
            y = y + 21,
            shadow = true
        )

        // Type
        drawScaledText(
            matrixStack = pMatrixStack,
            font = CobblemonResources.DEFAULT_LARGE,
            text = lang("ui.info.type").bold(),
            x = x + 8,
            y = y + 36,
            shadow = true
        )

        val type = pokemon.types.map { it.displayName.copy() }.reduce { acc, next -> acc.plus("/").plus(next) }

        drawScaledText(
            matrixStack = pMatrixStack,
            font = CobblemonResources.DEFAULT_LARGE,
            text = type.bold(),
            x = x + 53,
            y = y + 36,
            shadow = true
        )

        // OT
        drawScaledText(
            matrixStack = pMatrixStack,
            font = CobblemonResources.DEFAULT_LARGE,
            text = lang("ui.info.original_trainer").bold(),
            x = x + 8,
            y = y + 51,
            shadow = true
        )

        if (pokemon.isPlayerOwned() && pokemon.getOwnerPlayer() != null) {
            pokemon.getOwnerPlayer()?.displayName?.copy()?.let {
                drawScaledText(
                    matrixStack = pMatrixStack,
                    font = CobblemonResources.DEFAULT_LARGE,
                    text = it.bold(),
                    x = x + 53,
                    y = y + 51,
                    shadow = true
                )
            }
        }

        // Nature
        drawScaledText(
            matrixStack = pMatrixStack,
            font = CobblemonResources.DEFAULT_LARGE,
            text = lang("ui.info.nature").bold(),
            x = x + 8,
            y = y + 66,
            shadow = true
        )

        drawScaledText(
            matrixStack = pMatrixStack,
            font = CobblemonResources.DEFAULT_LARGE,
            text = pokemon.nature.displayName.asTranslated().bold(),
            x = x + 53,
            y = y + 66,
            shadow = true
        )

        // Ability
        drawScaledText(
            matrixStack = pMatrixStack,
            font = CobblemonResources.DEFAULT_LARGE,
            text = lang("ui.info.ability").bold(),
            x = x + 8,
            y = y + 81,
            shadow = true
        )

        drawScaledText(
            matrixStack = pMatrixStack,
            font = CobblemonResources.DEFAULT_LARGE,
            text = pokemon.ability.displayName.asTranslated().bold(),
            x = x + 53,
            y = y + 81,
            shadow = true
        )

        val smallTextScale = 0.5F

        pMatrixStack.push()
        pMatrixStack.scale(smallTextScale, smallTextScale, 1F)
        MultiLineLabelK.create(
            component = pokemon.ability.description.asTranslated(),
            width = 117 / smallTextScale,
            maxLines = 3
        ).renderLeftAligned(
            poseStack = pMatrixStack,
            x = (x + 8) / smallTextScale,
            y = (y + 94.5) / smallTextScale,
            ySpacing = 5.5 / smallTextScale,
            colour = ColourLibrary.WHITE,
            shadow = true
        )
        pMatrixStack.pop()

        drawScaledText(
            matrixStack = pMatrixStack,
            text = lang("ui.info.experience_points"),
            x = x + 72.5,
            y = y + 125,
            scale = smallTextScale,
            shadow = true
        )

        drawScaledText(
            matrixStack = pMatrixStack,
            text = lang("ui.info.to_next_level"),
            x = x + 72.5,
            y = y + 137,
            scale = smallTextScale,
            shadow = true
        )

        val mcFont = MinecraftClient.getInstance().textRenderer
        val experience = pokemon.experience.toString().text()
        val experienceForThisLevel = pokemon.experience - if (pokemon.level == 1) 0 else pokemon.experienceGroup.getExperience(pokemon.level)
        val experienceToNext = pokemon.experienceGroup.getExperience(pokemon.level + 1) - pokemon.experienceGroup.getExperience(pokemon.level)

        drawScaledText(
            matrixStack = pMatrixStack,
            text = experience,
            x = (x + 127) - (mcFont.getWidth(experience) * smallTextScale),
            y = y + 125,
            scale = smallTextScale,
            shadow = true
        )

        drawScaledText(
            matrixStack = pMatrixStack,
            text = experienceToNext.toString().text(),
            x = (x + 127) - (mcFont.getWidth(experienceToNext.toString().text()) * smallTextScale),
            y = y + 137,
            scale = smallTextScale,
            shadow = true
        )

        val expRatio = experienceForThisLevel.toFloat() / experienceToNext.toFloat()
        val expBarWidthMax = 55
        val expBarWidth = expRatio * expBarWidthMax

        blitk(
            matrixStack = pMatrixStack,
            texture = CobblemonResources.WHITE,
            x = x + 72,
            y = y + 131,
            width = expBarWidth,
            height = 1,
            textureWidth = expBarWidth / expRatio,
            uOffset = expBarWidthMax - expBarWidth,
            red = 0.2,
            green = 0.65,
            blue = 0.84
        )
    }
}
