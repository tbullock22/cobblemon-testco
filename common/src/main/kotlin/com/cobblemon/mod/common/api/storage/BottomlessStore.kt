/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage

import com.cobblemon.mod.common.api.reactive.Observable.Companion.stopAfter
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.DataKeys
import com.google.gson.JsonObject
import java.util.UUID
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.network.ServerPlayerEntity

class BottomlessPosition(val currentIndex: Int) : StorePosition

/**
 * A [PokemonStore] that has no maximum capacity. It's used internally as an overflow store.
 *
 * @author Hiroku
 * @since May 2nd, 2022
 */
open class BottomlessStore(override val uuid: UUID) : PokemonStore<BottomlessPosition>() {
    val pokemon = mutableListOf<Pokemon>()
    val storeChangeObservable = SimpleObservable<Unit>()

    override fun iterator() = pokemon.iterator()

    override fun get(position: BottomlessPosition) = position.currentIndex
        .takeIf { it < pokemon.size && it >= 0 }
        ?.let { pokemon[it] }

    override fun getFirstAvailablePosition() = BottomlessPosition(pokemon.size)
    override fun isValidPosition(position: BottomlessPosition) = position.currentIndex >= 0
    operator fun get(index: Int) = index.takeIf { it >= 0 && it < pokemon.size }?.let { pokemon[it] }
    override fun getObservingPlayers() = emptySet<ServerPlayerEntity>()
    override fun sendTo(player: ServerPlayerEntity) {}

    override fun initialize() {
        pokemon.forEachIndexed { index, pokemon ->
            pokemon.storeCoordinates.set(StoreCoordinates(this, BottomlessPosition(index)))
            pokemon.getChangeObservable().pipe(
                stopAfter { it.storeCoordinates.get()?.store != this }
            ).subscribe { storeChangeObservable.emit(Unit) }
        }
    }

    override fun saveToNBT(nbt: NbtCompound): NbtCompound {
        pokemon.forEachIndexed { index, pokemon -> nbt.put(DataKeys.STORE_SLOT + index, pokemon.saveToNBT(NbtCompound())) }
        return nbt
    }

    override fun loadFromNBT(nbt: NbtCompound): BottomlessStore {
        var i = -1
        while (nbt.contains(DataKeys.STORE_SLOT + ++i)) {
            pokemon.add(Pokemon().loadFromNBT(nbt.getCompound(DataKeys.STORE_SLOT + i)))
        }
        return this
    }

    override fun saveToJSON(json: JsonObject): JsonObject {
        pokemon.forEachIndexed { index, pokemon -> json.add(DataKeys.STORE_SLOT + index, pokemon.saveToJSON(JsonObject())) }
        return json
    }

    override fun loadFromJSON(json: JsonObject): BottomlessStore {
        var i = -1
        while (json.has(DataKeys.STORE_SLOT + ++i)) {
            pokemon.add(Pokemon().loadFromJSON(json.getAsJsonObject(DataKeys.STORE_SLOT + i)))
        }
        return this
    }

    override fun loadPositionFromNBT(nbt: NbtCompound): StoreCoordinates<BottomlessPosition> {
        val slot = nbt.getByte(DataKeys.STORE_SLOT).toInt()
        return StoreCoordinates(this, BottomlessPosition(slot))
    }

    override fun savePositionToNBT(position: BottomlessPosition, nbt: NbtCompound) {
        nbt.putByte(DataKeys.STORE_SLOT, position.currentIndex.toByte())
    }

    override fun getAnyChangeObservable() = storeChangeObservable

    override fun setAtPosition(position: BottomlessPosition, pokemon: Pokemon?) {
        if (position.currentIndex == this.pokemon.size && pokemon != null) {
            this.pokemon.add(pokemon)
            storeChangeObservable.emit(Unit)
        } else if (position.currentIndex in 0 until this.pokemon.size) {
            if (pokemon != null) {
                this.pokemon.add(position.currentIndex, pokemon)
            } else {
                this.pokemon.removeAt(position.currentIndex)
            }
            storeChangeObservable.emit(Unit)
        }
    }
}