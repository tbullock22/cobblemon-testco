/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.abilities

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.data.DataRegistry
import com.cobblemon.mod.common.api.data.JsonDataRegistry
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.battles.runner.GraalShowdown
import com.cobblemon.mod.common.net.messages.client.data.AbilityRegistrySyncPacket
import com.cobblemon.mod.common.pokemon.abilities.HiddenAbilityType
import com.cobblemon.mod.common.util.asTranslated
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import net.minecraft.resource.ResourceManager
import java.lang.reflect.Type
import net.minecraft.resource.ResourceType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.MutableText
import net.minecraft.util.Identifier

/**
 * Registry for all known Abilities
 */
object Abilities : DataRegistry {

    override val id = cobblemonResource("abilities")
    override val type = ResourceType.SERVER_DATA
    override val observable = SimpleObservable<Abilities>()

    val DUMMY = AbilityTemplate(name = "dummy")

    private val abilityMap = mutableMapOf<String, AbilityTemplate>()

    override fun reload(manager: ResourceManager) {
        PotentialAbility.types.clear()
        PotentialAbility.types.add(CommonAbilityType)
        PotentialAbility.types.add(HiddenAbilityType)
        this.abilityMap.clear()
        val script = """
            PokemonShowdown.Dex.mod("${Cobblemon.MODID}")
              .abilities.all()
              .map(ability => ability.id);
        """.trimIndent()
        val arrayResult = GraalShowdown.context.eval("js", script)
        for (i in 0 until arrayResult.arraySize) {
            val id = arrayResult.getArrayElement(i).asString()
            val ability = AbilityTemplate(id)
            this.register(ability)
        }
        Cobblemon.LOGGER.info("Loaded {} abilities", this.abilityMap.size)
        this.observable.emit(this)
    }

    override fun sync(player: ServerPlayerEntity) {
        AbilityRegistrySyncPacket(all()).sendToPlayer(player)
    }

    fun register(ability: AbilityTemplate): AbilityTemplate {
        this.abilityMap[ability.name.lowercase()] = ability
        return ability
    }

    fun all() = this.abilityMap.values.toList()
    fun first() = this.abilityMap.values.first()
    fun get(name: String) = abilityMap[name.lowercase()]
    fun getOrException(name: String) = get(name) ?: throw IllegalArgumentException("Unable to find ability of name: $name")
    fun count() = this.abilityMap.size

    internal fun receiveSyncPacket(abilities: Collection<AbilityTemplate>) {
        this.abilityMap.clear()
        abilities.forEach { ability -> this.register(ability) }
    }

}