/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages

import com.cobblemon.mod.common.api.abilities.Abilities
import com.cobblemon.mod.common.api.moves.BenchedMoves
import com.cobblemon.mod.common.api.moves.MoveSet
import com.cobblemon.mod.common.api.net.Decodable
import com.cobblemon.mod.common.api.net.Encodable
import com.cobblemon.mod.common.api.pokeball.PokeBalls
import com.cobblemon.mod.common.api.pokemon.Natures
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.pokemon.status.Statuses
import com.cobblemon.mod.common.net.IntSize
import com.cobblemon.mod.common.pokemon.EVs
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.pokemon.IVs
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.activestate.PokemonState
import com.cobblemon.mod.common.pokemon.status.PersistentStatus
import com.cobblemon.mod.common.pokemon.status.PersistentStatusContainer
import com.cobblemon.mod.common.util.readSizedInt
import com.cobblemon.mod.common.util.writeSizedInt
import io.netty.buffer.Unpooled
import net.minecraft.item.ItemStack
import java.util.UUID
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier

/**
 * A data transfer object for an entire [Pokemon], complete with all of the information a player is allowed
 * to know about their own Pokémon. The main purpose of this is to have a write- and read-safe object that
 * doesn't need reference data to be loaded prior to being mapped back into a [Pokemon] instance.
 *
 * @author Hiroku
 * @since November 10th, 2022
 */
class PokemonDTO : Encodable, Decodable {
    var toClient = false
    var uuid = UUID.randomUUID()
    lateinit var species: Identifier
    var form = ""
    var level = 1
    var experience = 1
    var friendship = 0
    var currentHealth = 0
    var gender = Gender.MALE
    var ivs = IVs()
    var evs = EVs()
    var moveSet = MoveSet()
    var scaleModifier = 0F
    var ability = ""
    var shiny = false
    var status: Identifier? = null
    lateinit var state: PokemonState
    lateinit var caughtBall: Identifier
    var benchedMoves = BenchedMoves()
    var aspects = setOf<String>()
    lateinit var evolutionBuffer: PacketByteBuf
    lateinit var nature: Identifier
    var heldItem: ItemStack = ItemStack.EMPTY

    constructor()
    constructor(pokemon: Pokemon, toClient: Boolean) {
        this.toClient = toClient
        this.uuid = pokemon.uuid
        this.species = pokemon.species.resourceIdentifier
        this.form = pokemon.form.name
        this.level = pokemon.level
        this.experience = pokemon.experience
        this.friendship = pokemon.friendship
        this.currentHealth = pokemon.currentHealth
        this.gender = pokemon.gender
        this.ivs = pokemon.ivs
        this.evs = pokemon.evs
        this.moveSet = pokemon.moveSet
        this.scaleModifier = pokemon.scaleModifier
        this.ability = pokemon.ability.name
        this.shiny = pokemon.shiny
        this.status = pokemon.status?.status?.name
        this.state = pokemon.state
        this.caughtBall = pokemon.caughtBall.name
        this.aspects = pokemon.aspects
        evolutionBuffer = PacketByteBuf(Unpooled.buffer())
        pokemon.evolutionProxy.saveToBuffer(evolutionBuffer, toClient)
        this.nature = pokemon.nature.name
        this.heldItem = pokemon.heldItemNoCopy()
    }

    override fun encode(buffer: PacketByteBuf) {
        buffer.writeBoolean(toClient)
        buffer.writeUuid(uuid)
        buffer.writeIdentifier(species)
        buffer.writeString(form)
        buffer.writeInt(experience)
        buffer.writeSizedInt(IntSize.U_SHORT, level)
        buffer.writeShort(friendship)
        buffer.writeShort(currentHealth)
        buffer.writeSizedInt(IntSize.U_BYTE, gender.ordinal)
        ivs.saveToBuffer(buffer)
        evs.saveToBuffer(buffer)
        moveSet.saveToBuffer(buffer)
        buffer.writeFloat(scaleModifier)
        buffer.writeString(ability)
        buffer.writeBoolean(shiny)
        state.writeToBuffer(buffer)
        buffer.writeNullable(status) { _, v -> buffer.writeIdentifier(v) }
        buffer.writeIdentifier(caughtBall)
        benchedMoves.saveToBuffer(buffer)
        buffer.writeSizedInt(IntSize.U_BYTE, aspects.size)
        aspects.forEach { buffer.writeString(it) }
        val byteCount = evolutionBuffer.readableBytes()
        buffer.writeSizedInt(IntSize.U_SHORT, byteCount)
        buffer.writeBytes(evolutionBuffer)
        evolutionBuffer.release()
        buffer.writeIdentifier(nature)
        buffer.writeItemStack(this.heldItem)
    }

    override fun decode(buffer: PacketByteBuf) {
        toClient = buffer.readBoolean()
        uuid = buffer.readUuid()
        species = buffer.readIdentifier()
        form = buffer.readString()
        experience = buffer.readInt()
        level = buffer.readSizedInt(IntSize.U_SHORT)
        friendship = buffer.readUnsignedShort()
        currentHealth = buffer.readUnsignedShort()
        gender = Gender.values()[buffer.readSizedInt(IntSize.U_BYTE)]
        ivs.loadFromBuffer(buffer)
        evs.loadFromBuffer(buffer)
        moveSet.loadFromBuffer(buffer)
        scaleModifier = buffer.readFloat()
        ability = buffer.readString()
        shiny = buffer.readBoolean()
        state = PokemonState.fromBuffer(buffer)
        status = buffer.readNullable { buffer.readIdentifier() }
        caughtBall = buffer.readIdentifier()
        benchedMoves.loadFromBuffer(buffer)
        val aspects = mutableSetOf<String>()
        repeat(times = buffer.readSizedInt(IntSize.U_BYTE)) {
            aspects.add(buffer.readString())
        }
        this.aspects = aspects
        val bytesToRead = buffer.readSizedInt(IntSize.U_SHORT)
        evolutionBuffer = PacketByteBuf(buffer.readBytes(bytesToRead))
        nature = buffer.readIdentifier()
        heldItem = buffer.readItemStack()
    }

    fun create(): Pokemon {
        return Pokemon().also {
            it.isClient = toClient
            it.uuid = uuid
            it.species = PokemonSpecies.getByIdentifier(species)!!
            it.form = it.species.forms.find { it.name == form } ?: it.species.standardForm
            it.experience = experience
            it.level = level
            it.setFriendship(friendship)
            it.currentHealth = currentHealth
            it.gender = gender
            it.ivs = ivs
            it.evs = evs
            it.moveSet.clear()
            for (move in moveSet) {
                it.moveSet.add(move)
            }
            it.scaleModifier = scaleModifier
            it.ability = Abilities.getOrException(ability).create()
            it.shiny = shiny
            it.state = state
            it.status = status?.let { id ->
                val statusType = Statuses.getStatus(id)
                if (statusType is PersistentStatus) {
                    PersistentStatusContainer(statusType, 0)
                } else {
                    null
                }
            }
            it.caughtBall = PokeBalls.getPokeBall(caughtBall)!!
            it.benchedMoves.addAll(benchedMoves)
            it.aspects = aspects
            it.evolutionProxy.loadFromBuffer(evolutionBuffer)
            evolutionBuffer.release()
            it.nature = Natures.getNature(nature)!!
            it.swapHeldItem(heldItem, false)
        }
    }
}