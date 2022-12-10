/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.battle

import com.cobblemon.mod.common.battles.ShowdownActionResponse
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.battle.ClientBattleActor
import com.cobblemon.mod.common.client.battle.SingleActionRequest
import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleActionSelection
import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleGeneralActionSelection
import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleSwitchPokemonSelection
import com.cobblemon.mod.common.client.gui.battle.widgets.BattleMessagePane
import com.cobblemon.mod.common.client.keybind.boundKey
import com.cobblemon.mod.common.client.keybind.keybinds.PartySendBinding
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.util.battleLang
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.util.math.MatrixStack
class BattleGUI : Screen(battleLang("gui.title")) {
    companion object {
        const val OPTION_VERTICAL_SPACING = 3
        const val OPTION_HORIZONTAL_SPACING = 3
        const val OPTION_ROOT_X = 12
        const val OPTION_VERTICAL_OFFSET = 85

        val fightResource = cobblemonResource("ui/battle/battle_menu_fight.png")
        val bagResource = cobblemonResource("ui/battle/battle_menu_bag.png")
        val switchResource = cobblemonResource("ui/battle/battle_menu_switch.png")
        val runResource = cobblemonResource("ui/battle/battle_menu_run.png")
    }

    var opacity = 0F
    val actor = CobblemonClient.battle?.side1?.actors?.find { it.uuid == MinecraftClient.getInstance().player?.uuid }

    var queuedActions = mutableListOf<() -> Unit>()

    override fun init() {
        super.init()
        addDrawableChild(BattleMessagePane(CobblemonClient.battle!!.messages))
    }

    fun changeActionSelection(newSelection: BattleActionSelection?) {
        val current = children().find { it is BattleActionSelection }
        queuedActions.add {
            current?.let(this::remove)
            if (newSelection != null) {
                addDrawableChild(newSelection)
            }
        }
    }

    fun getCurrentActionSelection() = children().filterIsInstance<BattleActionSelection>().firstOrNull()

    fun selectAction(request: SingleActionRequest, response: ShowdownActionResponse) {
        val battle = CobblemonClient.battle ?: return
        if (request.response == null) {
            request.response = response
            changeActionSelection(null)
            battle.checkForFinishedChoosing()
        }
    }

    override fun render(poseStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        opacity = CobblemonClient.battleOverlay.opacityRatio.toFloat()
        children().filterIsInstance<BattleMessagePane>().forEach { it.opacity = opacity.coerceAtLeast(0.3F) }

        queuedActions.forEach { it() }
        queuedActions.clear()
        super.render(poseStack, mouseX, mouseY, delta)
        val battle = CobblemonClient.battle
        if (battle == null) {
            close()
            return
        } else if (CobblemonClient.battleOverlay.opacityRatio <= 0.1 && CobblemonClient.battle?.minimised == true) {
            close()
            return
        }

        if (actor != null) {
            if (battle.mustChoose) {
                if (getCurrentActionSelection() == null) {
                    val unanswered = battle.getFirstUnansweredRequest()
                    if (unanswered != null) {
                        changeActionSelection(deriveRootActionSelection(actor, unanswered))
                    }
                }
            } else if (getCurrentActionSelection() != null) {
                changeActionSelection(null)
            }
        }

        val currentSelection = getCurrentActionSelection()
        if (currentSelection == null || currentSelection is BattleGeneralActionSelection ) {
            drawScaledText(
                matrixStack = poseStack,
                text = battleLang("ui.hide_label", PartySendBinding.boundKey().localizedText),
                x = MinecraftClient.getInstance().window.scaledWidth / 2,
                y = (MinecraftClient.getInstance().window.scaledHeight / 5),
                opacity = 0.75F * opacity,
                centered = true
            )
        }


        queuedActions.forEach { it() }
        queuedActions.clear()
    }

    fun deriveRootActionSelection(actor: ClientBattleActor, request: SingleActionRequest): BattleActionSelection {
        return if (request.forceSwitch) {
            BattleSwitchPokemonSelection(this, request)
        } else {
            BattleGeneralActionSelection(this, request)
        }
    }

    override fun shouldPause() = false
    override fun close() {
        super.close()
        CobblemonClient.battle?.minimised = true
    }

    override fun charTyped(chr: Char, modifiers: Int): Boolean {
        if (chr.toString() == PartySendBinding.boundKey().localizedText.string && CobblemonClient.battleOverlay.opacity == BattleOverlay.MAX_OPACITY) {
            val battle = CobblemonClient.battle ?: return false
            battle.minimised = !battle.minimised
            return true
        }
        return super.charTyped(chr, modifiers)
    }
}