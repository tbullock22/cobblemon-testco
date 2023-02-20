/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.battles.runner.GraalShowdown
import com.cobblemon.mod.common.net.messages.client.effect.SpawnSnowstormParticlePacket
import com.cobblemon.mod.common.particle.SnowstormParticleReader
import com.cobblemon.mod.common.util.fromJson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import java.io.File
import java.io.PrintWriter
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity

object TestCommand {

    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        val command = CommandManager.literal("testcommand")
            .requires { it.hasPermissionLevel(4) }
            .executes { execute(it) }
        dispatcher.register(command)
    }

    private fun execute(context: CommandContext<ServerCommandSource>): Int {
        if (context.source.entity !is ServerPlayerEntity) {
            return Command.SINGLE_SUCCESS
        }

        try {
            testParticles(context)

//            extractMovesData()
//            // Player variables
//            val player = context.source.entity as ServerPlayerEntity
//            val party = player.party()
//            party.heal()
//
//            val playerActor = PlayerBattleActor(
//                player.uuid,
//                party.toBattleTeam()
//            )
//
//            // Enemy variables
//            val pokemon = Pokemon().apply { species = PokemonSpecies.random() }.also { it.initialize() }
//            val enemyPokemon = BattlePokemon(pokemon)
//
//            val enemyPokemon2 = BattlePokemon(PokemonSpecies.random().create())
//            val enemyPokemon3 = BattlePokemon(PokemonSpecies.random().create())
//            val enemyPokemon4 = BattlePokemon(PokemonSpecies.random().create())
//
//            enemyPokemon.effectedPokemon.sendOut(player.world as ServerWorld, player.pos.add(2.0, 0.0, 0.0))
//            enemyPokemon2.effectedPokemon.sendOut(player.world as ServerWorld, player.pos.add(-2.0, 0.0, 0.0))
//            enemyPokemon3.effectedPokemon.sendOut(player.world as ServerWorld, player.pos.add(0.0, 0.0, 2.0))
//            enemyPokemon4.effectedPokemon.sendOut(player.world as ServerWorld, player.pos.add(0.0, 0.0, -2.0))
//
//            // Start the battle
//            BattleRegistry.startBattle(
//                battleFormat = BattleFormat.GEN_8_DOUBLES,
//                side1 = BattleSide(playerActor),
//                side2 = BattleSide(MultiPokemonBattleActor(listOf(enemyPokemon, enemyPokemon2, enemyPokemon3, enemyPokemon4)))
//            )

//            val player = context.source.entity as ServerPlayerEntity
//            player.giveItemStack(PokemonItem.from(PokemonSpecies.random(), "alolan"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Command.SINGLE_SUCCESS
    }

    private fun testParticles(context: CommandContext<ServerCommandSource>) {
        val file = File("particle.particle.json")
        val effect = SnowstormParticleReader.loadEffect(GsonBuilder().create().fromJson<JsonObject>(file.readText()))

        val player = context.source.entity as ServerPlayerEntity
        val position = player.pos.add(4.0, 1.0, 4.0)
        val pkt = SpawnSnowstormParticlePacket(effect, position)
        player.sendPacket(pkt)
    }

    private fun extractMovesData() {
        val ctx = GraalShowdown.context
        ctx.eval("js", """
                const ShowdownMoves = require('pokemon-showdown/data/moves');
            """.trimIndent())
        val moves = ctx.getBindings("js").getMember("ShowdownMoves").getMember("Moves")
        val gson = GsonBuilder().setPrettyPrinting().create()
        for (moveName in moves.memberKeys) {
            try {
                val value = moves.getMember(moveName)
                val obj = JsonObject()
                obj.addProperty("name", moveName)
                obj.addProperty("type", value.getMember("type").asString())
                obj.addProperty("damageCategory", value.getMember("category").asString())
                obj.addProperty("target", value.getMember("target").asString())
                obj.addProperty("power", value.getMember("basePower").asInt())
                obj.addProperty("accuracy", value.getMember("accuracy").let { if (it.isBoolean) -1F else it.asFloat() })
                obj.addProperty("pp", value.getMember("pp").asInt())
                obj.addProperty("priority", value.getMember("priority").asInt())
                if (value.hasMember("secondary")) {
                    val secondary = value.getMember("secondary")
                    if (secondary.hasMember("chance")) {
                        obj.addProperty("effectChance", secondary.getMember("chance").asInt())
                    }
                }
                val file = File("outputmoves").also { it.mkdir() }
                val pw = PrintWriter(File(file, "$moveName.json"))
                pw.write(gson.toJson(obj))
                pw.close()
            } catch (e: Exception) {
                println("Issue when converting $moveName")
                e.printStackTrace()
            }
        }
    }

    private fun extractAbilitiesData() {
        val ctx = GraalShowdown.context
        ctx.eval("js", """
            const ShowdownAbilities = require('pokemon-showdown/data/abilities');
        """.trimIndent())
        val abilities = ctx.getBindings("js").getMember("ShowdownAbilities").getMember("Abilities")
        val gson = GsonBuilder().setPrettyPrinting().create()
        for (abilityName in abilities.memberKeys) {
            try {
                val obj = JsonObject()
                obj.addProperty("name", abilityName)
                obj.addProperty("displayName", "cobblemon.ability.$abilityName")
                obj.addProperty("description", "cobblemon.ability.$abilityName.desc")
                val file = File("outputabilities").also { it.mkdir() }
                val pw = PrintWriter(File(file, "$abilityName.json"))
                pw.write(gson.toJson(obj))
                pw.close()
            } catch (e: Exception) {
                println("Issue when converting $abilityName")
                e.printStackTrace()
            }
        }
    }
}