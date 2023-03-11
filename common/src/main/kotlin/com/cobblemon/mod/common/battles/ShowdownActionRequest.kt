/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.battles

import com.cobblemon.mod.common.net.IntSize
import com.cobblemon.mod.common.util.readSizedInt
import com.cobblemon.mod.common.util.writeSizedInt
import java.lang.Integer.max
import java.util.UUID
import net.minecraft.network.PacketByteBuf
class ShowdownActionRequest(
    var wait: Boolean = false,
    var active: MutableList<ShowdownMoveset>? = null,
    var forceSwitch: List<Boolean> = emptyList(),
    var noCancel: Boolean = false,
    var side: ShowdownSide? = null
) {
    fun <T, E : Targetable> iterate(activePokemon: List<E>, iterator: (E, ShowdownMoveset?, forceSwitch: Boolean) -> T): List<T> {
        val size = max(active?.size ?: 0, forceSwitch.size)
        val responses = mutableListOf<T>()
        repeat(times = size) { index ->
            val activeBattlePokemon = activePokemon.let { if (it.size > index) it[index] else throw IllegalStateException("No active Pokémon for slot $index but needed to choose action for it?") }
            val moveset = active?.let { if (it.size > index) it[index] else null }
            val forceSwitch = forceSwitch.let { if (it.size > index) it[index] else false }
            responses.add(iterator(activeBattlePokemon, moveset, forceSwitch))
        }
        return responses
    }

    fun saveToBuffer(buffer: PacketByteBuf) {
        buffer.writeBoolean(wait)
        buffer.writeSizedInt(IntSize.U_BYTE, active?.size ?: 0)
        active?.forEach { it.saveToBuffer(buffer) }
        buffer.writeSizedInt(IntSize.U_BYTE, forceSwitch.size)
        forceSwitch.forEach(buffer::writeBoolean)
        buffer.writeBoolean(noCancel)
        buffer.writeBoolean(side != null)
        side?.saveToBuffer(buffer)
    }

    fun loadFromBuffer(buffer: PacketByteBuf): ShowdownActionRequest {
        wait = buffer.readBoolean()
        val activeSize = buffer.readSizedInt(IntSize.U_BYTE)
        if (activeSize > 0) {
            val active = mutableListOf<ShowdownMoveset>()
            repeat(times = activeSize) { active.add(ShowdownMoveset().loadFromBuffer(buffer)) }
            this.active = active
        }
        val forceSwitch = mutableListOf<Boolean>()
        repeat(times = buffer.readSizedInt(IntSize.U_BYTE)) { forceSwitch.add(buffer.readBoolean()) }
        this.forceSwitch = forceSwitch
        noCancel = buffer.readBoolean()
        if (buffer.readBoolean()) {
            side = ShowdownSide().loadFromBuffer(buffer)
        }
        return this
    }
}

enum class ShowdownActionResponseType(val loader: (PacketByteBuf) -> ShowdownActionResponse) {
    SWITCH({ SwitchActionResponse(UUID.randomUUID()) }),
    MOVE({ MoveActionResponse("", null) }),
    DEFAULT({ DefaultActionResponse() }),
    BALL({ BallActionResponse() }),
    PASS({ PassActionResponse });
}

abstract class ShowdownActionResponse(val type: ShowdownActionResponseType) {
    companion object {
        fun loadFromBuffer(buffer: PacketByteBuf): ShowdownActionResponse {
            val type = ShowdownActionResponseType.values()[buffer.readSizedInt(IntSize.U_BYTE)]
            return type.loader(buffer).loadFromBuffer(buffer)
        }
    }

    open fun saveToBuffer(buffer: PacketByteBuf) {
        buffer.writeSizedInt(IntSize.U_BYTE, type.ordinal)
    }

    open fun loadFromBuffer(buffer: PacketByteBuf): ShowdownActionResponse = this
    abstract fun isValid(activeBattlePokemon: ActiveBattlePokemon, showdownMoveSet: ShowdownMoveset?, forceSwitch: Boolean): Boolean
    abstract fun toShowdownString(activeBattlePokemon: ActiveBattlePokemon, showdownMoveSet: ShowdownMoveset?): String
}

data class MoveActionResponse(var moveName: String, var targetPnx: String? = null): ShowdownActionResponse(ShowdownActionResponseType.MOVE) {
    override fun isValid(activeBattlePokemon: ActiveBattlePokemon, showdownMoveSet: ShowdownMoveset?, forceSwitch: Boolean): Boolean {
        if (forceSwitch || showdownMoveSet == null) {
            return false
        }

        val move = showdownMoveSet.moves.find { it.id == moveName } ?: return false
        if (!move.canBeUsed()) {
            // No PP or disabled or something
            return false
        }
        val availableTargets = move.target.targetList(activeBattlePokemon)?.takeIf { it.isNotEmpty() } ?: return true

        val pnx = targetPnx ?: return false // If the targets list is non-null then they need to have specified a target
        val (_, targetPokemon) = activeBattlePokemon.actor.battle.getActorAndActiveSlotFromPNX(pnx)
        if (targetPokemon !in availableTargets || targetPokemon.battlePokemon == null || targetPokemon.battlePokemon!!.health <= 0) {
            return false // It's not a possible target.
        }

        return true
    }

    override fun toShowdownString(activeBattlePokemon: ActiveBattlePokemon, showdownMoveSet: ShowdownMoveset?): String {
        val pnx = targetPnx
        showdownMoveSet!!
        val moveIndex = showdownMoveSet.moves.indexOfFirst { it.id == moveName } + 1

        return if (pnx != null) {
            val (_, targetPokemon) = activeBattlePokemon.actor.battle.getActorAndActiveSlotFromPNX(pnx)
            val digit = targetPokemon.getSignedDigitRelativeTo(activeBattlePokemon)
            "move $moveIndex $digit"
        } else {
            "move $moveIndex"
        }
    }

    override fun saveToBuffer(buffer: PacketByteBuf) {
        super.saveToBuffer(buffer)
        buffer.writeString(moveName)
        buffer.writeBoolean(targetPnx != null)
        targetPnx?.let(buffer::writeString)
    }

    override fun loadFromBuffer(buffer: PacketByteBuf): ShowdownActionResponse {
        super.loadFromBuffer(buffer)
        moveName = buffer.readString()
        if (buffer.readBoolean()) {
            targetPnx = buffer.readString()
        }
        return this
    }
}

data class SwitchActionResponse(var newPokemonId: UUID) : ShowdownActionResponse(ShowdownActionResponseType.SWITCH) {
    override fun saveToBuffer(buffer: PacketByteBuf) {
        super.saveToBuffer(buffer)
        buffer.writeUuid(newPokemonId)
    }

    override fun loadFromBuffer(buffer: PacketByteBuf): ShowdownActionResponse {
        super.loadFromBuffer(buffer)
        newPokemonId = buffer.readUuid()
        return this
    }

    override fun isValid(activeBattlePokemon: ActiveBattlePokemon, showdownMoveSet: ShowdownMoveset?, forceSwitch: Boolean): Boolean {
        val pokemon = activeBattlePokemon.actor.pokemonList.find { it.uuid == newPokemonId }
        return when {
            pokemon == null -> false // No such Pokémon
            pokemon.health <= 0 -> false // Pokémon is dead
            showdownMoveSet != null && showdownMoveSet.trapped -> false // You're not allowed to switch
            activeBattlePokemon.actor.getSide().activePokemon.any { it.battlePokemon?.uuid == newPokemonId } -> false // Pokémon is already sent out
            else -> true
        }
    }

    override fun toShowdownString(activeBattlePokemon: ActiveBattlePokemon, showdownMoveSet: ShowdownMoveset?): String {
        return "switch ${activeBattlePokemon.actor.pokemonList.indexOfFirst { it.uuid == newPokemonId } + 1}"
    }
}
class DefaultActionResponse: ShowdownActionResponse(ShowdownActionResponseType.DEFAULT) {
    override fun isValid(activeBattlePokemon: ActiveBattlePokemon, showdownMoveSet: ShowdownMoveset?, forceSwitch: Boolean) = true
    override fun toShowdownString(activeBattlePokemon: ActiveBattlePokemon, showdownMoveSet: ShowdownMoveset?) = "default"
}

object PassActionResponse : ShowdownActionResponse(ShowdownActionResponseType.PASS) {
    override fun isValid(activeBattlePokemon: ActiveBattlePokemon, showdownMoveSet: ShowdownMoveset?, forceSwitch: Boolean) = true
    override fun toShowdownString(activeBattlePokemon: ActiveBattlePokemon, showdownMoveSet: ShowdownMoveset?) = "pass"
}
class BallActionResponse() : ShowdownActionResponse(ShowdownActionResponseType.BALL) {
    override fun isValid(activeBattlePokemon: ActiveBattlePokemon, showdownMoveSet: ShowdownMoveset?, forceSwitch: Boolean): Boolean {
        if (forceSwitch) {
            return false
        } else if (showdownMoveSet == null) {
            return false
        }

        return activeBattlePokemon.actor.expectingCaptureActions-- > 0
    }

    override fun toShowdownString(activeBattlePokemon: ActiveBattlePokemon, showdownMoveSet: ShowdownMoveset?) = "pass"
}
class ShowdownMoveset {
    lateinit var moves: List<InBattleMove>
    var trapped = false

    fun saveToBuffer(buffer: PacketByteBuf) {
        buffer.writeSizedInt(IntSize.U_BYTE, moves.size)
        moves.forEach { it.saveToBuffer(buffer) }
        buffer.writeBoolean(trapped)
    }

    fun loadFromBuffer(buffer: PacketByteBuf): ShowdownMoveset {
        val moves = mutableListOf<InBattleMove>()
        repeat(times = buffer.readSizedInt(IntSize.U_BYTE)) {
            moves.add(InBattleMove.loadFromBuffer(buffer))
        }
        this.moves = moves
        this.trapped = buffer.readBoolean()
        return this
    }
}
class ShowdownSide {
    lateinit var name: UUID
    lateinit var id: String
    lateinit var pokemon: List<ShowdownPokemon>
    fun saveToBuffer(buffer: PacketByteBuf) {
        buffer.writeUuid(name)
        buffer.writeString(id)
        buffer.writeSizedInt(IntSize.U_BYTE, pokemon.size)
        pokemon.forEach { it.saveToBuffer(buffer) }
    }
    fun loadFromBuffer(buffer: PacketByteBuf): ShowdownSide {
        name = buffer.readUuid()
        id = buffer.readString()
        val pokemon = mutableListOf<ShowdownPokemon>()
        repeat(times = buffer.readSizedInt(IntSize.U_BYTE)) {
            pokemon.add(ShowdownPokemon().loadFromBuffer(buffer))
        }
        this.pokemon = pokemon
        return this
    }
}
class ShowdownPokemon {
    lateinit var ident: String
    lateinit var details: String
    lateinit var condition: String
    var active: Boolean = false
    val moves = mutableListOf<String>()
    lateinit var baseAbility: String
    lateinit var pokeball: String
    lateinit var ability: String

    val uuid: UUID by lazy { UUID.fromString(details.split(",")[1].trim()) }
    fun saveToBuffer(buffer: PacketByteBuf) {
        buffer.writeString(ident)
        buffer.writeString(details)
        buffer.writeString(condition)
        buffer.writeBoolean(active)
        buffer.writeSizedInt(IntSize.U_BYTE, moves.size)
        moves.forEach(buffer::writeString)
        buffer.writeString(baseAbility)
        buffer.writeString(pokeball)
        buffer.writeString(ability)

    }
    fun loadFromBuffer(buffer: PacketByteBuf): ShowdownPokemon {
        ident = buffer.readString()
        details = buffer.readString()
        condition = buffer.readString()
        active = buffer.readBoolean()
        repeat(times = buffer.readSizedInt(IntSize.U_BYTE)) {
            moves.add(buffer.readString())
        }
        baseAbility = buffer.readString()
        pokeball = buffer.readString()
        ability = buffer.readString()
        return this
    }
}