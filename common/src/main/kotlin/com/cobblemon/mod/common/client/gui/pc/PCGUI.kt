/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.pc

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.gui.ExitButton
import com.cobblemon.mod.common.client.gui.TypeIcon
import com.cobblemon.mod.common.client.gui.summary.Summary
import com.cobblemon.mod.common.client.gui.summary.widgets.ModelWidget
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.client.storage.ClientPC
import com.cobblemon.mod.common.client.storage.ClientParty
import com.cobblemon.mod.common.net.messages.server.storage.pc.UnlinkPlayerFromPCPacket
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.asTranslated
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.lang
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.client.util.InputUtil
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.sound.SoundEvent
import net.minecraft.text.Text

class PCGUI(
    private val pc: ClientPC,
    private val party: ClientParty
) : Screen(Text.translatable("cobblemon.ui.pc.title")) {

    companion object {
        const val BASE_WIDTH = 349
        const val BASE_HEIGHT = 205
        const val TYPE_SPACER_WIDTH = 128
        const val TYPE_SPACER_HEIGHT = 12
        const val PC_SPACER_WIDTH = 342
        const val PC_SPACER_HEIGHT = 14
        const val PORTRAIT_SIZE = 66
        const val SCALE = 0.5F

        private val baseResource = cobblemonResource("ui/pc/pc_base.png")
        private val portraitBackgroundResource = cobblemonResource("ui/pc/portrait_background.png")
        private val topSpacerResource = cobblemonResource("ui/pc/pc_spacer_top.png")
        private val bottomSpacerResource = cobblemonResource("ui/pc/pc_spacer_bottom.png")
        private val rightSpacerResource = cobblemonResource("ui/pc/pc_spacer_right.png")
        private val typeSpacerResource = cobblemonResource("ui/pc/type_spacer.png")
        private val typeSpacerSingleResource = cobblemonResource("ui/pc/type_spacer_single.png")
        private val typeSpacerDoubleResource = cobblemonResource("ui/pc/type_spacer_double.png")
    }

    private lateinit var storageWidget: StorageWidget
    private var modelWidget: ModelWidget? = null
    internal var previewPokemon: Pokemon? = null

    var ticksElapsed = 0
    var selectPointerOffsetY = 0
    var selectPointerOffsetIncrement = false

    override fun init() {
        val x = (width - BASE_WIDTH) / 2
        val y = (height - BASE_HEIGHT) / 2

        // Add Exit Button
        this.addDrawableChild(
            ExitButton(pX = x + 320, pY = y + 186) {
                playSound(CobblemonSounds.PC_OFF.get())
                MinecraftClient.getInstance().setScreen(null)
                UnlinkPlayerFromPCPacket().sendToServer()
            }
        )

        // Add Forward Button
        this.addDrawableChild(
            NavigationButton(
                pX = x + 221,
                pY = y + 17,
                forward = true
            ) { storageWidget.box += 1 }
        )

        // Add Backwards Button
        this.addDrawableChild(
            NavigationButton(
                pX = x + 119,
                pY = y + 17,
                forward = false
            ) { storageWidget.box -= 1 }
        )

        // Add Storage
        this.storageWidget = StorageWidget(
            pX = x + 85,
            pY = y + 27,
            pcGui = this,
            pc = pc,
            party = party
        )

        this.setPreviewPokemon(null)
        this.addDrawableChild(storageWidget)
        super.init()
    }

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(matrices)

        val x = (width - BASE_WIDTH) / 2
        val y = (height - BASE_HEIGHT) / 2

        // Render Portrait Background
        blitk(
            matrixStack = matrices,
            texture = portraitBackgroundResource,
            x = x + 6,
            y = y + 27,
            width = PORTRAIT_SIZE,
            height = PORTRAIT_SIZE
        )

        // Render Model Portrait
        modelWidget?.render(matrices, mouseX, mouseY, delta)

        // Render Base Resource
        blitk(
            matrixStack = matrices,
            texture = baseResource,
            x = x, y = y,
            width = BASE_WIDTH,
            height = BASE_HEIGHT
        )

        // Render Info Labels
        drawScaledText(
            matrixStack = matrices,
            text = lang("ui.info.nature").bold(),
            x = x + 39,
            y = y + 129.5,
            centered = true,
            scale = SCALE
        )

        drawScaledText(
            matrixStack = matrices,
            text = lang("ui.info.ability").bold(),
            x = x + 39,
            y = y + 146.5,
            centered = true,
            scale = SCALE
        )

        drawScaledText(
            matrixStack = matrices,
            text = lang("ui.moves").bold(),
            x = x + 39,
            y = y + 163.5,
            centered = true,
            scale = SCALE
        )

        // Render Pokemon Info
        val pokemon = previewPokemon
        if (pokemon != null) {
            // Status
            val status = pokemon.status?.status
            if (pokemon.isFainted() || status != null) {
                val statusName = if (pokemon.isFainted()) "fnt" else status?.showdownName
                blitk(
                    matrixStack = matrices,
                    texture = cobblemonResource("ui/battle/battle_status_$statusName.png"),
                    x = x + 34,
                    y = y + 1,
                    height = 7,
                    width = 39,
                    uOffset = 35,
                    textureWidth = 74
                )

                blitk(
                    matrixStack = matrices,
                    texture = cobblemonResource("ui/summary/status_trim.png"),
                    x = x + 34,
                    y = y + 2,
                    height = 6,
                    width = 3
                )

                drawScaledText(
                    matrixStack = matrices,
                    font = CobblemonResources.DEFAULT_LARGE,
                    text = lang("ui.status.$statusName").bold(),
                    x = x + 39,
                    y = y
                )
            }

            // Level
            drawScaledText(
                matrixStack = matrices,
                font = CobblemonResources.DEFAULT_LARGE,
                text = lang("ui.lv").bold(),
                x = x + 6,
                y = y + 1.5,
                shadow = true
            )

            drawScaledText(
                matrixStack = matrices,
                font = CobblemonResources.DEFAULT_LARGE,
                text = pokemon.level.toString().text().bold(),
                x = x + 19,
                y = y + 1.5,
                shadow = true
            )

            // Poké Ball
            val ballResource = cobblemonResource("textures/items/poke_balls/" + pokemon.caughtBall.name.path + ".png")
            blitk(
                matrixStack = matrices,
                texture = ballResource,
                x = (x + 3.5) / SCALE,
                y = (y + 12) / SCALE,
                width = 16,
                height = 16,
                scale = SCALE
            )

            drawScaledText(
                matrixStack = matrices,
                font = CobblemonResources.DEFAULT_LARGE,
                text = pokemon.displayName.bold(),
                x = x + 12,
                y = y + 11.5,
                shadow = true
            )

            if (pokemon.gender != Gender.GENDERLESS) {
                val isMale = pokemon.gender == Gender.MALE
                val textSymbol = if (isMale) "♂".text().bold() else "♀".text().bold()
                drawScaledText(
                    matrixStack = matrices,
                    font = CobblemonResources.DEFAULT_LARGE,
                    text = textSymbol,
                    x = x + 69, // 64 when tag icon is implemented
                    y = y + 11.5,
                    colour = if (isMale) 0x32CBFF else 0xFC5454,
                    shadow = true
                )
            }

            // Held Item
            val heldItem = pokemon.heldItemNoCopy()
            val itemX = x + 3
            val itemY = y + 98
            if (!heldItem.isEmpty) {
                MinecraftClient.getInstance().itemRenderer.renderGuiItemIcon(heldItem, itemX, itemY)
                MinecraftClient.getInstance().itemRenderer.renderGuiItemOverlay(MinecraftClient.getInstance().textRenderer, heldItem, itemX, itemY)
            }

            drawScaledText(
                matrixStack = matrices,
                text = lang("held_item"),
                x = x + 27,
                y = y + 108.5,
                scale = SCALE
            )

            // Shiny Icon
            if (pokemon.shiny) {
                blitk(
                    matrixStack = matrices,
                    texture = Summary.iconShinyResource,
                    x = (x + 63.5) / SCALE,
                    y = (y + 28.5) / SCALE,
                    width = 14,
                    height = 14,
                    scale = SCALE
                )
            }

            blitk(
                matrixStack = matrices,
                texture = if (pokemon.secondaryType != null) typeSpacerDoubleResource else typeSpacerSingleResource,
                x = (x + 7) / SCALE,
                y = (y + 118.5) / SCALE,
                width = TYPE_SPACER_WIDTH,
                height = TYPE_SPACER_HEIGHT,
                scale = SCALE
            )

            TypeIcon(
                x = x + 39,
                y = y + 117,
                type = pokemon.primaryType,
                secondaryType = pokemon.secondaryType,
                doubleCenteredOffset = 5F,
                secondaryOffset = 10F,
                small = true,
                centeredX = true
            ).render(matrices)

            // Nature
            drawScaledText(
                matrixStack = matrices,
                text = pokemon.nature.displayName.asTranslated(),
                x = x + 39,
                y = y + 137,
                centered = true,
                shadow = true,
                scale = SCALE
            )

            // Ability
            drawScaledText(
                matrixStack = matrices,
                text = pokemon.ability.displayName.asTranslated(),
                x = x + 39,
                y = y + 154,
                centered = true,
                shadow = true,
                scale = SCALE
            )

            // Moves
            val moves = pokemon.moveSet.getMoves()
            for (i in moves.indices) {
                drawScaledText(
                    matrixStack = matrices,
                    text = moves[i].displayName,
                    x = x + 39,
                    y = y + 170.5 + (7 * i),
                    centered = true,
                    shadow = true,
                    scale = SCALE
                )
            }

        } else {
            blitk(
                matrixStack = matrices,
                texture = typeSpacerResource,
                x = (x + 7) / SCALE,
                y = (y + 118.5) / SCALE,
                width = TYPE_SPACER_WIDTH,
                height = TYPE_SPACER_HEIGHT,
                scale = SCALE
            )
        }

        // Box Label
        drawScaledText(
            matrixStack = matrices,
            font = CobblemonResources.DEFAULT_LARGE,
            text = Text.translatable("cobblemon.ui.pc.box.title", (this.storageWidget.box + 1).toString()).bold(),
            x = x + 172,
            y = y + 15,
            centered = true
        )

        blitk(
            matrixStack = matrices,
            texture = topSpacerResource,
            x = (x + 86.5) / SCALE,
            y = (y + 13) / SCALE,
            width = PC_SPACER_WIDTH,
            height = PC_SPACER_HEIGHT,
            scale = SCALE
        )

        blitk(
            matrixStack = matrices,
            texture = bottomSpacerResource,
            x = (x + 86.5) / SCALE,
            y = (y + 189) / SCALE,
            width = PC_SPACER_WIDTH,
            height = PC_SPACER_HEIGHT,
            scale = SCALE
        )

        blitk(
            matrixStack = matrices,
            texture = rightSpacerResource,
            x = (x + 275.5) / SCALE,
            y = (y + 184) / SCALE,
            width = 64,
            height = 24,
            scale = SCALE
        )

        super.render(matrices, mouseX, mouseY, delta)

        // Item Tooltip
        if (pokemon != null && !pokemon.heldItemNoCopy().isEmpty) {
            val itemX = x + 3
            val itemY = y + 98
            val itemHovered = mouseX.toFloat() in (itemX.toFloat()..(itemX.toFloat() + 16)) && mouseY.toFloat() in (itemY.toFloat()..(itemY.toFloat() + 16))
            if (itemHovered) renderTooltip(matrices, pokemon.heldItemNoCopy(), mouseX, mouseY)
        }
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        when (keyCode) {
            InputUtil.GLFW_KEY_ESCAPE -> {
                playSound(CobblemonSounds.PC_OFF.get())
                UnlinkPlayerFromPCPacket().sendToServer()
            }
            InputUtil.GLFW_KEY_RIGHT -> {
                playSound(CobblemonSounds.GUI_CLICK.get())
                this.storageWidget.box += 1
            }
            InputUtil.GLFW_KEY_LEFT -> {
                playSound(CobblemonSounds.GUI_CLICK.get())
                this.storageWidget.box -= 1
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    /**
     * Whether this Screen should pause the Game in SinglePlayer
     */
    override fun shouldPause(): Boolean {
        return false
    }

    override fun tick() {
        ticksElapsed++

        // Calculate select pointer offset
        var delayFactor = 3
        if (ticksElapsed % (2 * delayFactor) == 0) selectPointerOffsetIncrement = !selectPointerOffsetIncrement
        if (ticksElapsed % delayFactor == 0) selectPointerOffsetY += if (selectPointerOffsetIncrement) 1 else -1
    }

    private fun playSound(soundEvent: SoundEvent) {
        MinecraftClient.getInstance().soundManager.play(PositionedSoundInstance.master(soundEvent, 1.0F))
    }

    fun setPreviewPokemon(pokemon: Pokemon?) {
        if (pokemon != null) {
            previewPokemon = pokemon

            val x = (width - BASE_WIDTH) / 2
            val y = (height - BASE_HEIGHT) / 2
            modelWidget = ModelWidget(
                pX = x + 6,
                pY = y + 27,
                pWidth = PORTRAIT_SIZE,
                pHeight = PORTRAIT_SIZE,
                pokemon = pokemon.asRenderablePokemon(),
                baseScale = 2F,
                rotationY = 325F,
                offsetY = -10.0
            )
        } else {
            previewPokemon = null;
            modelWidget = null;
        }
    }
}