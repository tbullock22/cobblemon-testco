/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.abilities

import com.cobblemon.mod.common.util.DataKeys
import com.google.gson.JsonObject
import net.minecraft.nbt.NbtCompound
import net.minecraft.text.MutableText

/**
 * Representing an Ability with all its attributes
 *
 * Can be extended to allow for custom attributes (be sure to overwrite the load and save methods)
 *
 * @author Qu
 * @since January 9th, 2022
 */
open class Ability internal constructor(var template: AbilityTemplate, var forced: Boolean) {

    val name: String
        get() = template.name

    val displayName: String
        get() = template.displayName

    val description: String
        get() = template.description

    open fun saveToNBT(nbt: NbtCompound): NbtCompound {
        nbt.putString(DataKeys.POKEMON_ABILITY_NAME, name)
        nbt.putBoolean(DataKeys.POKEMON_ABILITY_FORCED, forced)
        return nbt
    }

    open fun saveToJSON(json: JsonObject): JsonObject {
        json.addProperty(DataKeys.POKEMON_ABILITY_NAME, name)
        json.addProperty(DataKeys.POKEMON_ABILITY_FORCED, forced)
        return json
    }

    open fun loadFromNBT(nbt: NbtCompound): Ability {
        forced = nbt.getBoolean(DataKeys.POKEMON_ABILITY_FORCED)
        return this
    }
    open fun loadFromJSON(json: JsonObject): Ability {
        forced = json.get(DataKeys.POKEMON_ABILITY_FORCED)?.asBoolean ?: false
        return this
    }
}