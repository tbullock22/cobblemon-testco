/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.moves

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.data.DataRegistry
import com.cobblemon.mod.common.api.data.ShowdownIdentifiable
import com.cobblemon.mod.common.api.moves.categories.DamageCategories
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.api.types.ElementalTypes
import com.cobblemon.mod.common.battles.MoveTarget
import com.cobblemon.mod.common.battles.runner.GraalShowdown
import com.cobblemon.mod.common.net.messages.client.data.MovesRegistrySyncPacket
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceType
import net.minecraft.server.network.ServerPlayerEntity

/**
 * Registry for all known Moves
 */
object Moves : DataRegistry {

    override val id = cobblemonResource("moves")
    override val type = ResourceType.SERVER_DATA
    override val observable = SimpleObservable<Moves>()

    private val allMoves = mutableMapOf<String, MoveTemplate>()
    private val idMapping = mutableMapOf<Int, MoveTemplate>()

    override fun reload(manager: ResourceManager) {
        this.allMoves.clear()
        this.idMapping.clear()
        val script = """
            PokemonShowdown.Dex.mod("${Cobblemon.MODID}")
              .moves.all();
        """.trimIndent()
        val arrayResult = GraalShowdown.context.eval("js", script)
        for (i in 0 until arrayResult.arraySize) {
            val jsMove = arrayResult.getArrayElement(i)
            val id = jsMove.getMember("id").asString()
            try {
                val elementalType = ElementalTypes.getOrException(jsMove.getMember("type").asString())
                val damageCategory = DamageCategories.getOrException(jsMove.getMember("category").asString())
                val power = jsMove.getMember("basePower").asDouble()
                val target = MoveTarget.fromShowdownId(jsMove.getMember("target").asString())
                // If not a double it's always true
                val accuracy = if (!jsMove.getMember("accuracy").fitsInDouble()) -1.0 else jsMove.getMember("accuracy").asDouble()
                val pp = jsMove.getMember("pp").asInt()
                val priority = jsMove.getMember("priority").asInt()
                val critRatio = if (jsMove.hasMember("critRatio")) jsMove.getMember("critRatio").asDouble() else 1.0
                val effectChances = arrayListOf<Double>()
                val secondariesMember = jsMove.getMember("secondaries")
                val secondaryMember = jsMove.getMember("secondary")
                if (!secondariesMember.isNull) {
                    for (j in 0 until secondariesMember.arraySize) {
                        val element = secondariesMember.getArrayElement(j)
                        // They declare moves without data on secondary effects for sheer force compatibility
                        if (element.hasMember("chance")) {
                            effectChances += element.getMember("chance").asDouble()
                        }
                    }
                }
                else if (!secondaryMember.isNull) {
                    // They declare moves without data on secondary effects for sheer force compatibility
                    if (secondaryMember.hasMember("chance")) {
                        effectChances += secondaryMember.getMember("chance").asDouble()
                    }
                }
                val move = MoveTemplate(id, elementalType, damageCategory, power, target, accuracy, pp, priority, critRatio, effectChances.toTypedArray())
                this.register(move)
            } catch (e: Exception) {
                Cobblemon.LOGGER.error("Caught exception trying to resolve the move '{}'", id, e)
            }
        }
        this.applyIDs()
        Cobblemon.LOGGER.info("Loaded {} moves", this.allMoves.size)
        this.observable.emit(this)
    }

    override fun sync(player: ServerPlayerEntity) {
        MovesRegistrySyncPacket(all()).sendToPlayer(player)
    }

    fun getByName(name: String) = allMoves[name.lowercase()]
    fun getByNumericalId(id: Int) = idMapping[id]!!
    fun getByNameOrDummy(name: String) = allMoves[name.lowercase()] ?: MoveTemplate.dummy(name.lowercase())
    fun getExceptional() = getByName("tackle") ?: allMoves.values.random()
    fun count() = allMoves.size
    fun names(): Collection<String> = this.allMoves.keys.toSet()
    fun all() = this.allMoves.values.toList()

    internal fun receiveSyncPacket(moves: Collection<MoveTemplate>) {
        moves.forEach { move ->
            this.register(move)
            this.idMapping[move.id] = move
        }
    }

    private fun register(move: MoveTemplate) {
        this.allMoves[move.name] = move
    }

    private fun applyIDs() {
        var id = 1
        this.allMoves.values
            .sortedBy { it.name }
            .forEach {
                it.id = id++
                idMapping[it.id] = it
            }
    }

}