/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage.player.adapter

import com.cobblemon.mod.common.api.storage.player.PlayerDataExtension
import com.cobblemon.mod.common.api.storage.player.PlayerDataExtensionRegistry
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

object PlayerDataExtensionAdapter: JsonSerializer<PlayerDataExtension>, JsonDeserializer<PlayerDataExtension> {
    override fun serialize(
        src: PlayerDataExtension,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        return src.serialize()
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): PlayerDataExtension {
        val jObject = json.asJsonObject
        val name = jObject.get(PlayerDataExtension.NAME_KEY)
            ?: throw IllegalStateException("PlayerDataExtension without name")
        val extension = PlayerDataExtensionRegistry.getOrException(name.asString)
        return extension.getDeclaredConstructor().newInstance().deserialize(jObject)
    }
}