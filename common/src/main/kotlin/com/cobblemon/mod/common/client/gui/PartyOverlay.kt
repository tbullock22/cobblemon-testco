/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.gui.drawPortraitPokemon
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.gui.battle.BattleGUI
import com.cobblemon.mod.common.client.keybind.boundKey
import com.cobblemon.mod.common.client.keybind.keybinds.HidePartyBinding
import com.cobblemon.mod.common.client.keybind.keybinds.PokeNavigatorBinding
import com.cobblemon.mod.common.client.keybind.keybinds.SummaryBinding
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.client.render.getDepletableRedGreen
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.lang
import com.mojang.blaze3d.systems.RenderSystem
import kotlin.math.roundToInt
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.hud.InGameHud
import net.minecraft.client.gui.screen.ChatScreen
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text

class PartyOverlay : InGameHud(MinecraftClient.getInstance(), MinecraftClient.getInstance().itemRenderer) {

    val partySlot = cobblemonResource("ui/party/party_slot.png")
    val partySlotActive = cobblemonResource("ui/party/party_slot_active.png")
    val partySlotFainted = cobblemonResource("ui/party/party_slot_fainted.png")
    val partySlotFaintedActive = cobblemonResource("ui/party/party_slot_fainted_active.png")
    val partySlotCollapsed = cobblemonResource("ui/party/party_slot_collapsed.png")
    val genderIconMale = cobblemonResource("ui/party/party_gender_male.png")
    val genderIconFemale = cobblemonResource("ui/party/party_gender_female.png")
    val portraitBackground = cobblemonResource("ui/party/party_slot_portrait_background.png")
    val screenExemptions: List<Class<out Screen>> = listOf(
        ChatScreen::class.java,
        BattleGUI::class.java
    )

    override fun render(matrixStack: MatrixStack, partialDeltaTicks: Float) {
        val minecraft = MinecraftClient.getInstance()
        val player = minecraft.player

        // Hiding if a Screen is open and not exempt
        if (minecraft.currentScreen != null) {
            if (!screenExemptions.contains(minecraft.currentScreen?.javaClass as Class<out Screen>))
                return
        }
        if (minecraft.options.debugEnabled) {
            return
        }
        // Hiding if toggled via Keybind
        if (HidePartyBinding.shouldHide) {
            return
        }

        val panelX = 0
        val party = CobblemonClient.storage.myParty
        if (party.slots.none { it != null }) {
            if (CobblemonClient.clientPlayerData.promptStarter &&
                !CobblemonClient.clientPlayerData.starterLocked &&
                !CobblemonClient.clientPlayerData.starterSelected &&
                !CobblemonClient.checkedStarterScreen
            ) {
                // ToDo replace back to PokeNav once reimplemented
                drawScaledText(
                    matrixStack = matrixStack,
                    text = lang("ui.starter.chooseyourstarter", SummaryBinding.boundKey().localizedText),
                    x = minecraft.window.scaledWidth / 2,
                    y = 70,
                    centered = true,
                    shadow = true
                )
            }

            return
        }

        val slotHeight = 30
        val slotWidth = 57
        val slotSpacing = 4
        val portraitDiameter = 21
        val totalHeight = party.slots.size * slotHeight
        val midY = minecraft.window.scaledHeight / 2
        val startY = (midY - totalHeight / 2) - ((slotSpacing * 5) / 2)
        val frameOffsetX = 17
        val frameOffsetY = 2
        val selectedSlot = CobblemonClient.storage.selectedSlot

        val scaleIt: (Int) -> Int = { (it * minecraft.window.scaleFactor).toInt() }
        val downscaleIt: (Number) -> Int = { (it.toFloat() / 4F * minecraft.window.scaleFactor).roundToInt() }

        party.forEachIndexed { index, pokemon ->
            if (pokemon != null) {
                val selectedOffsetX = if (selectedSlot == index) 6 else 0
                val y = startY + ((slotHeight + slotSpacing) * index) + frameOffsetY

                blitk(
                    matrixStack = matrixStack,
                    texture = portraitBackground,
                    x = panelX + frameOffsetX + selectedOffsetX,
                    y = y,
                    height = portraitDiameter,
                    width = portraitDiameter
                )

                val height = minecraft.window.height
                val scaledTotalHeight = downscaleIt(totalHeight)

                RenderSystem.enableScissor(
                    ((panelX + frameOffsetX + selectedOffsetX) * minecraft.window.scaleFactor).roundToInt(),
                    height / 2 + scaledTotalHeight * 2 + scaleIt(8) - scaleIt(((11 * index) + ((portraitDiameter + 2) * (index + 1))) - 2),
                    (portraitDiameter * minecraft.window.scaleFactor).roundToInt(),
                    (portraitDiameter * minecraft.window.scaleFactor).roundToInt()
                )

                val matrixStack = MatrixStack()
                matrixStack.translate(
                    panelX + frameOffsetX + selectedOffsetX + portraitDiameter / 2.0 - 1.0,
                    y.toDouble() - 12,
                    0.0
                )
                matrixStack.scale(1F, 1F, 1F)

                drawPortraitPokemon(pokemon.species, pokemon.aspects, matrixStack)

                RenderSystem.disableScissor()
            }
        }

        // Some long models end up translated such that the text ends up behind the invisible viewport rendered bits.
        // Kinda messed up but pushing these next elements forward seems a cheap enough fix.
        matrixStack.push()
        matrixStack.translate(0.0, 0.0, 300.0)
        party.slots.forEachIndexed { index, pokemon ->
            val selectedOffsetX = if (selectedSlot == index) 6 else 0
            val slotTexture = if (pokemon != null)
                    if (pokemon.isFainted())
                        if (selectedSlot == index) partySlotFaintedActive else partySlotFainted
                    else
                        if (selectedSlot == index) partySlotActive else partySlot
                else partySlotCollapsed
            blitk(
                matrixStack = matrixStack,
                texture = slotTexture,
                x = panelX,
                y = startY + ((slotHeight + slotSpacing) * index),
                height = slotHeight,
                width = slotWidth
            )

            if (pokemon != null) {
                val hpRatio = pokemon.currentHealth / pokemon.hp.toFloat()
                val barHeightMax = 18
                val hpBarWidth = 2
                val hpBarHeight = hpRatio * barHeightMax
                val expForThisLevel = pokemon.experience - if (pokemon.level == 1) 0 else pokemon.experienceGroup.getExperience(pokemon.level)
                val expToNextLevel = pokemon.experienceGroup.getExperience(pokemon.level + 1) - pokemon.experienceGroup.getExperience(pokemon.level)
                val expRatio = expForThisLevel / expToNextLevel.toFloat()

                val expBarWidth = 1
                val expBarHeight = expRatio * barHeightMax

                val (r, g) = getDepletableRedGreen(hpRatio)
                val b = 0

                blitk(
                    matrixStack = matrixStack,
                    texture = CobblemonResources.WHITE,
                    x = panelX + selectedOffsetX + 41,
                    y = startY + 5 + ((slotHeight + slotSpacing) * index) + (barHeightMax - hpBarHeight),
                    width = hpBarWidth,
                    height = hpBarHeight,
                    textureHeight = hpBarHeight / hpRatio,
                    vOffset = barHeightMax - hpBarHeight,
                    red = r,
                    green = g,
                    blue = b
                )

                blitk(
                    matrixStack = matrixStack,
                    texture = CobblemonResources.WHITE,
                    x = panelX + selectedOffsetX + 44,
                    y = startY + 5 + ((slotHeight + slotSpacing) * index) + (barHeightMax - expBarHeight),
                    width = expBarWidth,
                    height = expBarHeight,
                    textureHeight = expBarHeight / expRatio,
                    vOffset = barHeightMax - expBarHeight,
                    red = 0,
                    green = 0.784,
                    blue = 1.0
                )

                val fontScale = 0.5F

                drawScaledText(
                    matrixStack = matrixStack,
                    text = pokemon.species.translatedName,
                    x = panelX + selectedOffsetX + 2.5F,
                    y = startY + 1 + ((slotHeight + slotSpacing) * index) + slotHeight * 0.84F - 1F,
                    scale = fontScale
                )

                drawScaledText(
                    matrixStack = matrixStack,
                    text = lang("ui.lv"),
                    x = panelX + selectedOffsetX + 2.5F,
                    y = startY + ((slotHeight + slotSpacing) * index) + slotHeight * 0.84F - 10.75F,
                    scale = 0.45F
                )

                val width = minecraft.textRenderer.getWidth(pokemon.level.toString())
                drawScaledText(
                    matrixStack = matrixStack,
                    text = Text.translatable(pokemon.level.toString()),
                    x = panelX + selectedOffsetX + 6.5F - width / 4F,
                    y = startY + ((slotHeight + slotSpacing) * index) + slotHeight * 0.84F - 7F,
                    scale = 0.45F
                )

                if (pokemon.gender != Gender.GENDERLESS) {
                    blitk(
                        matrixStack = matrixStack,
                        texture = if (pokemon.gender == Gender.MALE) genderIconMale else genderIconFemale,
                        x = (panelX + selectedOffsetX + 35) * 2,
                        y = (startY + 1 + ((slotHeight + slotSpacing) * index) + slotHeight * 0.84F - 1F) * 2,
                        height = 7,
                        width = 5,
                        scale = 0.5F
                    )
                }

                val stateIcon = pokemon.state.getIcon(pokemon)
                if (stateIcon != null) {
                    blitk(
                        matrixStack = matrixStack,
                        texture = stateIcon,
                        x = ((panelX + selectedOffsetX + 2) * 2) + 1,
                        y = (startY + ((slotHeight + slotSpacing) * index) + frameOffsetY + 1) * 2,
                        height = 17,
                        width = 24,
                        scale = 0.5F
                    )
                }

                val ballIcon = cobblemonResource("ui/ball/" + pokemon.caughtBall.name.path + ".png")
                val ballHeight = 22
                blitk(
                    matrixStack = matrixStack,
                    texture = ballIcon,
                    x = ((panelX + selectedOffsetX + 38) * 2) + 1,
                    y = (startY + ((slotHeight + slotSpacing) * index) + 22) * 2,
                    height = ballHeight,
                    width = 18,
                    vOffset = if (stateIcon != null) ballHeight else 0,
                    textureHeight = ballHeight * 2,
                    scale = 0.5F
                )
            }
        }
        matrixStack.pop()
    }
}