/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.interactive

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.item.interactive.PokemonInteractiveItem.Ownership
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity

/**
 * An [InteractiveItem] targeting [PokemonEntity]s.
 */
interface PokemonInteractiveItem : InteractiveItem<PokemonEntity> {

    /**
     * The accepted [Ownership] for the Pokémon entity in order for the interaction to fire.
     */
    val accepted: Set<Ownership>

    override fun onInteraction(player: ServerPlayerEntity, entity: PokemonEntity, stack: ItemStack): Boolean {
        val pokemon = entity.pokemon
        val storeCoordinates = pokemon.storeCoordinates.get()
        val ownership = when {
            storeCoordinates == null -> Ownership.WILD
            storeCoordinates.store.uuid == player.uuid -> Ownership.OWNER
            else -> Ownership.OWNED_ANOTHER
        }
        return if (ownership in accepted) {
            this.processInteraction(player, entity, stack)
        } else {
            false
        }
    }

    /**
     * Fired after [InteractiveItem.onInteraction] the [Ownership] is checked if contained in [accepted].
     *
     * @param player The [ServerPlayerEntity] interacting with the [entity].
     * @param entity The [PokemonEntity] being interacted with.
     * @param stack The [ItemStack] used in this interaction. [ItemStack.getItem] will always be of the same type as this [InteractiveItem].
     * @return true if the interaction was successful and no further interactions should be processed.
     */
    fun processInteraction(player: ServerPlayerEntity, entity: PokemonEntity, stack: ItemStack): Boolean

    /**
     * Represents the ownership status of a Pokemon relative to a Player.
     *
     * @author Licious
     * @since March 24th, 2022
     */
    enum class Ownership {

        /**
         * When the player owns the Pokemon.
         */
        OWNER,

        /**
         * When the Pokemon is owned by another entity.
         */
        OWNED_ANOTHER,

        /**
         * When the Pokemon has no owner.
         */
        WILD

    }

}