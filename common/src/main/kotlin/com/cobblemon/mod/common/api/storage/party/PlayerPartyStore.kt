/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage.party

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.pokemon.evolution.PassiveEvolution
import com.cobblemon.mod.common.api.storage.pc.PCStore
import com.cobblemon.mod.common.battles.BattleRegistry
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.activestate.ShoulderedState
import com.cobblemon.mod.common.util.DataKeys
import com.cobblemon.mod.common.util.getPlayer
import com.cobblemon.mod.common.util.isPokemonEntity
import com.cobblemon.mod.common.util.lang
import java.util.UUID
import kotlin.math.round
import kotlin.random.Random
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

/**
 * A [PartyStore] used for a single player. This uses the player's UUID as the store's UUID, and is declared as its own
 * class so that the purpose of this store is clear in practice. It also automatically adds the player's UUID as an
 * observer UUID as per [PartyStore.observerUUIDs]
 *
 * @author Hiroku
 * @since November 29th, 2021
 */
open class PlayerPartyStore(
    /** The UUID of the player this store is for. */
    val playerUUID: UUID
) : PartyStore(playerUUID) {

    private var secondsSinceFriendshipUpdate = 0

    override fun initialize() {
        super.initialize()
        observerUUIDs.add(playerUUID)
    }

    open fun getOverflowPC(): PCStore? {
        return Cobblemon.storage.getPC(playerUUID)
    }

    override fun add(pokemon: Pokemon): Boolean {
        return if (super.add(pokemon)) {
            true
        } else {
            val player = playerUUID.getPlayer()
            val pc = getOverflowPC()

            if (pc == null || !pc.add(pokemon)) {
                if (pc == null) {
                    player?.sendMessage(lang("overflow_no_pc"))
                } else {
                    player?.sendMessage(lang("overflow_no_space", pc.name))
                }
                false
            } else {
                player?.sendMessage(lang("overflow_to_pc", pokemon.species.translatedName, pc.name))
                true
            }
        }
    }

    /**
     * Called on the party every second for routine party updates
     * ex: Passive healing, statuses, etc
     */
    fun onSecondPassed(player: ServerPlayerEntity) {
        // Passive healing and passive statuses require the player be out of battle
        if (BattleRegistry.getBattleByParticipatingPlayer(player) == null) {
            val random = Random.Default
            for (pokemon in this) {
                // Awake from fainted
                if (pokemon.isFainted()) {
                    pokemon.faintedTimer -= 1
                    if (pokemon.faintedTimer <= -1) {
                        pokemon.currentHealth = (pokemon.hp * Cobblemon.config.faintAwakenHealthPercent).toInt()
                        player.sendMessage(Text.translatable("cobblemon.party.faintRecover", pokemon.species.translatedName))
                    }
                }
                // Passive healing while less than full health
                else if (pokemon.currentHealth < pokemon.hp) {
                    pokemon.healTimer--
                    if (pokemon.healTimer <= -1) {
                        pokemon.healTimer = Cobblemon.config.healTimer
                        val healAmount = 1.0.coerceAtLeast(pokemon.hp.toDouble() * Cobblemon.config.healPercent)
                        pokemon.currentHealth = pokemon.currentHealth + round(healAmount).toInt();
                    }
                }

                // Statuses
                val status = pokemon.status
                if (status != null && !player.isSleeping) {
                    if (status.isExpired()) {
                        status.status.onStatusExpire(player, pokemon, random)
                        pokemon.status = null
                    } else {
                        status.status.onSecondPassed(player, pokemon, random)
                        status.tickTimer()
                    }
                }

                // Passive evolutions
                pokemon.evolutions.filterIsInstance<PassiveEvolution>().forEach { it.attemptEvolution(pokemon) }
            }
            // Friendship
            // ToDo expand this down the line just a very basic implementation for the first releases
            if (++this.secondsSinceFriendshipUpdate == 120) {
                this.secondsSinceFriendshipUpdate = 0
                this.forEach { pokemon ->
                    if (pokemon.friendship < 160) {
                        if (pokemon.entity != null) {
                            pokemon.incrementFriendship(1)
                        }
                    }
                }
            }
        }

        // Shoulder validation code
        if (player.shoulderEntityLeft.isPokemonEntity() && !validateShoulder(player.shoulderEntityLeft, true)) {
            player.dropShoulderEntity(player.shoulderEntityLeft)
        }
        if (player.shoulderEntityRight.isPokemonEntity() && !validateShoulder(player.shoulderEntityRight, false)) {
            player.dropShoulderEntity(player.shoulderEntityRight)
        }

        forEach {
            val state = it.state
            if (state is ShoulderedState && !state.isStillShouldered(player)) {
                it.recall()
            }
        }
    }

    fun validateShoulder(shoulderEntity: NbtCompound, isLeft: Boolean): Boolean {
        val pokemon = find { it.uuid == shoulderEntity.getCompound("Pokemon").getUuid(DataKeys.POKEMON_UUID) }
        if (pokemon == null || (pokemon.state as? ShoulderedState)?.isLeftShoulder != isLeft) {
            return false
        }
        return true
    }
}