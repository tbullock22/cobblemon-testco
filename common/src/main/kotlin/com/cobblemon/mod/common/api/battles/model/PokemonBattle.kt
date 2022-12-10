/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.battles.model

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.Cobblemon.LOGGER
import com.cobblemon.mod.common.Cobblemon.showdown
import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.api.battles.model.actor.ActorType
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor
import com.cobblemon.mod.common.api.battles.model.actor.EntityBackedBattleActor
import com.cobblemon.mod.common.api.battles.model.actor.FleeableBattleActor
import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.text.yellow
import com.cobblemon.mod.common.battles.ActiveBattlePokemon
import com.cobblemon.mod.common.battles.BattleCaptureAction
import com.cobblemon.mod.common.battles.BattleFormat
import com.cobblemon.mod.common.battles.BattleRegistry
import com.cobblemon.mod.common.battles.BattleSide
import com.cobblemon.mod.common.battles.dispatch.BattleDispatch
import com.cobblemon.mod.common.battles.dispatch.DispatchResult
import com.cobblemon.mod.common.battles.dispatch.GO
import com.cobblemon.mod.common.net.messages.client.battle.BattleEndPacket
import com.cobblemon.mod.common.pokemon.feature.BattleCriticalHitsFeature
import com.cobblemon.mod.common.util.DataKeys
import com.cobblemon.mod.common.util.battleLang
import com.cobblemon.mod.common.util.getPlayer
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.util.UUID
import java.util.concurrent.ConcurrentLinkedQueue
import net.minecraft.text.Text

/**
 * Individual battle instance
 *
 * @since January 16th, 2022
 * @author Deltric, Hiroku
 */
open class PokemonBattle(
    val format: BattleFormat,
    val side1: BattleSide,
    val side2: BattleSide
) {
    /** Whether or not logging will be silenced for this battle. */
    var mute = true
    init {
        side1.battle = this
        side2.battle = this
        this.actors.forEach { actor ->
            actor.pokemonList.forEach { battlePokemon ->
                battlePokemon.effectedPokemon.getFeature<BattleCriticalHitsFeature>(BattleCriticalHitsFeature.ID)?.reset()
            }
        }
    }

    val sides: Iterable<BattleSide>
        get() = listOf(side1, side2)
    val actors: Iterable<BattleActor>
        get() = sides.flatMap { it.actors.toList() }
    val activePokemon: Iterable<ActiveBattlePokemon>
        get() = actors.flatMap { it.activePokemon }
    val playerUUIDs: Iterable<UUID>
        get() = actors.flatMap { it.getPlayerUUIDs() }
    val players = playerUUIDs.mapNotNull { it.getPlayer() }
    val spectators = mutableListOf<UUID>()

    val battleId = UUID.randomUUID()

    val showdownMessages = mutableListOf<String>()
    var started = false
    var ended = false
    // TEMP battle showcase stuff
    var announcingRules = false
    var turn: Int = 1
        private set


    var dispatchResult = GO
    val dispatches = ConcurrentLinkedQueue<BattleDispatch>()

    val captureActions = mutableListOf<BattleCaptureAction>()

    /** Whether or not there is one side with at least one player, and the other only has wild Pokémon. */
    val isPvW: Boolean
        get() {
            val playerSide = sides.find { it.actors.any { it.type == ActorType.PLAYER } } ?: return false
            if (playerSide.actors.any { it.type != ActorType.PLAYER }) {
                return false
            }
            val otherSide = sides.find { it != playerSide }!!
            return otherSide.actors.all { it.type == ActorType.WILD }
        }

    /** Whether or not there are player actors on both sides. */
    val isPvP: Boolean
        get() = sides.all { it.actors.any { it.type == ActorType.PLAYER } }

    /** Whether or not there is one player side and one NPC side. The opposite side to the player must all be NPCs. */
    val isPvN: Boolean
        get() {
            val playerSide = sides.find { it.actors.any { it.type == ActorType.PLAYER } } ?: return false
            if (playerSide.actors.any { it.type != ActorType.PLAYER }) {
                return false
            }
            val otherSide = sides.find { it != playerSide }!!
            return otherSide.actors.all { it.type == ActorType.NPC }
        }

    /**
     * Gets an actor by their showdown id
     * @return the actor if found otherwise null
     */
    fun getActor(showdownId: String) : BattleActor? {
        return actors.find { actor -> actor.showdownId == showdownId }
    }

    /**
     * Gets an actor by their game id
     * @return the actor if found otherwise null
     */
    fun getActor(actorId: UUID) : BattleActor? {
        return actors.find { actor -> actor.uuid == actorId }
    }

    /**
     * Gets a BattleActor and an [ActiveBattlePokemon] from a pnx key, e.g. p2a
     *
     * Returns null if either the pn or x is invalid.
     */
    fun getActorAndActiveSlotFromPNX(pnx: String): Pair<BattleActor, ActiveBattlePokemon> {
        val actor = actors.find { it.showdownId == pnx.substring(0, 2) }
            ?: throw IllegalStateException("Invalid pnx: $pnx - unknown actor")
        val letter = pnx[2]
        val pokemon = actor.getSide().activePokemon.find { it.getLetter() == letter }
            ?: throw IllegalStateException("Invalid pnx: $pnx - unknown pokemon")
        return actor to pokemon
    }

    fun broadcastChatMessage(component: Text) {
        return actors.forEach { it.sendMessage(component) }
    }

    fun writeShowdownAction(vararg messages: String) {
        val jsonArray = JsonArray()
        for (message in messages) {
            jsonArray.add(message)
        }
        val request = JsonObject()
        request.addProperty(DataKeys.REQUEST_TYPE, DataKeys.REQUEST_BATTLE_SEND_MESSAGE)
        request.addProperty(DataKeys.REQUEST_BATTLE_ID, battleId.toString())
        request.add(DataKeys.REQUEST_MESSAGES, jsonArray)
        val json = BattleRegistry.gson.toJson(request)
        log(json)
        showdown.write(json)
    }

    fun turn(newTurnNumber: Int) {
        actors.forEach { it.turn() }
        for (side in sides) {
            val opposite = side.getOppositeSide()
            side.activePokemon.forEach {
                val battlePokemon = it.battlePokemon ?: return@forEach
                battlePokemon.facedOpponents.addAll(opposite.activePokemon.mapNotNull { it.battlePokemon })
            }
        }
        this.turn = newTurnNumber
    }

    fun end() {
        ended = true
        for (actor in actors) {
            for (pokemon in actor.pokemonList.filter { it.health > 0 }) {
                if (pokemon.facedOpponents.isNotEmpty() /* TODO exp share held item check */) {
                    val experience = Cobblemon.experienceCalculator.calculate(pokemon)
                    if (experience > 0) {
                        actor.awardExperience(pokemon, (experience * Cobblemon.config.experienceMultiplier).toInt())
                    }
                    Cobblemon.evYieldCalculator.calculate(pokemon).forEach { (stat, amount) ->
                        pokemon.originalPokemon.evs.add(stat, amount)
                    }
                }
            }
        }
        sendUpdate(BattleEndPacket())
        BattleRegistry.closeBattle(this)
    }

    fun finishCaptureAction(captureAction: BattleCaptureAction) {
        captureActions.remove(captureAction)
        checkForInputDispatch()
    }

    fun log(message: String = "") {
        if (!mute) {
            LOGGER.info(message)
        }
    }

    fun sendUpdate(packet: NetworkPacket) {
        actors.forEach { it.sendUpdate(packet) }
        sendSpectatorUpdate(packet)
    }

    fun sendToActors(packet: NetworkPacket) {
        CobblemonNetwork.sendToPlayers(actors.flatMap { it.getPlayerUUIDs().mapNotNull { it.getPlayer() } }, packet)
    }

    fun sendSplitUpdate(privateActor: BattleActor, publicPacket: NetworkPacket, privatePacket: NetworkPacket) {
        actors.forEach {  it.sendUpdate(if (it == privateActor) privatePacket else publicPacket) }
        sendSpectatorUpdate(publicPacket)
    }

    fun sendSpectatorUpdate(packet: NetworkPacket) {
        CobblemonNetwork.sendToPlayers(spectators.mapNotNull { it.getPlayer() }, packet)
    }

    fun dispatch(dispatcher: () -> DispatchResult) {
        dispatches.add(BattleDispatch { dispatcher() })
    }

    fun dispatchGo(dispatcher: () -> Unit) {
        dispatch {
            dispatcher()
            GO
        }
    }

    fun dispatchInsert(dispatcher: () -> Iterable<BattleDispatch>) {
        dispatch {
            val newDispatches = dispatcher()
            val previousDispatches = dispatches.toList()
            dispatches.clear()
            dispatches.addAll(newDispatches)
            dispatches.addAll(previousDispatches)
            GO
        }
    }

    fun dispatch(dispatcher: BattleDispatch) {
        dispatches.add(dispatcher)
    }

    fun tick() {
        while (dispatchResult.canProceed()) {
            val dispatch = dispatches.poll() ?: break
            dispatchResult = dispatch(this)
        }

        if (started && isPvW && !ended) {
            checkFlee()
        }
    }

    open fun checkFlee() {
        // Do we check the player's pokemon being nearby or the player themselves? Player themselves because pokemon could be stuck together in a pit
        val wildPokemonOutOfRange = actors
            .filterIsInstance<FleeableBattleActor>()
            .filter { it.getWorldAndPosition() != null }
            .none { pokemonActor ->
                val (world, pos) = pokemonActor.getWorldAndPosition()!!
                val nearestPlayerActorDistance = actors
                    .filter { it.type == ActorType.PLAYER }
                    .filterIsInstance<EntityBackedBattleActor<*>>()
                    .mapNotNull { it.entity }
                    .filter { it.world == world }
                    .minOfOrNull { pos.distanceTo(it.pos) }

                nearestPlayerActorDistance != null && nearestPlayerActorDistance < pokemonActor.fleeDistance
            }

        if (wildPokemonOutOfRange) {
            actors.filterIsInstance<EntityBackedBattleActor<*>>().mapNotNull { it.entity }.forEach { it.sendMessage(battleLang("flee").yellow()) }
            stop()
        }
    }

    fun stop() {
        end()
        writeShowdownAction(">forcetie") // This will terminate the Showdown connection
    }

    fun checkForInputDispatch() {
        val readyToInput = actors.any { !it.mustChoose && it.responses.isNotEmpty() } && actors.none { it.mustChoose }
        if (readyToInput && captureActions.isEmpty()) {
            actors.filter { it.responses.isNotEmpty() }.forEach { it.writeShowdownResponse() }
            actors.forEach { it.responses.clear() ; it.request = null }
        }
    }
}