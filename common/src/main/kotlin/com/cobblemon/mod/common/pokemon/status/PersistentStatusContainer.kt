/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.status

import com.cobblemon.mod.common.api.pokemon.status.Statuses
import com.cobblemon.mod.common.util.DataKeys
import com.google.gson.JsonObject
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Identifier

/**
 * Container that stores all status details
 *
 * @author Deltric
 */
class PersistentStatusContainer(
    val status: PersistentStatus,
    var secondsLeft: Int = 0
) {
    fun isExpired(): Boolean {
        return this.secondsLeft <= 0
    }

    fun tickTimer() {
        this.secondsLeft--
    }

    fun saveToNBT(nbt: NbtCompound): NbtCompound {
        nbt.putString(DataKeys.POKEMON_STATUS_NAME, status.name.toString())
        nbt.putInt(DataKeys.POKEMON_STATUS_TIMER, secondsLeft)
        return nbt
    }

    fun saveToJSON(json: JsonObject): JsonObject {
        json.addProperty(DataKeys.POKEMON_STATUS_NAME, status.name.toString())
        json.addProperty(DataKeys.POKEMON_STATUS_TIMER, secondsLeft)
        return json
    }

    companion object {
        fun loadFromNBT(nbt: NbtCompound): PersistentStatusContainer? {
            val statusId = nbt.getString(DataKeys.POKEMON_STATUS_NAME)
            val activeSeconds = nbt.getInt(DataKeys.POKEMON_STATUS_TIMER)

            // Missing status id
            if (statusId.isEmpty()) {
                return null
            }

            // Return null if status doesn't exist
            val status = Statuses.getStatus(Identifier(statusId)) ?: return null
            // Return null if not a persistent status
            if (status !is PersistentStatus) return null
            return PersistentStatusContainer(status, activeSeconds)
        }

        fun loadFromJSON(json: JsonObject): PersistentStatusContainer? {
            val statusId = json.get(DataKeys.POKEMON_STATUS_NAME).asString
            val activeSeconds = json.get(DataKeys.POKEMON_STATUS_TIMER).asInt

            // Missing status id
            if (statusId.isEmpty()) {
                return null
            }

            // Return null if status doesn't exist
            val status = Statuses.getStatus(Identifier(statusId)) ?: return null
            // Return null if not a persistent status
            if (status !is PersistentStatus) return null
            return PersistentStatusContainer(status, activeSeconds)
        }
    }
}