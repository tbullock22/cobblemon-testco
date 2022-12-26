/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.abilities

import com.cobblemon.mod.common.util.lang
import com.google.gson.JsonObject
import net.minecraft.nbt.NbtCompound
import net.minecraft.text.MutableText

/**
 * This represents the base of an Ability.
 * To build an Ability you MUST use its template.
 *
 * @param name: The English name used to load / find it (spaces -> _)
 */
class AbilityTemplate(
    val name: String = "",
    var builder: (AbilityTemplate) -> Ability = { Ability(it) },
    val displayName: String = "cobblemon.ability.$name",
    val description: String = "cobblemon.ability.$name.desc"
) {
    /**
     * Returns the Ability or if applicable the extension connected to this template
     */
    fun create() = builder(this)


    /**
     * Returns the Ability and loads the given NBT Tag into it.
     *
     * Ability extensions need to write and read their needed data from here.
     */
    fun create(nbt: NbtCompound) = builder(this).loadFromNBT(nbt)

    /**
     * Returns the Ability and loads the given JSON object into it.
     *
     * Ability extensions need to write and read their needed data from here.
     */
    fun create(json: JsonObject) = builder(this).loadFromJSON(json)
}