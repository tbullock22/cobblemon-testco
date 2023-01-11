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
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.gui.battle.BattleGUI
import com.cobblemon.mod.common.client.keybind.boundKey
import com.cobblemon.mod.common.client.keybind.keybinds.HidePartyBinding
import com.cobblemon.mod.common.client.keybind.keybinds.SummaryBinding
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.client.render.getDepletableRedGreen
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.lang
import kotlin.math.roundToInt
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.gui.hud.InGameHud
import net.minecraft.client.gui.screen.ChatScreen
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.util.math.MatrixStack

class PartyOverlay : InGameHud(MinecraftClient.getInstance(), MinecraftClient.getInstance().itemRenderer) {

    companion object {
        private const val SLOT_HEIGHT = 30
        private const val SLOT_WIDTH = 57
        private const val SLOT_SPACING = 4
        private const val PORTRAIT_DIAMETER = 21
        private const val SCALE = 0.5F

        private val partySlot = cobblemonResource("ui/party/party_slot.png")
        private val partySlotActive = cobblemonResource("ui/party/party_slot_active.png")
        private val partySlotFainted = cobblemonResource("ui/party/party_slot_fainted.png")
        private val partySlotFaintedActive = cobblemonResource("ui/party/party_slot_fainted_active.png")
        private val partySlotCollapsed = cobblemonResource("ui/party/party_slot_collapsed.png")
        private val genderIconMale = cobblemonResource("ui/party/party_gender_male.png")
        private val genderIconFemale = cobblemonResource("ui/party/party_gender_female.png")
        private val portraitBackground = cobblemonResource("ui/party/party_slot_portrait_background.png")
    }

    private val screenExemptions: List<Class<out Screen>> = listOf(
        ChatScreen::class.java,
        BattleGUI::class.java
    )

    override fun render(matrixStack: MatrixStack, partialDeltaTicks: Float) {
        val minecraft = MinecraftClient.getInstance()

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

        val totalHeight = party.slots.size * SLOT_HEIGHT
        val midY = minecraft.window.scaledHeight / 2
        val startY = (midY - totalHeight / 2) - ((SLOT_SPACING * 5) / 2)
        val portraitFrameOffsetX = 17
        val portraitFrameOffsetY = 2
        val selectedSlot = CobblemonClient.storage.selectedSlot

        val scaleIt: (Int) -> Int = { (it * minecraft.window.scaleFactor).toInt() }
        val downscaleIt: (Number) -> Int = { (it.toFloat() / 4F * minecraft.window.scaleFactor).roundToInt() }

        party.forEachIndexed { index, pokemon ->
            if (pokemon != null) {
                val selectedOffsetX = if (selectedSlot == index) 6 else 0
                val indexOffsetY = (SLOT_HEIGHT + SLOT_SPACING) * index
                val y = startY + indexOffsetY + portraitFrameOffsetY

                blitk(
                    matrixStack = matrixStack,
                    texture = portraitBackground,
                    x = panelX + portraitFrameOffsetX + selectedOffsetX,
                    y = y,
                    height = PORTRAIT_DIAMETER,
                    width = PORTRAIT_DIAMETER
                )

                DrawableHelper.enableScissor(
                    panelX + portraitFrameOffsetX + selectedOffsetX,
                    y,
                    panelX + portraitFrameOffsetX + selectedOffsetX + PORTRAIT_DIAMETER,
                    y + PORTRAIT_DIAMETER
                )

                val matrixStack = MatrixStack()
                matrixStack.translate(
                    panelX + portraitFrameOffsetX + selectedOffsetX + PORTRAIT_DIAMETER / 2.0 - 1.0,
                    y.toDouble() - 12,
                    0.0
                )
                matrixStack.scale(1F, 1F, 1F)

                drawPortraitPokemon(pokemon.species, pokemon.aspects, matrixStack)

                DrawableHelper.disableScissor()
            }
        }

        // Some long models end up translated such that the text ends up behind the invisible viewport rendered bits.
        // Kinda messed up but pushing these next elements forward seems a cheap enough fix.
        matrixStack.push()
        matrixStack.translate(0.0, 0.0, 300.0)
        party.slots.forEachIndexed { index, pokemon ->
            val selectedOffsetX = if (selectedSlot == index) 6 else 0
            val indexOffsetY = (SLOT_HEIGHT + SLOT_SPACING) * index
            val indexY = startY + indexOffsetY

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
                y = indexY,
                height = SLOT_HEIGHT,
                width = SLOT_WIDTH
            )

            if (pokemon != null) {
                val stateIcon = pokemon.state.getIcon(pokemon)
                if (stateIcon != null) {
                    blitk(
                        matrixStack = matrixStack,
                        texture = stateIcon,
                        x = (panelX + selectedOffsetX + 2.5) / SCALE,
                        y = (indexY + portraitFrameOffsetY + 1) / SCALE,
                        height = 17,
                        width = 24,
                        scale = SCALE
                    )
                }

                drawScaledText(
                    matrixStack = matrixStack,
                    text = lang("ui.lv"),
                    x = panelX + selectedOffsetX + 8.5F,
                    y = indexY + 14,
                    scale = SCALE,
                    centered = true,
                    shadow = true
                )

                drawScaledText(
                    matrixStack = matrixStack,
                    text = pokemon.level.toString().text(),
                    x = panelX + selectedOffsetX + 8.5F,
                    y = indexY + 18.5,
                    scale = SCALE,
                    centered = true,
                    shadow = true
                )

                drawScaledText(
                    matrixStack = matrixStack,
                    text = pokemon.displayName,
                    x = panelX + selectedOffsetX + 2.5F,
                    y = indexY + 25,
                    scale = SCALE
                )

                if (pokemon.gender != Gender.GENDERLESS) {
                    blitk(
                        matrixStack = matrixStack,
                        texture = if (pokemon.gender == Gender.MALE) genderIconMale else genderIconFemale,
                        x = (panelX + selectedOffsetX + 35) / SCALE,
                        y = (indexY + 25)  / SCALE,
                        height = 7,
                        width = 5,
                        scale = SCALE
                    )
                }

                val hpRatio = pokemon.currentHealth / pokemon.hp.toFloat()
                val barHeightMax = 18
                val hpBarWidth = 2
                val hpBarHeight = hpRatio * barHeightMax

                val (red, green) = getDepletableRedGreen(hpRatio)

                blitk(
                    matrixStack = matrixStack,
                    texture = CobblemonResources.WHITE,
                    x = panelX + selectedOffsetX + 41,
                    y = indexY + (barHeightMax - hpBarHeight) + 5,
                    width = hpBarWidth,
                    height = hpBarHeight,
                    textureHeight = hpBarHeight / hpRatio,
                    vOffset = barHeightMax - hpBarHeight,
                    red = red * 0.8F,
                    green = green * 0.8F,
                    blue = 0.27F
                )

                val expForThisLevel = pokemon.experience - if (pokemon.level == 1) 0 else pokemon.experienceGroup.getExperience(pokemon.level)
                val expToNextLevel = pokemon.experienceGroup.getExperience(pokemon.level + 1) - pokemon.experienceGroup.getExperience(pokemon.level)
                val expRatio = expForThisLevel / expToNextLevel.toFloat()

                val expBarWidth = 1
                val expBarHeight = expRatio * barHeightMax

                blitk(
                    matrixStack = matrixStack,
                    texture = CobblemonResources.WHITE,
                    x = panelX + selectedOffsetX + 44,
                    y = indexY + (barHeightMax - expBarHeight) + 5,
                    width = expBarWidth,
                    height = expBarHeight,
                    textureHeight = expBarHeight / expRatio,
                    vOffset = barHeightMax - expBarHeight,
                    red = 0.2,
                    green = 0.65,
                    blue = 0.84
                )

                val ballIcon = cobblemonResource("ui/ball/" + pokemon.caughtBall.name.path + ".png")
                val ballHeight = 22
                blitk(
                    matrixStack = matrixStack,
                    texture = ballIcon,
                    x = (panelX + selectedOffsetX + 38.5) / SCALE,
                    y = (indexY + 22) / SCALE,
                    height = ballHeight,
                    width = 18,
                    vOffset = if (stateIcon != null) ballHeight else 0,
                    textureHeight = ballHeight * 2,
                    scale = SCALE
                )

                val status = pokemon.status?.status
                if (!pokemon.isFainted() && status != null) {
                    val statusName = status.showdownName
                    blitk(
                        matrixStack = matrixStack,
                        texture = cobblemonResource("ui/party/status_$statusName.png"),
                        x = panelX + selectedOffsetX + 46,
                        y = indexY + 8,
                        height = 14,
                        width = 4
                    )
                }
            }
        }
        matrixStack.pop()
    }
}