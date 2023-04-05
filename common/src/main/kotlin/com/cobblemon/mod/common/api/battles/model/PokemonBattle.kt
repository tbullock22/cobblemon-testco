/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.battles.model

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.Cobblemon.LOGGER
import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage
import com.cobblemon.mod.common.api.battles.model.actor.ActorType
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor
import com.cobblemon.mod.common.api.battles.model.actor.EntityBackedBattleActor
import com.cobblemon.mod.common.api.battles.model.actor.FleeableBattleActor
import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.tags.CobblemonItemTags
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.api.text.yellow
import com.cobblemon.mod.common.battles.ActiveBattlePokemon
import com.cobblemon.mod.common.battles.BattleCaptureAction
import com.cobblemon.mod.common.battles.BattleFormat
import com.cobblemon.mod.common.battles.BattleRegistry
import com.cobblemon.mod.common.battles.BattleSide
import com.cobblemon.mod.common.battles.dispatch.BattleDispatch
import com.cobblemon.mod.common.battles.dispatch.DispatchResult
import com.cobblemon.mod.common.battles.dispatch.GO
import com.cobblemon.mod.common.battles.dispatch.WaitDispatch
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.battles.runner.ShowdownService
import com.cobblemon.mod.common.net.messages.client.battle.BattleEndPacket
import com.cobblemon.mod.common.pokemon.evolution.progress.DefeatEvolutionProgress
import com.cobblemon.mod.common.util.battleLang
import com.cobblemon.mod.common.util.getPlayer
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
                battlePokemon.criticalHits = 0
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
    val afterDispatches = mutableListOf<() -> Unit>()

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

    fun getBattlePokemon(pnx: String, pokemonID: String): BattlePokemon {
        val actor = actors.find { it.showdownId == pnx.substring(0, 2) }
            ?: throw IllegalStateException("Invalid pnx: $pnx - unknown actor")
        return actor.pokemonList.find { it.uuid.toString() == pokemonID }
            ?: throw IllegalStateException("Invalid pnx: $pnx - unknown pokemon")
    }

    fun broadcastChatMessage(component: Text) {
        return actors.forEach { it.sendMessage(component) }
    }

    fun writeShowdownAction(vararg messages: String) {
        log(messages.joinToString("\n"))
        ShowdownService.get().send(battleId, messages.toList().toTypedArray())
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
        this.actors.forEach { actor ->
            val faintedPokemons = actor.pokemonList.filter { it.health <= 0 }
            actor.getSide().getOppositeSide().actors.forEach { opponent ->
                val opponentNonFaintedPokemons = opponent.pokemonList.filter { it.health > 0 }
                faintedPokemons.forEach { faintedPokemon ->
                    for (opponentPokemon in opponentNonFaintedPokemons) {
                        val facedFainted = opponentPokemon.facedOpponents.contains(faintedPokemon)
                        val defeatProgress = DefeatEvolutionProgress()
                        val pokemon = opponentPokemon.effectedPokemon
                        if (facedFainted && defeatProgress.shouldKeep(pokemon)) {
                            val progress = pokemon.evolutionProxy.current().progressFirstOrCreate({ it is DefeatEvolutionProgress && it.currentProgress().target.matches(faintedPokemon.effectedPokemon) }) { defeatProgress }
                            progress.updateProgress(DefeatEvolutionProgress.Progress(progress.currentProgress().target, progress.currentProgress().amount + 1))
                        }
                        val multiplier = when {
                            // ToDo when Exp. All is implement if enabled && !facedFainted return 2.0, probably should be a configurable value too, this will have priority over the Exp. Share
                            !facedFainted && pokemon.heldItemNoCopy().isIn(CobblemonItemTags.EXPERIENCE_SHARE) -> Cobblemon.config.experienceShareMultiplier
                            // ToDo when Exp. All is implemented the facedFainted and else can be collapsed into the 1.0 return value
                            facedFainted -> 1.0
                            else -> continue
                        }
                        val experience = Cobblemon.experienceCalculator.calculate(opponentPokemon, faintedPokemon, multiplier)
                        if (experience > 0) {
                            opponent.awardExperience(opponentPokemon, experience)
                        }
                        Cobblemon.evYieldCalculator.calculate(opponentPokemon).forEach { (stat, amount) ->
                            pokemon.evs.add(stat, amount)
                        }
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

    /**
     * Sends a packet depending on the side of an actor.
     *
     * @param source The actor that triggered the necessity for this update.
     * @param allyPacket The packet sent to the [source] and their allies.
     * @param opponentPacket The packet sent to the opposing actors.
     * @param spectatorsAsAlly If the spectators receive the [allyPacket] or the [opponentPacket], default is false.
     */
    fun sendSidedUpdate(source: BattleActor, allyPacket: NetworkPacket, opponentPacket: NetworkPacket, spectatorsAsAlly: Boolean = false) {
        source.getSide().actors.forEach { it.sendUpdate(allyPacket) }
        source.getSide().getOppositeSide().actors.forEach { it.sendUpdate(opponentPacket) }
        sendSpectatorUpdate(if (spectatorsAsAlly) allyPacket else opponentPacket)
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

    fun dispatchWaiting(delaySeconds: Float = 1F, dispatcher: () -> Unit) {
        dispatch {
            dispatcher()
            WaitDispatch(delaySeconds)
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

    fun doWhenClear(action: () -> Unit) {
        afterDispatches.add(action)
    }

    fun tick() {
        while (dispatchResult.canProceed()) {
            val dispatch = dispatches.poll() ?: break
            dispatchResult = dispatch(this)
        }

        if (dispatches.isEmpty()) {
            afterDispatches.toList().forEach { it() }
            afterDispatches.clear()
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
                if (pokemonActor.fleeDistance == -1F) true
                else {
                    val (world, pos) = pokemonActor.getWorldAndPosition()!!
                    val nearestPlayerActorDistance = actors.asSequence()
                        .filter { it.type == ActorType.PLAYER }
                        .filterIsInstance<EntityBackedBattleActor<*>>()
                        .mapNotNull { it.entity }
                        .filter { it.world == world }
                        .minOfOrNull { pos.distanceTo(it.pos) }

                    nearestPlayerActorDistance != null && nearestPlayerActorDistance < pokemonActor.fleeDistance
                }
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

    /**
     * Creates a [Text] representation of an error to interpret a battle message.
     * This also logs the error, the goal of this function is to make sure users see missing interpretations and report them to us.
     * Logging is independent of [mute].
     *
     * @param message The [BattleMessage] that wasn't able to find a lang interpretation.
     * @return The generated [Text] meant to notify the client.
     */
    internal fun createUnimplemented(message: BattleMessage): Text {
        LOGGER.error("Missing interpretation on '{}' action {}", message.id, message.rawMessage)
        return Text.literal("Missing interpretation on '${message.id}' action ${message.rawMessage}").red()
    }

    /**
     * A variant of [createUnimplemented].
     * This will log both the public and private message however the clients will no longer receive the full raw message in order to prevent extra information they shouldn't see.
     *
     * @param publicMessage The public variant of [BattleMessage] that wasn't able to find a lang interpretation.
     * @param privateMessage The private variant of [BattleMessage] that wasn't able to find a lang interpretation.
     * @return The generated [Text] meant to notify the client.
     *
     * @throws IllegalArgumentException if the [publicMessage] and [privateMessage] don't have a matching [BattleMessage.id].
     */
    internal fun createUnimplementedSplit(publicMessage: BattleMessage, privateMessage: BattleMessage): Text {
        if (publicMessage.id != privateMessage.id) {
            throw IllegalArgumentException("Messages do not match")
        }
        LOGGER.error("Missing interpretation on '{}' action: \nPublic » {}\nPrivate » {}", publicMessage.id, publicMessage.rawMessage, privateMessage.rawMessage)
        return Text.literal("Missing interpretation on '${publicMessage.id}' action please report to the developers").red()
    }

}