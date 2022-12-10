/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.summary.widgets

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.gui.summary.Summary
import com.cobblemon.mod.common.client.gui.summary.SummaryButton
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.lang
import java.security.InvalidParameterException
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text

class PartyWidget(
    pX: Int,
    pY: Int,
    val isParty: Boolean,
    val summary: Summary,
    private val partyList: List<Pokemon?>
) : SoundlessWidget(pX, pY, WIDTH, HEIGHT, Text.literal("PartyOverlay")) {

    companion object {
        const val WIDTH = 114
        const val HEIGHT = 113
        private const val SCALE = 0.5F

        private val backgroundResource = cobblemonResource("ui/summary/summary_party_background.png")
        private val swapButtonResource = cobblemonResource("ui/summary/summary_party_swap.png")
        private val swapButtonActiveResource = cobblemonResource("ui/summary/summary_party_swap_active.png")
        private val swapButtonIconResource = cobblemonResource("ui/summary/summary_party_swap_icon.png")
    }

    var swapEnabled: Boolean = false
    var swapSource: Int? = null
    var draggedSlot: PartySlotWidget? = null

    private val partySize = partyList.size
    private val partySlots = arrayListOf<PartySlotWidget>()
    val swapButton: SummaryButton = SummaryButton(
        buttonX = x + 80F,
        buttonY = y - 9F,
        buttonWidth = 26,
        buttonHeight = 14,
        resource = swapButtonResource,
        activeResource = swapButtonActiveResource,
        clickAction = {
            swapEnabled = !swapEnabled
            if (!swapEnabled) {
                swapSource = null
                draggedSlot = null
            }
        }
    )

    init {
        if (partySize > 6 || partySize < 1)
            throw InvalidParameterException("Invalid party size")

        this.partyList.forEachIndexed { index, pokemon ->
            var x = x + 6
            var y = y + 7

            if (index > 0) {
                val isEven = index % 2 == 0
                val offsetIndex = (index - (if (isEven) 0 else 1)) / 2
                val offsetX = if (isEven) 0 else 51
                val offsetY = if (isEven) 0 else 8

                x += offsetX
                y += (32 * offsetIndex) + offsetY
            }

            PartySlotWidget(
                pX = x,
                pY = y,
                partyWidget = this,
                summary = summary,
                pokemon = pokemon,
                index = index,
                isClientPartyMember = isParty
            ).also { widget ->
                this.addWidget(widget)
                partySlots.add(widget)
            }
        }
    }

    override fun render(pMatrixStack: MatrixStack, pMouseX: Int, pMouseY: Int, pPartialTicks: Float) {
        blitk(
            matrixStack = pMatrixStack,
            texture = backgroundResource,
            x = x,
            y = y,
            width = width,
            height = height
        )

        // Label
        drawScaledText(
            matrixStack = pMatrixStack,
            font = CobblemonResources.DEFAULT_LARGE,
            text = lang("ui.party").bold(),
            x = x + 32.5,
            y = y - 14.5,
            centered = true,
            shadow = true
        )

        swapButton.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks)

        blitk(
            matrixStack = pMatrixStack,
            texture = swapButtonIconResource,
            x = (x + 90) / SCALE,
            y = (y - 6) / SCALE,
            width = 12,
            height = 17,
            scale = SCALE
        )

        partySlots.forEach { it.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks) }

        if (draggedSlot != null) {
            pMatrixStack.push()
            pMatrixStack.translate(0.0, 0.0, 500.0)
            draggedSlot!!.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks)
            pMatrixStack.pop()
        }
    }

    override fun mouseClicked(pMouseX: Double, pMouseY: Double, pButton: Int): Boolean {
        if (swapButton.isHovered) {
            swapButton.onPress()
            swapButton.isActive = swapEnabled
        }

        if (swapEnabled) {
            val index = getIndexFromPos(pMouseX, pMouseY)
            if (index > -1) {
                val sourcePokemon = partyList[index]
                if (sourcePokemon != null) {
                    swapSource = index
                    draggedSlot = PartySlotWidget(
                        pX = pMouseX - (PartySlotWidget.WIDTH / 2),
                        pY = pMouseY - (PartySlotWidget.HEIGHT / 2),
                        partyWidget = this,
                        summary = summary,
                        pokemon = sourcePokemon,
                        index = -1,
                        isClientPartyMember = isParty
                    )
                }
            }
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton)
    }

    override fun mouseReleased(pMouseX: Double, pMouseY: Double, pButton: Int): Boolean {
        if (swapEnabled) {
            if (swapSource != null) {
                val index = getIndexFromPos(pMouseX, pMouseY)
                if (index > -1 && index != swapSource) {
                    summary.swapPartySlot(swapSource!!, index)
                }
                swapSource = null
                draggedSlot = null
            }
        }
        return super.mouseReleased(pMouseX, pMouseY, pButton)
    }

    fun enableSwap(boolean: Boolean = true) {
        swapEnabled = boolean
        swapButton.isActive = boolean
    }

    private fun getIndexFromPos(mouseX: Double, mouseY: Double): Int {
        for (index in 0..5) {
            var posX = x + 6
            var posY = y + 7
            if (index > 0) {
                val isEven = index % 2 == 0
                val offsetIndex = (index - (if (isEven) 0 else 1)) / 2
                val offsetX = if (isEven) 0 else 51
                val offsetY = if (isEven) 0 else 8

                posX += offsetX
                posY += (32 * offsetIndex) + offsetY
            }
            if (mouseX.toInt() in posX..(posX + PartySlotWidget.WIDTH)
                && mouseY.toInt() in posY..(posY + PartySlotWidget.HEIGHT)) {
                return index
            }
        }
        return -1
    }

    fun isWithinScreen(mouseX: Double, mouseY: Double): Boolean = mouseX.toInt() in x..(x + WIDTH)
            && mouseY.toInt() in y..(y + HEIGHT)
}