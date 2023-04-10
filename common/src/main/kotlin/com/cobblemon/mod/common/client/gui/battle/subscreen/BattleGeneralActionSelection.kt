/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.battle.subscreen

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.battles.model.actor.ActorType
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.battle.SingleActionRequest
import com.cobblemon.mod.common.client.gui.battle.BattleGUI
import com.cobblemon.mod.common.client.gui.battle.widgets.BattleOptionTile
import com.cobblemon.mod.common.util.battleLang
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.Selectable
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.client.sound.SoundManager
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.MutableText
import net.minecraft.util.Identifier
class BattleGeneralActionSelection(
    battleGUI: BattleGUI,
    request: SingleActionRequest
) : BattleActionSelection(
    battleGUI,
    request,
    BattleGUI.OPTION_ROOT_X,
    MinecraftClient.getInstance().window.scaledHeight - BattleGUI.OPTION_VERTICAL_OFFSET,
    (BattleOptionTile.OPTION_WIDTH + 3 * BattleGUI.OPTION_HORIZONTAL_SPACING).toInt(),
    (BattleOptionTile.OPTION_HEIGHT + 3 * BattleGUI.OPTION_VERTICAL_SPACING).toInt(),
    battleLang("choose_action")
) {
    val tiles = mutableListOf<BattleOptionTile>()
    init {
        var rank = 0

        addOption(rank++, battleLang("ui.fight"), BattleGUI.fightResource) {
            playDownSound(MinecraftClient.getInstance().soundManager)
            battleGUI.changeActionSelection(BattleMoveSelection(battleGUI, request))
        }

        if (request.moveSet?.trapped != true) {
            addOption(rank++, battleLang("ui.switch"), BattleGUI.switchResource) {
                battleGUI.changeActionSelection(BattleSwitchPokemonSelection(battleGUI, request))
                playDownSound(MinecraftClient.getInstance().soundManager)
            }
        }

        CobblemonClient.battle?.let { battle ->
            if (battle.battleFormat.battleType.pokemonPerSide == 1 && battle.side2.actors.first().type == ActorType.WILD) {
                addOption(rank++, battleLang("ui.capture"), BattleGUI.bagResource) {
                    CobblemonClient.battle?.minimised = true
                    MinecraftClient.getInstance().player?.sendMessage(battleLang("throw_pokeball_prompt"), false)
                    playDownSound(MinecraftClient.getInstance().soundManager)
                }

                addOption(rank++, battleLang("ui.run"), BattleGUI.runResource) {
                    CobblemonClient.battle?.minimised = true
                    MinecraftClient.getInstance().player?.sendMessage(battleLang("run_prompt"), false)
                    playDownSound(MinecraftClient.getInstance().soundManager)
                }
            }
        }
    }

    private fun addOption(rank: Int, text: MutableText, texture: Identifier, onClick: () -> Unit) {
        val startY = MinecraftClient.getInstance().window.scaledHeight - BattleGUI.OPTION_VERTICAL_OFFSET
        val x = if (rank % 2 == 0) BattleGUI.OPTION_ROOT_X else BattleGUI.OPTION_ROOT_X + BattleGUI.OPTION_HORIZONTAL_SPACING + BattleOptionTile.OPTION_WIDTH
        val y = if (rank > 1) startY + BattleOptionTile.OPTION_HEIGHT + BattleGUI.OPTION_HORIZONTAL_SPACING else startY
        tiles.add(
            BattleOptionTile(
                battleGUI = battleGUI,
                x = x,
                y = y,
                resource = texture,
                text = text,
                onClick = onClick
            )
        )
    }

    override fun renderButton(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        for (tile in tiles) {
            tile.render(matrices, mouseX, mouseY, delta)
        }
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        return tiles.any { it.mouseClicked(mouseX, mouseY, button) }
    }

    override fun appendDefaultNarrations(builder: NarrationMessageBuilder) {
    }

    override fun playDownSound(soundManager: SoundManager) {
        soundManager.play(PositionedSoundInstance.master(CobblemonSounds.GUI_CLICK, 1.0F))
    }

    override fun getType() = Selectable.SelectionType.NONE
}