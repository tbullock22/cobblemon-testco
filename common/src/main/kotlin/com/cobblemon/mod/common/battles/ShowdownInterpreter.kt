/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.battles

import com.cobblemon.mod.common.Cobblemon.LOGGER
import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor
import com.cobblemon.mod.common.api.battles.model.actor.EntityBackedBattleActor
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.battles.BattleVictoryEvent
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.api.pokemon.status.Statuses
import com.cobblemon.mod.common.api.text.aqua
import com.cobblemon.mod.common.api.text.gold
import com.cobblemon.mod.common.api.text.plus
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.battles.dispatch.BattleDispatch
import com.cobblemon.mod.common.battles.dispatch.DispatchResult
import com.cobblemon.mod.common.battles.dispatch.GO
import com.cobblemon.mod.common.battles.dispatch.UntilDispatch
import com.cobblemon.mod.common.battles.dispatch.WaitDispatch
import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage
import com.cobblemon.mod.common.api.moves.Moves
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.net.messages.client.battle.BattleFaintPacket
import com.cobblemon.mod.common.net.messages.client.battle.BattleHealthChangePacket
import com.cobblemon.mod.common.net.messages.client.battle.BattleInitializePacket
import com.cobblemon.mod.common.net.messages.client.battle.BattleMakeChoicePacket
import com.cobblemon.mod.common.net.messages.client.battle.BattlePersistentStatusPacket
import com.cobblemon.mod.common.net.messages.client.battle.BattleQueueRequestPacket
import com.cobblemon.mod.common.net.messages.client.battle.BattleSetTeamPokemonPacket
import com.cobblemon.mod.common.net.messages.client.battle.BattleSwitchPokemonPacket
import com.cobblemon.mod.common.pokemon.evolution.progress.DamageTakenEvolutionProgress
import com.cobblemon.mod.common.pokemon.evolution.progress.RecoilEvolutionProgress
import com.cobblemon.mod.common.pokemon.evolution.progress.UseMoveEvolutionProgress
import com.cobblemon.mod.common.pokemon.status.PersistentStatus
import com.cobblemon.mod.common.util.asTranslated
import com.cobblemon.mod.common.util.battleLang
import com.cobblemon.mod.common.util.getPlayer
import com.cobblemon.mod.common.util.lang
import com.cobblemon.mod.common.util.runOnServer
import com.cobblemon.mod.common.util.swap
import java.util.UUID
import java.util.concurrent.CompletableFuture
import net.minecraft.entity.LivingEntity
import net.minecraft.server.world.ServerWorld
import kotlin.math.roundToInt

object ShowdownInterpreter {
    private val updateInstructions = mutableMapOf<String, (PokemonBattle, String, MutableList<String>) -> Unit>()
    private val sideUpdateInstructions = mutableMapOf<String, (PokemonBattle, BattleActor, String) -> Unit>()
    private val splitUpdateInstructions = mutableMapOf<String, (PokemonBattle, BattleActor, String, String) -> Unit>()
    // Stores a reference to the previous move message in a battle so a minor action can refer back to it (Battle UUID : Move message)
    private val lastMover = mutableMapOf<UUID, String>()

    init {
        updateInstructions["|player|"] = this::handlePlayerInstruction
        updateInstructions["|teamsize|"] = this::handleTeamSizeInstruction
        updateInstructions["|gametype|"] = this::handleGameTypeInstruction
        updateInstructions["|gen|"] = this::handleGenInstruction
        updateInstructions["|tier|"] = this::handleTierInstruction
        updateInstructions["|rated"] = this::handleRatedInstruction
        updateInstructions["|rule|"] = this::handleRuleInstruction
        updateInstructions["|clearpoke"] = this::handleClearPokeInstruction
        updateInstructions["|poke|"] = this::handlePokeInstruction
        updateInstructions["|teampreview"] = this::handleTeamPreviewInstruction
        updateInstructions["|start"] = this::handleStartInstruction
        updateInstructions["|turn|"] = this::handleTurnInstruction
        updateInstructions["|upkeep"] = this::handleUpkeepInstruction
        updateInstructions["|faint|"] = this::handleFaintInstruction
        updateInstructions["|win|"] = this::handleWinInstruction
        updateInstructions["|move|"] = this::handleMoveInstruction
        updateInstructions["|cant|"] = this::handleCantInstruction
        updateInstructions["|-supereffective|"] = this::handleSuperEffectiveInstruction
        updateInstructions["|-resisted|"] = this::handleResistInstruction
        updateInstructions["|-crit"] = this::handleCritInstruction
        updateInstructions["|-weather|"] = this::handleWeatherInstruction
        updateInstructions["|-mustrecharge|"] = this::handleRechargeInstructions
        updateInstructions["|-fail|"] = this::handleFailInstruction
        updateInstructions["|-start|"] = this::handleStartInstructions
        updateInstructions["|-activate|"] = this::handleActivateInstructions
        updateInstructions["|-curestatus|"] = this::handleCureStatusInstruction
        updateInstructions["|-nothing"] = { battle, _, _ ->
            battle.dispatchGo { battle.broadcastChatMessage(battleLang("nothing")) }
        }
        updateInstructions["|-unboost|"] = { battle, line, remainingLines -> boostInstruction(battle, line, remainingLines, false) }
        updateInstructions["|-boost|"] = { battle, line, remainingLines -> boostInstruction(battle, line, remainingLines, true) }
        updateInstructions["|t:|"] = {_, _, _ -> }
        updateInstructions["|pp_update|"] = this::handlePpUpdateInstruction
        updateInstructions["|-immune"] = this::handleImmuneInstruction
        updateInstructions["|-status|"] = this::handleStatusInstruction
        updateInstructions["|-end|"] = this::handleEndInstruction
        updateInstructions["|-miss|"] = this::handleMissInstruction
        updateInstructions["|-hitcount|"] = this::handleHitCountInstruction
        updateInstructions["|-item|"] = this::handleItemInstruction
        updateInstructions["|-enditem|"] = this::handleEndItemInstruction

        sideUpdateInstructions["|request|"] = this::handleRequestInstruction
        splitUpdateInstructions["|switch|"] = this::handleSwitchInstruction
        splitUpdateInstructions["|-damage|"] = this::handleDamageInstruction
        splitUpdateInstructions["|drag|"] = this::handleDragInstruction
    }

    private fun boostInstruction(battle: PokemonBattle, line: String, remainingLines: MutableList<String>, isBoost: Boolean) {
        val targetPNX = line.split("|")[2].split(":")[0]
        val targetPokemon = battle.getActorAndActiveSlotFromPNX(targetPNX)
        val statKey = line.split("|")[3]
        val stages = line.split("|")[4].toInt()
        val stat = getStat(statKey).displayName
        val severity = getSeverity(stages)
        val rootKey = if (isBoost) "boost" else "unboost"

        if (stages == 0) {
            val othersExist = remainingLines.removeIf {
                val isAlsoBoost = it.startsWith(if (isBoost) "|-boost" else "|-unboost")
                // Same type boost targeting the same person and both zero
                return@removeIf isAlsoBoost && it.split("|")[2] == line.split("|")[2] && it.split("|")[4] == "0"
            }
            if (othersExist) {
                battle.dispatchGo {
                    battle.broadcastChatMessage(battleLang("$rootKey.cap.multiple", targetPokemon.second.battlePokemon?.getName() ?: "ERROR".text()))
                }
                return
            }
        }

        battle.dispatchGo {
            battle.broadcastChatMessage(battleLang("$rootKey.$severity", targetPokemon.second.battlePokemon?.getName() ?: "ERROR".text(), stat))
        }
    }

    private fun getStat(statKey: String) = when(statKey) {
        "atk" -> Stats.ATTACK
        "def" -> Stats.DEFENCE
        "spa" -> Stats.SPECIAL_ATTACK
        "spd" -> Stats.SPECIAL_DEFENCE
        "spe" -> Stats.SPEED
        "evasion" -> Stats.EVASION
        else -> Stats.ACCURACY
    }

    private fun getSeverity(stages: Int) = when(stages) {
        0 -> "cap.single"
        1 -> "slight"
        2 -> "sharp"
        3 -> "severe"
        else -> "HUH!?"
    }

    fun interpretMessage(battleId: UUID, message: String) {
        // Check key map and use function if matching
        if (message.startsWith("{\"winner\":\"")) {
            // The post-win message is something we don't care about just yet. It's basically a summary of what happened in the battle.
            // Check /docs/example-post-win-message.json for its format.
            return
        }

        val battle = BattleRegistry.getBattle(battleId)

        if (battle == null) {
            LOGGER.info("No battle could be found with the id: $battleId")
            return
        }

        runOnServer {
            battle.showdownMessages.add(message)
            interpret(battle, message)
        }
    }

    fun interpret(battle: PokemonBattle, rawMessage: String) {
        battle.log()
        battle.log(rawMessage)
        battle.log()

        val lines = rawMessage.split("\n").toMutableList()
        if (lines[0] == "update") {
            lines.removeAt(0)
            while (lines.isNotEmpty()) {
                val line = lines.removeAt(0)

                // Split blocks have a public and private message below
                if (line.startsWith("|split|")) {
                    val showdownId = line.split("|split|")[1]
                    val targetActor = battle.getActor(showdownId)

                    if (targetActor == null) {
                        battle.log("No actor could be found with the showdown id: $showdownId")
                        return
                    }

                    val privateMessage = lines[0]
                    val publicMessage = lines[1]

                    for (instruction in splitUpdateInstructions.entries) {
                        if (lines[0].startsWith(instruction.key)) {
                            instruction.value(battle, targetActor, publicMessage, privateMessage)
                            break
                        }
                    }

                    lines.removeFirst()
                    lines.removeFirst()
                } else {
                    if (line != "|") {
                        val instruction = updateInstructions.entries.find { line.startsWith(it.key) }?.value
                        if (instruction != null) {
                            instruction(battle, line, lines)
                        } else {
                            battle.dispatch {
                                battle.broadcastChatMessage(line.text())
                                GO
                            }
                        }
                    }
                }
            }
        } else if (lines[0] == "sideupdate") {
            val showdownId = lines[1]
            val targetActor = battle.getActor(showdownId)
            val line = lines[2]

            if (targetActor == null) {
                battle.log("No actor could be found with the showdown id: $showdownId")
                return
            }

            for (instruction in sideUpdateInstructions.entries) {
                if (line.startsWith(instruction.key)) {
                    instruction.value(battle, targetActor, line)
                }
            }
        }
    }

    /**
     * Format:
     * |player|PLAYER|USERNAME|AVATAR|RATING
     *
     * Definitions:
     * PLAYER is p1 or p2 unless 4 player battle which adds p3 and p4
     * USERNAME is the Cobblemon battle actors uuid
     * AVATAR is unused currently
     * RATING is unused currently
     */
    private fun handlePlayerInstruction(battle: PokemonBattle, message: String, remainingLines: MutableList<String>) {
//        battle.log("Player Instruction")
    }

    /**
     * Format:
     * |teamsize|PLAYER|NUMBER
     *
     * Definitions:
     * PLAYER is p1 or p2 unless 4 player battle which adds p3 and p4
     * NUMBER is number of Pokémon your opponent starts with for team preview.
     */
    private fun handleTeamSizeInstruction(battle: PokemonBattle, message: String, remainingLines: MutableList<String>) {
//        battle.log("Team Size Instruction")
    }

    /**
     * Format:
     * |gametype|GAMETYPE
     *
     * Definitions:
     * GAMETYPE is singles, doubles, triples, multi, and or freeforall
     */
    private fun handleGameTypeInstruction(battle: PokemonBattle, message: String, remainingLines: MutableList<String>) {
        battle.log("Game Type Instruction: $message")
//
//        battle.broadcastChatMessage(LiteralText("${Formatting.GOLD}${Formatting.BOLD}Battle Type:"))
//
//        val tierName = message.split("|gametype|")[1]
//        val textComponent = LiteralText(" ${Formatting.GRAY}$tierName")
//        battle.broadcastChatMessage(textComponent)
//        battle.broadcastChatMessage("".text())
    }

    /**
     * Format:
     * |gen|GENNUM
     *
     * Definitions:
     * GENNUM is Generation number, from 1 to 7. Stadium counts as its respective gens;
     * Let's Go counts as 7, and modded formats count as whatever gen they were based on.
     */
    private fun handleGenInstruction(battle: PokemonBattle, message: String, remainingLines: MutableList<String>) {
        battle.log("Gen Instruction: $message")
    }

    /**
     * Format:
     * |tier|FORMATNAME
     *
     * Definitions:
     * FORMATNAME is the name of the format being played.
     */
    private fun handleTierInstruction(battle: PokemonBattle, message: String, remainingLines: MutableList<String>) {
        battle.log("Tier Instruction: $message")
//
//        battle.broadcastChatMessage(LiteralText("${Formatting.GOLD}${Formatting.BOLD}Battle Tier:"))
//
//        val tierName = message.split("|tier|")[1]
//        val textComponent = LiteralText(" ${Formatting.GRAY}$tierName")
//        battle.broadcastChatMessage(textComponent)
//        battle.broadcastChatMessage("".text())
    }

    /**
     * Format:
     * |rated or |rated|MESSAGE
     *
     * Definitions:
     * No message: Will be sent if the game will affect the player's ladder rating (Elo score).
     * Message: Will be sent if the game is official in some other way, such as being a tournament game.
     * Does not actually mean the game is rated.
     */
    private fun handleRatedInstruction(battle: PokemonBattle, message: String, remainingLines: MutableList<String>) {
//        battle.log("Rated Instruction")
    }

    /**
     * Format:
     * |rule|RULE: DESCRIPTION
     *
     * Definitions:
     * RULE is a rule and its description
     */
    private fun handleRuleInstruction(battle: PokemonBattle, message: String, remainingLines: MutableList<String>) {
        battle.log("Rule Instruction: $message")
        if (!battle.announcingRules) {
            battle.announcingRules = true
//            val textComponent = LiteralText("${Formatting.GOLD}${Formatting.BOLD}Battle Rules:")
//            battle.broadcastChatMessage(textComponent)
        }
//        val rule = message.substringAfter("|rule|")
//        val textComponent = LiteralText("${Formatting.GRAY} - $rule")
//        battle.broadcastChatMessage(textComponent)
    }

    /**
     * Format:
     * |clearpoke
     *
     * Marks the start of Team Preview
     */
    private fun handleClearPokeInstruction(battle: PokemonBattle, message: String, remainingLines: MutableList<String>) {
        battle.log("Clear Poke Instruction")
    }

    /**
     * Format:
     * |poke|PLAYER|DETAILS|ITEM
     *
     * Declares a Pokémon for Team Preview.
     *
     * PLAYER is the player ID
     * DETAILS describes the pokemon
     * ITEM will be an item if the pokemon is holding an item or blank if it isn't
     */
    private fun handlePokeInstruction(battle: PokemonBattle, message: String, remainingLines: MutableList<String>) {
        battle.log("Poke Instruction: $message")
//
//        val args = message.split("|")
//        val showdownId = args[2]
//        val pokemon = args[3]
//
//        val targetActor = battle.getActor(showdownId)
//
//        if (targetActor == null) {
//            battle.log("No actor could be found with the showdown id: $showdownId")
//            return
//        }
//
//        if (targetActor is PlayerBattleActor) {
//            if (!targetActor.announcedPokemon) {
//                targetActor.announcedPokemon = true
//                val textComponent = battleLang("your_team").gold().bold()
//                targetActor.sendMessage(textComponent)
//            }
//
//            battle.broadcastChatMessage("- $pokemon".aqua()) // change to our own description using UUID to get the Pokemon
//        }
    }

    /**
     * Format:
     * |teampreview indicates team preview is over
     */
    private fun handleTeamPreviewInstruction(battle: PokemonBattle, message: String, remainingLines: MutableList<String>) {
        battle.log("Start Team Preview Instruction: $message")
    }

    /**
     * Format:
     * |start
     *
     * Indicates that the game has started.
     */
    private fun handleStartInstruction(battle: PokemonBattle, message: String, remainingLines: MutableList<String>) {
        battle.log("Start Instruction: $message")
    }

    /**
     * Format:
     * |turn|NUMBER
     *
     * It is now turn NUMBER.
     */
    private fun handleTurnInstruction(battle: PokemonBattle, message: String, remainingLines: MutableList<String>) {
        if (!battle.started) {
            battle.started = true
            val players = battle.actors.mapNotNull { it.uuid.getPlayer() }
            if (players.isNotEmpty()) {
                val initializePacket = BattleInitializePacket(battle)
                players.forEach { CobblemonNetwork.sendToPlayer(it, initializePacket) }
            }
            battle.actors.forEach {
                it.sendUpdate(BattleSetTeamPokemonPacket(it.pokemonList.map { it.effectedPokemon }))
                val req = it.request ?: return@forEach
                it.sendUpdate(BattleQueueRequestPacket(req))
            }
        }

        // TODO maybe tell the client that the turn number has changed
        val turnNumber = message.split("|turn|")[1].toInt()

        battle.dispatch {
            battle.sendToActors(BattleMakeChoicePacket())
            battle.broadcastChatMessage("It is now turn $turnNumber".aqua())
            battle.turn(turnNumber)
            GO
        }
    }

    private fun handleUpkeepInstruction(battle: PokemonBattle, message: String, remainingLines: MutableList<String>) {
        battle.dispatch {
            battle.actors.forEach { it.upkeep() }
            GO
        }
    }

    /**
     * Format:
     * |faint|POKEMON
     *
     * The Pokémon POKEMON has fainted.
     */
    private fun handleFaintInstruction(battle: PokemonBattle, message: String, remainingLines: MutableList<String>) {
        battle.dispatch {
            val pnx = message.split("|faint|")[1].substring(0, 3)
            val (actor, pokemon) = battle.getActorAndActiveSlotFromPNX(pnx)
            battle.sendUpdate(BattleFaintPacket(pnx, battleLang("fainted", pokemon.battlePokemon?.getName() ?: "ALREADY DEAD")))
            pokemon.battlePokemon?.effectedPokemon?.currentHealth = 0
            pokemon.battlePokemon?.sendUpdate()
            battle.broadcastChatMessage(battleLang("fainted", pokemon.battlePokemon?.getName() ?: "ALREADY DEAD".red()).red())
            pokemon.battlePokemon = null
            WaitDispatch(2.5F)
        }
    }

    private fun handleWinInstruction(battle: PokemonBattle, message: String, remainingLines: MutableList<String>) {
        battle.dispatch {
            val ids = message.split("|win|")[1].split("&").map { it.trim() }
            val winners = ids.map { battle.getActor(UUID.fromString(it))!! }
            val winnersText = winners.map { it.getName() }.reduce { acc, next -> acc + " & " + next }

            battle.broadcastChatMessage(battleLang("win", winnersText).gold())

            battle.end()
            CobblemonEvents.BATTLE_VICTORY.post(BattleVictoryEvent(battle, winners))

            this.lastMover.remove(battle.battleId)
            GO
        }
    }

    fun handleStatusInstruction(battle: PokemonBattle, message: String, remainingLines: MutableList<String>) {
        battle.dispatchGo {
            val pnx = message.split("|-status|")[1].substring(0, 3)
            val (_, pokemon) = battle.getActorAndActiveSlotFromPNX(pnx)
            val editedMessage = message.replace("|-status|", "")
            val statusLabel = editedMessage.split("|")[1]
            val status = Statuses.getStatus(statusLabel)
                ?: return@dispatchGo LOGGER.error("Unrecognized status: $statusLabel")

            if (status is PersistentStatus) {
                pokemon.battlePokemon?.effectedPokemon?.let{
                    it.applyStatus(status)
                    battle.sendUpdate(BattlePersistentStatusPacket(pnx, status))
                }

            }

            battle.broadcastChatMessage(status.applyMessage.asTranslated(pokemon.battlePokemon?.getName() ?: "DEAD".text()))
        }
    }

    private fun handleMissInstruction(battle: PokemonBattle, message: String, remainingLines: MutableList<String>) {
        battle.dispatch {
            battle.broadcastChatMessage(battleLang("missed"))
            WaitDispatch(1.5F)
        }
    }

    private fun handleImmuneInstruction(battle: PokemonBattle, message: String, remainingLines: MutableList<String>) {
        val pnx = message.split("|-immune|")[1].substring(0, 3)
        val from = if ("[from]" in message) message.substringAfter("[from]").trim() else null

        battle.dispatchGo {
            val (_, pokemon) = battle.getActorAndActiveSlotFromPNX(pnx)
            val name = pokemon.battlePokemon?.getName() ?: "DEAD".text()
            battle.broadcastChatMessage(battleLang("immune", name))
        }
    }

    // |move|p1a: Charizard|Tackle|p2a: Magikarp
    private fun handleMoveInstruction(battle: PokemonBattle, message: String, remainingLines: MutableList<String>) {
        battle.dispatch {
            val editMessaged = message.replace("|move|", "")
            this.lastMover[battle.battleId] = message
            val userPNX = editMessaged.split("|")[0].split(":")[0].trim()
            val (_, userPokemon) = battle.getActorAndActiveSlotFromPNX(userPNX)
            val move = editMessaged.split("|")[1].split("|")[0]
            val hasTarget = editMessaged.split("|").size == 3 && editMessaged.split("|")[2].isNotEmpty()
            val targetPokemon = if (hasTarget) {
                val targetPNX = editMessaged.split("|")[2].split(":")[0]
                battle.getActorAndActiveSlotFromPNX(targetPNX)
            } else {
                null
            }
            userPokemon.battlePokemon?.effectedPokemon?.let { pokemon ->
                Moves.getByName(move)?.let { moveTemplate ->
                    val progress = UseMoveEvolutionProgress()
                    if (progress.shouldKeep(pokemon)) {
                        val created = pokemon.evolutionProxy.current().progressFirstOrCreate({ it is UseMoveEvolutionProgress && it.currentProgress().move == moveTemplate }) { progress }
                        created.updateProgress(UseMoveEvolutionProgress.Progress(created.currentProgress().move, created.currentProgress().amount + 1))
                    }
                }
            }
            if (targetPokemon != null && targetPokemon.second != userPokemon) {
                val targetPNX = editMessaged.split("|")[2].split(":")[0]
                val (_, targetPokemon) = battle.getActorAndActiveSlotFromPNX(targetPNX)
                battle.broadcastChatMessage(battleLang(
                    key = "used_move_on",
                    userPokemon.battlePokemon?.getName() ?: "ERROR".red(),
                    move,
                    targetPokemon.battlePokemon?.getName() ?: "ERROR".red()
                ))
            } else {
                battle.broadcastChatMessage(battleLang(
                    key = "used_move",
                    userPokemon.battlePokemon?.getName() ?: "ERROR".red(),
                    move
                ))
            }

            GO
        }
    }

    private fun handleCantInstruction(battle: PokemonBattle, message: String, remainingLines: MutableList<String>) {
        battle.dispatch {
            val editMessaged = message.replace("|cant|", "")

            val pnx = editMessaged.split("|")[0].split(":")[0]
            val (actor, pokemon) = battle.getActorAndActiveSlotFromPNX(pnx)
            val action = editMessaged.split("|")[1]
            val name = pokemon.battlePokemon?.getName() ?: "DEAD".text()
            val actionText = when (action) {
                Statuses.SLEEP.showdownName -> lang("status.sleep.is", name)
                Statuses.PARALYSIS.showdownName -> lang("status.paralysis.is", name)
                Statuses.FROZEN.showdownName -> lang("status.frozen.is", name)
                "flinch" -> battleLang("flinched", name)
                else -> action.text() // Way for us to find those we have not implemented
            }

            battle.broadcastChatMessage(actionText.red())
            GO
        }
    }

    /**
     * Format:
     * |-resisted|p%a
     *
     * player % resisted the attack.
     */
    private fun handleResistInstruction(battle: PokemonBattle, message: String, remainingLines: MutableList<String>) {
        battle.dispatch {
            battle.broadcastChatMessage(battleLang("resisted"))
            GO
        }
    }

    /**
     * Format:
     * |pp_update|<side_id>: <pokemon_uuid>|...<move_id>: <move_pp>
     */
    private fun handlePpUpdateInstruction(battle: PokemonBattle, message: String, remainingLines: MutableList<String>) {
        battle.dispatch {
            val editMessaged = message.replace("|pp_update|", "")
            val data = editMessaged.split("|")
            val actorAndPokemonData = data[0].split(": ")
            val actorID = actorAndPokemonData[0]
            val pokemonID = UUID.fromString(actorAndPokemonData[1])
            val actor = battle.getActor(actorID) ?: return@dispatch GO
            val pokemon = actor.pokemonList.firstOrNull { battlePokemon -> battlePokemon.effectedPokemon.uuid == pokemonID } ?: return@dispatch GO
            val moveDatum = data[1].split(", ")
            moveDatum.forEach { moveData ->
                val moveIdAndPp = moveData.split(": ")
                val moveId = moveIdAndPp[0]
                val movePp = moveIdAndPp[1]
                val move = pokemon.effectedPokemon.moveSet.firstOrNull { move -> move.name.equals(moveId, true) } ?: return@dispatch GO
                move.currentPp = movePp.toInt()
            }
            GO
        }
    }

    /**
     * Format:
     * |-supereffective|p%a
     *
     * player % was weak against the attack.
     */
    private fun handleSuperEffectiveInstruction(battle: PokemonBattle, message: String, remainingLines: MutableList<String>) {
        battle.dispatch {
            battle.broadcastChatMessage(battleLang("superEffective"))
            GO
        }
    }

    private fun handleCritInstruction(battle: PokemonBattle, message: String, remainingLines: MutableList<String>) {
        battle.dispatch {
            battle.broadcastChatMessage(battleLang("crit"))
            this.lastMover[battle.battleId]?.let { message ->
                val editMessaged = message.replace("|move|", "")
                val userPNX = editMessaged.split("|")[0].split(":")[0].trim()
                val battlePokemon = battle.getActorAndActiveSlotFromPNX(userPNX).second.battlePokemon ?: return@let
                battlePokemon.criticalHits++
            }
            GO
        }
    }

    private fun handleWeatherInstruction(battle: PokemonBattle, message: String, remainingLines: MutableList<String>){
        battle.dispatch{
            when (message){
                "|-weather|RainDance" -> battle.broadcastChatMessage(battleLang("rain_dance"))
                "|-weather|RainDance|[upkeep]" -> battle.broadcastChatMessage(battleLang("rain_dance_upkeep"))
                "|-weather|Sandstorm" -> battle.broadcastChatMessage(battleLang("sandstorm"))
                "|-weather|Sandstorm|[upkeep]" -> battle.broadcastChatMessage(battleLang("sandstorm_upkeep"))
                "|-weather|SunnyDay" -> battle.broadcastChatMessage(battleLang("sunny_day_upkeep"))
                "|-weather|SunnyDay|[upkeep]" -> battle.broadcastChatMessage(battleLang("sunny_day_upkeep"))
                "|-weather|Hail" -> battle.broadcastChatMessage(battleLang("hail"))
                "|-weather|Hail|[upkeep]" -> battle.broadcastChatMessage(battleLang("hail_upkeep"))
                "|-weather|NoWeather" -> battle.broadcastChatMessage(battleLang("rain_dance_upkeep"))
            }
            GO
        }
    }

    private fun handleFailInstruction(battle: PokemonBattle, message: String, remainingLines: MutableList<String>){
        battle.dispatch{
            battle.broadcastChatMessage(battleLang("fail"))
            WaitDispatch(1.5F)
        }
    }

    private fun handleRechargeInstructions(battle: PokemonBattle, message: String, remainingLines: MutableList<String>){
        battle.dispatch{
            val pnx = message.split("|-mustrecharge|")[1].substring(0, 3)
            val (_, pokemon) = battle.getActorAndActiveSlotFromPNX(pnx)
            battle.broadcastChatMessage(battleLang("recharge", pokemon.battlePokemon?.getName() ?: ""))
            WaitDispatch(2F)
        }
    }

    private fun handleCureStatusInstruction(battle: PokemonBattle, message: String, remainingLines: MutableList<String>) {
        battle.dispatch {
            val pnx = message.split("|-curestatus|")[1].substring(0, 3)
            val remaining = message.substringAfter("|-curestatus|")
            val (_, pokemon) = battle.getActorAndActiveSlotFromPNX(pnx)
            val status = Statuses.getStatus(remaining.split("|")[1])
            val battlePokemon = pokemon.battlePokemon ?: return@dispatch GO
            if (status is PersistentStatus) {
                battlePokemon.effectedPokemon.status = null
                battle.sendUpdate(BattlePersistentStatusPacket(pnx, null))
            }
            val showMessage = "[msg]" in remaining
            if (status != null && showMessage) {
                status.removeMessage?.let { battle.broadcastChatMessage(it.asTranslated(battlePokemon.getName())) }
            }
            WaitDispatch(1F)
        }
    }

    private fun handleStartInstructions(battle: PokemonBattle, message: String, remainingLines: MutableList<String>){
        battle.dispatch {
            val pnx = message.split("|-start|")[1].substring(0, 3)
            val (_, pokemon) = battle.getActorAndActiveSlotFromPNX(pnx)
            if (message.contains("|confusion")){
                battle.broadcastChatMessage(battleLang("confusion_start",pokemon.battlePokemon?.getName() ?: ""))
            }
            if (message.contains("|protect")){
                battle.broadcastChatMessage(battleLang("protect_start",pokemon.battlePokemon?.getName() ?: ""))
            }
            if (message.contains("move: Bide")){
                battle.broadcastChatMessage(battleLang("bide_start",pokemon.battlePokemon?.getName() ?: ""))
            }
            WaitDispatch(2F)
        }
    }

    private fun handleActivateInstructions(battle: PokemonBattle, message: String, remainingLines: MutableList<String>){
        battle.dispatch{
            val pnx = message.split("|-activate|")[1].substring(0, 3)
            val (_, pokemon) = battle.getActorAndActiveSlotFromPNX(pnx)
//            if ("|confusion" in message) { // Don't even say anything about it, it's too spammy
//                battle.broadcastChatMessage(battleLang("confusion_continues_idk", pokemon.battlePokemon!!.getName()))
//            }
            when {
                "|protect" in message -> battle.broadcastChatMessage(battleLang("protect_activate",pokemon.battlePokemon!!.getName()))
                "move: Magnitude" in message -> battle.broadcastChatMessage(battleLang("magnitude_level", message.substringAfterLast("|").toIntOrNull() ?: 1))
                "move: Bide" in message -> battle.broadcastChatMessage(battleLang("bide_activate",pokemon.battlePokemon!!.getName()))
                // ToDo Focus Band, use battleLang("item.hung_on.end", battlerName, itemName)
            }
            GO
        }
    }

    private fun handleEndInstruction(battle: PokemonBattle, message: String, remainingLines: MutableList<String>) {
        battle.dispatch {
            val editedMessage = message.split("|-end|")[1]
            val pnx = editedMessage.substring(0, 3)
            val (_, pokemon) = battle.getActorAndActiveSlotFromPNX(pnx)
            val fromWhat = editedMessage.split("|")[1]

            when (fromWhat) {
                "confusion" -> battle.broadcastChatMessage(battleLang("confusion_snapped", pokemon.battlePokemon!!.getName()))
                "move: Bide" -> battle.broadcastChatMessage(battleLang("bide_end", pokemon.battlePokemon!!.getName()))
                else -> battle.broadcastChatMessage(editedMessage.text())
            }

            WaitDispatch(1F)
        }
    }

    /**
     * Format:
     * |request|REQUEST
     *
     * The protocol message to tell you that it's time for you to make a decision is:
     */
    private fun handleRequestInstruction(battle: PokemonBattle, battleActor: BattleActor, message: String) {
        battle.log("Request Instruction")

        if (message.contains("teamPreview")) // TODO probably change when we're allowing team preview
            return

        // Parse Json message and update state info for actor
        val request = BattleRegistry.gson.fromJson(message.split("|request|")[1], ShowdownActionRequest::class.java)
        if (battle.started) {
            battle.dispatch {
                battleActor.sendUpdate(BattleQueueRequestPacket(request))
                battleActor.request = request
                battleActor.responses.clear()
                val goneIndices = battleActor.activePokemon.filter { it.isGone() }.map { battleActor.activePokemon.indexOf(it) }
                val forceSwitchIndices = request.forceSwitch.mapIndexedNotNull { index, b -> if (b) index else null }
                if (forceSwitchIndices.any { it in goneIndices }) {
                    battleActor.mustChoose = true
                    battleActor.sendUpdate(BattleMakeChoicePacket())
                }
                GO
            }
        } else {
            battleActor.request = request
            battleActor.responses.clear()
        }
    }

    private fun handleSwitchInstruction(battle: PokemonBattle, battleActor: BattleActor, publicMessage: String, privateMessage: String) {
        val pnx = publicMessage.split("|")[2].split(":")[0]
        if (!battle.started) {
            val (actor, activePokemon) = battle.getActorAndActiveSlotFromPNX(pnx)
            val uuid = UUID.fromString(publicMessage.split("|")[2].split(":")[1].trim())
            val pokemon = actor.pokemonList.find { it.uuid == uuid } ?: throw IllegalStateException("Unable to find ${actor.showdownId}'s Pokemon with UUID: $uuid")
            val entity = if (actor is EntityBackedBattleActor<*>) actor.entity else null
            activePokemon.battlePokemon = pokemon
            val pokemonEntity = pokemon.entity
            if (pokemonEntity == null && entity != null) {
                pokemon.effectedPokemon.sendOutWithAnimation(
                    source = entity,
                    battleId = battle.battleId,
                    level = entity.world as ServerWorld,
                    position = entity.pos
                )
            }
        } else {
            battle.dispatchInsert {
                val (actor, activePokemon) = battle.getActorAndActiveSlotFromPNX(pnx)
                val uuid = UUID.fromString(publicMessage.split("|")[2].split(":")[1].trim())
                val pokemon = actor.pokemonList.find { it.uuid == uuid } ?: throw IllegalStateException("Unable to find ${actor.showdownId}'s Pokemon with UUID: $uuid")
                val entity = if (actor is EntityBackedBattleActor<*>) actor.entity else null

                if (activePokemon.battlePokemon == pokemon) {
                    return@dispatchInsert emptySet() // Already switched in, Showdown does this if the pokemon is going to die before it can switch
                }

                setOf(
                    BattleDispatch {
                        if (entity != null) {
                            this.createEntitySwitch(battle, actor, entity, pnx, activePokemon, pokemon)
                        } else {
                            this.createNonEntitySwitch(battle, actor, pnx, activePokemon, pokemon)
                        }
                    }
                )
            }
        }
    }

    private fun createEntitySwitch(battle: PokemonBattle, actor: BattleActor, entity: LivingEntity, pnx: String, activePokemon: ActiveBattlePokemon, newPokemon: BattlePokemon): DispatchResult {
        val pokemonEntity = activePokemon.battlePokemon?.entity
        // If we can't find the entity for some reason then we're going to skip the recall animation
        val sendOutFuture = CompletableFuture<Unit>()
        (pokemonEntity?.recallWithAnimation() ?: CompletableFuture.completedFuture(Unit)).thenApply {
            // Queue actual swap and send-in after the animation has ended
            actor.pokemonList.swap(actor.activePokemon.indexOf(activePokemon), actor.pokemonList.indexOf(newPokemon))
            activePokemon.battlePokemon = newPokemon
            battle.sendUpdate(BattleSwitchPokemonPacket(pnx, newPokemon))
            if (newPokemon.entity != null) {
                sendOutFuture.complete(Unit)
            } else {
                val lastPosition = activePokemon.position
                // Send out at previous Pokémon's location if it is known, otherwise actor location
                val world = lastPosition?.first ?: entity.world as ServerWorld
                val pos = lastPosition?.second ?: entity.pos
                newPokemon.effectedPokemon.sendOutWithAnimation(
                    source = entity,
                    battleId = battle.battleId,
                    level = world,
                    position = pos
                ).thenAccept { sendOutFuture.complete(Unit) }
            }
        }

        return UntilDispatch { sendOutFuture.isDone }
    }

    private fun createNonEntitySwitch(battle: PokemonBattle, actor: BattleActor, pnx: String, activePokemon: ActiveBattlePokemon, newPokemon: BattlePokemon): DispatchResult {
        actor.pokemonList.swap(actor.activePokemon.indexOf(activePokemon), actor.pokemonList.indexOf(newPokemon))
        activePokemon.battlePokemon = newPokemon
        battle.sendUpdate(BattleSwitchPokemonPacket(pnx, newPokemon))
        return WaitDispatch(1.5F)
    }

    fun handleDamageInstruction(battle: PokemonBattle, actor: BattleActor, publicMessage: String, privateMessage: String) {
        val pnx = publicMessage.split("|")[2].split(":")[0]
        val battleMessage = BattleMessage(publicMessage)
        val (_, activePokemon) = battleMessage.actorAndActivePokemon(0, battle)!!
        if (battleMessage.optionalArgument("from")?.equals("recoil", true) == true) {
            activePokemon.battlePokemon?.effectedPokemon?.let { pokemon ->
                val recoilProgress = RecoilEvolutionProgress()
                // Lazy cheat to see if it's necessary to use this
                if (recoilProgress.shouldKeep(pokemon)) {
                    val progress = pokemon.evolutionProxy.current().progressFirstOrCreate({ it is RecoilEvolutionProgress }) { recoilProgress }
                    val newPercentage = battleMessage.argumentAt(1)?.split("/")?.getOrNull(0)?.toIntOrNull() ?: 0
                    val newHealth = (pokemon.hp * (newPercentage / 100.0)).roundToInt()
                    val difference = pokemon.currentHealth - newHealth
                    if (difference > 0) {
                        progress.updateProgress(RecoilEvolutionProgress.Progress(progress.currentProgress().recoil + difference))
                    }
                }
            }
        }
        val newHealth = privateMessage.split("|")[3].split(" ")[0]
        val cause = if ("[from]" in publicMessage) publicMessage.substringAfter("[from]").trim() else null

        battle.dispatch {
            val newHealthRatio: Float
            val remainingHealth = newHealth.split("/")[0].toInt()
            if (newHealth == "0") {
                newHealthRatio = 0F
                battle.dispatch {
                    activePokemon.battlePokemon?.effectedPokemon?.currentHealth = 0
                    activePokemon.battlePokemon?.sendUpdate()
                    GO
                }
            } else {
                val maxHealth = newHealth.split("/")[1].toInt()
                val difference = maxHealth - remainingHealth
                newHealthRatio = remainingHealth.toFloat() / maxHealth
                battle.dispatch {
                    activePokemon.battlePokemon?.effectedPokemon?.currentHealth = remainingHealth
                    if (difference > 0) {
                        activePokemon.battlePokemon?.effectedPokemon?.let { pokemon ->
                            val damageProgress = DamageTakenEvolutionProgress()
                            // Lazy cheat to see if it's necessary to use this
                            if (damageProgress.shouldKeep(pokemon)) {
                                val progress = pokemon.evolutionProxy.current().progressFirstOrCreate({ it is DamageTakenEvolutionProgress }) { damageProgress }
                                progress.updateProgress(DamageTakenEvolutionProgress.Progress(progress.currentProgress().amount + difference))
                            }
                        }
                    }
                    activePokemon.battlePokemon?.sendUpdate()
                    GO
                }
            }
            battle.sendUpdate(BattleHealthChangePacket(pnx, newHealthRatio, remainingHealth))
            if (cause != null) {
                when (cause) {
                    "confusion" -> battle.broadcastChatMessage(battleLang("confusion_activate", activePokemon.battlePokemon?.getName()!!))
                }
            }
            WaitDispatch(1F)
        }
    }

    fun handleDragInstruction(battle: PokemonBattle, actor: BattleActor, publicMessage: String, privateMessage: String) {
        battle.dispatchGo {
            val pnx = publicMessage.split("|")[2].split(":")[0]
            val (_, activePokemon) = battle.getActorAndActiveSlotFromPNX(pnx)
            val uuid = UUID.fromString(publicMessage.split("|")[3].split(",")[1].trim())
            val pokemon = actor.pokemonList.find { it.uuid == uuid } ?: throw IllegalStateException("Unable to find ${actor.showdownId}'s Pokemon with UUID: $uuid")
            battle.broadcastChatMessage(battleLang("dragged_out", pokemon.getName()))
            val entity = if (actor is EntityBackedBattleActor<*>) actor.entity else null
            battle.dispatch {
                if (entity != null) {
                    this.createEntitySwitch(battle, actor, entity, pnx, activePokemon, pokemon)
                } else {
                    this.createNonEntitySwitch(battle, actor, pnx, activePokemon, pokemon)
                }
            }
        }

    }

    // |-hitcount|POKEMON|NUM
    fun handleHitCountInstruction(battle: PokemonBattle, message: String, remainingLines: MutableList<String>) {
        battle.dispatchGo {
            val hitCount = message.substringAfterLast("|").toIntOrNull() ?: -1
            val lang = if (hitCount == 1) battleLang("hit_count_singular") else battleLang("hit_count", hitCount)
            battle.broadcastChatMessage(lang)
        }
    }


    fun handleItemInstruction(battle: PokemonBattle, baseMessage: String, remainingLines: MutableList<String>) {
        battle.dispatchGo {
            val battleMessage = BattleMessage(baseMessage)
            val battlePokemon = battleMessage.actorAndActivePokemon(0, battle)?.second?.battlePokemon ?: return@dispatchGo
            battlePokemon.heldItemManager.handleStartInstruction(battlePokemon, battle, battleMessage)
        }
    }

    fun handleEndItemInstruction(battle: PokemonBattle, baseMessage: String, remainingLines: MutableList<String>) {
        battle.dispatchGo {
            val battleMessage = BattleMessage(baseMessage)
            val battlePokemon = battleMessage.actorAndActivePokemon(0, battle)?.second?.battlePokemon ?: return@dispatchGo
            battlePokemon.heldItemManager.handleEndInstruction(battlePokemon, battle, battleMessage)
        }
    }

}