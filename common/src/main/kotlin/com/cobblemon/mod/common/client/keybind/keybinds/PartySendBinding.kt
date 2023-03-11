/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.keybind.keybinds

import com.cobblemon.mod.common.CobblemonNetwork.sendToServer
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.gui.battle.BattleGUI
import com.cobblemon.mod.common.client.keybind.CobblemonBlockingKeyBinding
import com.cobblemon.mod.common.client.keybind.KeybindCategories
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.server.BattleChallengePacket
import com.cobblemon.mod.common.net.messages.server.SendOutPokemonPacket
import com.cobblemon.mod.common.util.traceFirstEntityCollision
import javax.swing.plaf.basic.BasicSliderUI.ScrollListener
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.InputUtil
import net.minecraft.entity.LivingEntity

object PartySendBinding : CobblemonBlockingKeyBinding(
    "key.cobblemon.throwpartypokemon",
    InputUtil.Type.KEYSYM,
    InputUtil.GLFW_KEY_R,
    KeybindCategories.COBBLEMON_CATEGORY
) {
    var secondsSinceActioned = 0F

    fun actioned() {
        secondsSinceActioned = 0F
    }

    fun canAction() = secondsSinceActioned > 0.75

    override fun onTick() {
        if (secondsSinceActioned < 100) {
            secondsSinceActioned += MinecraftClient.getInstance().tickDelta
        }

        super.onTick()
    }

    override fun onRelease() {
        if (!canAction() || timeDown > 1F) {
            return
        }

        val player = MinecraftClient.getInstance().player ?: return

        val battle = CobblemonClient.battle
        if (battle != null) {
            battle.minimised = !battle.minimised
            if (!battle.minimised) {
                MinecraftClient.getInstance().setScreen(BattleGUI())
                actioned()
            }
            return
        }

        if (CobblemonClient.storage.selectedSlot != -1 && MinecraftClient.getInstance().currentScreen == null) {
            val pokemon = CobblemonClient.storage.myParty.get(CobblemonClient.storage.selectedSlot)
            if (pokemon != null && pokemon.currentHealth > 0 ) {
                val targetedPokemon = player.traceFirstEntityCollision(entityClass = LivingEntity::class.java, ignoreEntity = player)
                if (targetedPokemon != null && (targetedPokemon !is PokemonEntity || targetedPokemon.canBattle(player))) {
                    sendToServer(BattleChallengePacket(targetedPokemon.id, pokemon.uuid))
                } else {
                    sendToServer(SendOutPokemonPacket(CobblemonClient.storage.selectedSlot))
                }
                actioned()
            }
        }
    }

    override fun onPress() {

    }
}