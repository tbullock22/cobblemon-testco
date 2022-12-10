/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling

import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.api.scheduling.after
import com.cobblemon.mod.common.api.text.aqua
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.api.text.yellow
import com.cobblemon.mod.common.battles.BattleBuilder
import com.cobblemon.mod.common.battles.BattleRegistry
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.client.battle.ChallengeNotificationPacket
import com.cobblemon.mod.common.net.messages.server.BattleChallengePacket
import com.cobblemon.mod.common.util.lang
import com.cobblemon.mod.common.util.party
import net.minecraft.server.network.ServerPlayerEntity

object ChallengeHandler : ServerPacketHandler<BattleChallengePacket> {
    override fun invokeOnServer(packet: BattleChallengePacket, ctx: CobblemonNetwork.NetworkContext, player: ServerPlayerEntity) {
        val targetedEntity = player.world.getEntityById(packet.targetedEntityId)?.let {
            if (it is PokemonEntity) {
                val owner = it.owner
                if (owner != null) {
                    return@let owner
                }
            }
            return@let it
        } ?: return
        val leadingPokemon = player.party()[packet.selectedPokemonId]?.uuid ?: return

        when (targetedEntity) {
            is PokemonEntity -> {
                if (!targetedEntity.canBattle(player)) {
                    return
                }
                /*
                if (targetedEntity.isOwner(player))
                    return@runOnServer
                 */
                BattleBuilder.pve(player, targetedEntity, leadingPokemon).ifErrored { it.sendTo(player) { it.red() } }
            }
            is ServerPlayerEntity -> {
                // Bandaid for odd desync thing with data tracker
                if (player == targetedEntity) {
                    return
                }
                // Check in on battle requests, if the other player has challenged me, this starts the battle
                val existingChallenge = BattleRegistry.pvpChallenges[targetedEntity.uuid]
                if (existingChallenge != null && !existingChallenge.isExpired()) {
                    BattleBuilder.pvp1v1(player, targetedEntity)
                    BattleRegistry.pvpChallenges.remove(targetedEntity.uuid)
                } else {
                    val challenge = BattleRegistry.BattleChallenge(targetedEntity.uuid)
                    BattleRegistry.pvpChallenges[player.uuid] = challenge
                    after(seconds = challenge.expiryTimeSeconds.toFloat()) {
                        BattleRegistry.pvpChallenges.remove(player.uuid, challenge)
                    }
                    targetedEntity.sendPacket(ChallengeNotificationPacket(player.name.copy().aqua()))
                    player.sendMessage(lang("challenge.sender", targetedEntity.name).yellow())
                }
            }
            else -> {
                // Unrecognized challenge target. NPCs will probably go here.
            }
        }
    }
}