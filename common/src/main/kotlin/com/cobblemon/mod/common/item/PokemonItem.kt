/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item

import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.RenderablePokemon
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.util.DataKeys
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.InvalidIdentifierException

class PokemonItem : CobblemonItem(Settings().maxCount(1)) {

    override fun getName(stack: ItemStack): Text = this.species(stack)?.translatedName ?: super.getName(stack)

    fun asPokemon(stack: ItemStack): Pokemon? {
        val species = this.species(stack) ?: return null
        val aspects = this.aspects(stack) ?: setOf()
        return Pokemon().apply {
            this.species = species
            this.aspects = aspects
        }
    }

    fun asRenderablePokemon(stack: ItemStack): RenderablePokemon? = this.asPokemon(stack)?.asRenderablePokemon()

    private fun species(stack: ItemStack): Species? {
        val nbt = stack.nbt ?: return null
        if (!nbt.contains(DataKeys.POKEMON_ITEM_SPECIES)) {
            return null
        }
        return try {
            val identifier = Identifier(nbt.getString(DataKeys.POKEMON_ITEM_SPECIES))
            PokemonSpecies.getByIdentifier(identifier)
        } catch (_: InvalidIdentifierException) {
            null
        }
    }

    private fun aspects(stack: ItemStack): Set<String>? {
        val nbt = stack.nbt ?: return null
        if (!nbt.contains(DataKeys.POKEMON_ITEM_ASPECTS)) {
            return null
        }
        return nbt.getList(DataKeys.POKEMON_ITEM_ASPECTS, NbtElement.STRING_TYPE.toInt())
            .filterIsInstance<NbtString>()
            .map { it.asString() }
            .toSet()
    }

    companion object {

        fun from(pokemon: Pokemon, count: Int = 1): ItemStack = from(pokemon.species, pokemon.aspects, count)

        fun from(properties: PokemonProperties, count: Int = 1): ItemStack = from(properties.create(), count)

        fun from(species: Species, vararg aspects: String, count: Int = 1): ItemStack = from(species, aspects.toSet(), count)

        fun from(species: Species, aspects: Set<String>, count: Int = 1): ItemStack {
            val stack = ItemStack(CobblemonItems.POKEMON_MODEL.get(), count)
            stack.orCreateNbt.apply {
                putString(DataKeys.POKEMON_ITEM_SPECIES, species.resourceIdentifier.toString())
                val list = NbtList()
                aspects.forEach { aspect ->
                    list.add(NbtString.of(aspect))
                }
                put(DataKeys.POKEMON_ITEM_ASPECTS, list)
            }
            return stack
        }

    }

}