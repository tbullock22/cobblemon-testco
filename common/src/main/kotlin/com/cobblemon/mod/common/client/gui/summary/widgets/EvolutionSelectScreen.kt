/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.summary.widgets

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.pokemon.evolution.EvolutionDisplay
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.gui.TypeIcon
import com.cobblemon.mod.common.client.gui.drawProfilePokemon
import com.cobblemon.mod.common.client.gui.summary.SummaryButton
import com.cobblemon.mod.common.client.gui.summary.widgets.common.SummaryScrollList
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.pokemon.FormData
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.lang
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Quaternion
import net.minecraft.util.math.Vec3f

class EvolutionSelectScreen(
    x: Int,
    y: Int,
    val pokemon: Pokemon
): SummaryScrollList<EvolutionSelectScreen.EvolveSlot>(
    x,
    y,
    lang("ui.evolution"),
    SLOT_HEIGHT + SLOT_SPACING
) {
    companion object {
        const val SLOT_HEIGHT = 25
        const val SLOT_SPACING = 5
        const val PORTRAIT_DIAMETER = 25

        private val slotResource = cobblemonResource("ui/summary/summary_evolve_slot.png")
        private val buttonResource = cobblemonResource("ui/summary/summary_evolve_select_button.png")
    }

    private var entriesCreated = false

    public override fun addEntry(entry: EvolveSlot): Int {
        return super.addEntry(entry)
    }

    override fun render(poseStack: MatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!entriesCreated) {
            entriesCreated = true
            pokemon.evolutionProxy.client().map { EvolveSlot(pokemon, it) }.forEach { entry -> this.addEntry(entry) }
        }
        super.render(poseStack, mouseX, mouseY, partialTicks)
    }

    class EvolveSlot(private val pokemon: Pokemon, private val evolution: EvolutionDisplay) : Entry<EvolveSlot>() {
        val client: MinecraftClient = MinecraftClient.getInstance()
        val form: FormData = evolution.species.getForm(evolution.aspects)
        val selectButton: SummaryButton = SummaryButton(
            buttonX = 0F,
            buttonY = 0F,
            buttonWidth = 40,
            buttonHeight = 10,
            clickAction = {
                MinecraftClient.getInstance().player?.closeScreen()
                MinecraftClient.getInstance().player?.sendMessage(lang("ui.evolve.into", pokemon.displayName, evolution.species.translatedName))
                pokemon.evolutionProxy.client().start(this.evolution)
            },
            text = lang("ui.evolve"),
            resource = buttonResource,
            boldText = true,
            largeText = false,
            textScale = 0.5F
        )

        override fun getNarration() = evolution.species.translatedName

        override fun render(
            poseStack: MatrixStack,
            index: Int,
            rowTop: Int,
            rowLeft: Int,
            rowWidth: Int,
            rowHeight: Int,
            mouseX: Int,
            mouseY: Int,
            isHovered: Boolean,
            partialTicks: Float
        ) {
            var x = rowLeft - 3
            var y = rowTop

            blitk(
                matrixStack = poseStack,
                texture = slotResource,
                x = x,
                y = y,
                height = SLOT_HEIGHT,
                width = rowWidth
            )

            drawScaledText(
                matrixStack = poseStack,
                font = CobblemonResources.DEFAULT_LARGE,
                text = evolution.species.translatedName.bold(),
                x = x + 4,
                y = y + 2,
                shadow = true
            )

            TypeIcon(
                x = x + 12,
                y = y + 13.5,
                type = form.primaryType,
                secondaryType = form.secondaryType,
                doubleCenteredOffset = 5F,
                secondaryOffset = 9.5F,
                small = true,
                centeredX = true
            ).render(poseStack)

            selectButton.setPosFloat(x + 23F, y + 13F)
            selectButton.render(poseStack, mouseX, mouseY, partialTicks)

            // Render Pokémon
            poseStack.push()
            poseStack.translate(x + (PORTRAIT_DIAMETER / 2) + 65.0, y + 0.0, 0.0)
            poseStack.scale(2.5F, 2.5F, 1F)
            drawProfilePokemon(
                species = form.species,
                aspects = form.aspects.toSet(),
                matrixStack = poseStack,
                rotation = Quaternion.fromEulerXyzDegrees(Vec3f(13F, 35F, 0F)),
                state = null,
                scale = 6F
            )
            poseStack.pop()
        }

        override fun mouseClicked(d: Double, e: Double, i: Int): Boolean {
            if (selectButton.isHovered) {
                selectButton.onPress()
                return true
            }
            return false
        }
    }
}