/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.activestate

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.DataKeys
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.getPlayer
import com.cobblemon.mod.common.util.isPokemonEntity
import com.cobblemon.mod.common.util.party
import com.cobblemon.mod.common.util.playSoundServer
import com.google.gson.JsonObject
import java.util.UUID
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Identifier
import net.minecraft.util.registry.RegistryKey
import net.minecraft.world.World

sealed class PokemonState {
    companion object {
        val states = mapOf(
            "inactive" to InactivePokemonState::class.java,
            "sent-out" to SentOutState::class.java,
            "shouldered" to ShoulderedState::class.java
        )

        fun fromBuffer(buffer: PacketByteBuf): PokemonState {
            val type = buffer.readString()
            return states[type]?.newInstance()?.readFromBuffer(buffer) ?: InactivePokemonState()
        }
    }

    val name: String
        get() = states.entries.find { it.value == this::class.java }!!.key

    open fun getIcon(pokemon: Pokemon): Identifier? = null

    open fun writeToNBT(nbt: NbtCompound): NbtCompound? {
        nbt.putString(DataKeys.POKEMON_STATE_TYPE, name)
        return nbt
    }

    open fun readFromNBT(nbt: NbtCompound): PokemonState = this
    open fun writeToJSON(json: JsonObject): JsonObject? = json
    open fun readFromJSON(json: JsonObject): PokemonState = this
    open fun writeToBuffer(buffer: PacketByteBuf) {
        buffer.writeString(name)
    }
    open fun readFromBuffer(buffer: PacketByteBuf): PokemonState = this
}
class InactivePokemonState : PokemonState() {
    override fun writeToNBT(nbt: NbtCompound) = null
}

sealed class ActivePokemonState : PokemonState() {
    abstract val entity: PokemonEntity?
    abstract fun recall()
}
class SentOutState() : ActivePokemonState() {
    private var entityId: Int = -1
    private var dimension = World.OVERWORLD

    override val entity: PokemonEntity?
        get() = Cobblemon.getLevel(dimension)?.getEntityById(entityId) as? PokemonEntity

    constructor(entity: PokemonEntity): this() {
        this.entityId = entity.id
        this.dimension = entity.world.registryKey
    }

    override fun getIcon(pokemon: Pokemon) = cobblemonResource("ui/party/party_icon_released.png")
    override fun writeToNBT(nbt: NbtCompound) = null
    override fun writeToJSON(json: JsonObject) = null

    override fun writeToBuffer(buffer: PacketByteBuf) {
        super.writeToBuffer(buffer)
        buffer.writeInt(entityId)
        buffer.writeString(dimension.value.toString())
    }

    override fun readFromBuffer(buffer: PacketByteBuf): SentOutState {
        super.readFromBuffer(buffer)
        entityId = buffer.readInt()
        dimension = RegistryKey.of(RegistryKey.ofRegistry(dimension.value), Identifier(buffer.readString()))
        return this
    }

    fun update(entity: PokemonEntity) {
        entityId =  entity.id
        dimension = entity.world.registryKey
    }

    override fun recall() {
        entity?.discard()
    }
}
class ShoulderedState() : ActivePokemonState() {
    var isLeftShoulder = false
    lateinit var playerUUID: UUID
    lateinit var pokemonUUID: UUID
    var stateId = UUID.randomUUID()

    constructor(playerUUID: UUID, isLeftShoulder: Boolean, pokemonUUID: UUID): this() {
        this.isLeftShoulder = isLeftShoulder
        this.playerUUID = playerUUID
        this.pokemonUUID = pokemonUUID
    }

    override val entity: PokemonEntity? = null

    override fun getIcon(pokemon: Pokemon): Identifier {
        val suffix = if (isLeftShoulder) "left" else "right"
        return cobblemonResource("ui/party/party_icon_shoulder_$suffix.png")
    }
    override fun writeToNBT(nbt: NbtCompound): NbtCompound {
        super.writeToNBT(nbt)
        nbt.putBoolean(DataKeys.POKEMON_STATE_SHOULDER, isLeftShoulder)
        nbt.putUuid(DataKeys.POKEMON_STATE_PLAYER_UUID, playerUUID)
        nbt.putUuid(DataKeys.POKEMON_STATE_ID, stateId)
        nbt.putUuid(DataKeys.POKEMON_STATE_POKEMON_UUID, pokemonUUID)
        return nbt
    }

    override fun readFromNBT(nbt: NbtCompound): PokemonState {
        super.readFromNBT(nbt)
        isLeftShoulder = nbt.getBoolean(DataKeys.POKEMON_STATE_SHOULDER)
        playerUUID = nbt.getUuid(DataKeys.POKEMON_STATE_PLAYER_UUID)
        stateId = nbt.getUuid(DataKeys.POKEMON_STATE_ID)
        pokemonUUID = nbt.getUuid(DataKeys.POKEMON_STATE_POKEMON_UUID)
        return this
    }

    override fun writeToJSON(json: JsonObject): JsonObject? {
        super.writeToJSON(json)
        json.addProperty(DataKeys.POKEMON_STATE_SHOULDER, isLeftShoulder)
        json.addProperty(DataKeys.POKEMON_STATE_PLAYER_UUID, playerUUID.toString())
        json.addProperty(DataKeys.POKEMON_STATE_ID, stateId.toString())
        json.addProperty(DataKeys.POKEMON_STATE_POKEMON_UUID, pokemonUUID.toString())
        return json
    }

    override fun readFromJSON(json: JsonObject): PokemonState {
        super.readFromJSON(json)
        isLeftShoulder = json.get(DataKeys.POKEMON_STATE_SHOULDER).asBoolean
        playerUUID = UUID.fromString(json.get(DataKeys.POKEMON_STATE_PLAYER_UUID).asString)
        stateId = UUID.fromString(json.get(DataKeys.POKEMON_STATE_ID).asString)
        pokemonUUID = UUID.fromString(json.get(DataKeys.POKEMON_STATE_POKEMON_UUID).asString)
        return this
    }

    override fun writeToBuffer(buffer: PacketByteBuf) {
        super.writeToBuffer(buffer)
        buffer.writeBoolean(isLeftShoulder)
        buffer.writeUuid(playerUUID)
        buffer.writeUuid(stateId)
        buffer.writeUuid(pokemonUUID)
    }

    override fun readFromBuffer(buffer: PacketByteBuf): PokemonState {
        super.readFromBuffer(buffer)
        isLeftShoulder = buffer.readBoolean()
        playerUUID = buffer.readUuid()
        stateId = buffer.readUuid()
        pokemonUUID = buffer.readUuid()
        return this
    }

    override fun recall() {
        val player = playerUUID.getPlayer() ?: return
        val nbt = if (isLeftShoulder) player.shoulderEntityLeft else player.shoulderEntityRight
        if (this.isShoulderedPokemon(nbt)) {
            player.world.playSoundServer(player.pos, SoundEvents.BLOCK_CANDLE_FALL)
            if (isLeftShoulder) {
                player.shoulderEntityLeft = NbtCompound()
            } else {
                player.shoulderEntityRight = NbtCompound()
            }
            this.removeShoulderEffects(player)
        }
    }

    private fun removeShoulderEffects(player: ServerPlayerEntity) {
        val partyPokemon = player.party().find { pokemon -> pokemon.uuid == this.pokemonUUID }
        partyPokemon?.form?.shoulderEffects?.forEach { effect -> effect.removeEffect(partyPokemon, player, isLeftShoulder) }
    }

    private fun isShoulderedPokemon(nbt: NbtCompound): Boolean = nbt.isPokemonEntity()
            && nbt.getCompound(DataKeys.POKEMON)
            .getCompound(DataKeys.POKEMON_STATE)
            .getUuid(DataKeys.POKEMON_STATE_ID) == this.stateId

    fun isStillShouldered(player: ServerPlayerEntity) = isShoulderedPokemon(if (isLeftShoulder) player.shoulderEntityLeft else player.shoulderEntityRight)
}