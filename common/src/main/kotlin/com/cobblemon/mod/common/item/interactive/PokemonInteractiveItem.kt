/*
 * Copyright (C) 2023 Cobblemon Contributors
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
 *
 * @param accepted The [Ownership] variants for this interaction to fire [PokemonInteractiveItem.processInteraction].
 */
abstract class PokemonInteractiveItem(properties: Settings, vararg accepted: Ownership) : InteractiveItem<PokemonEntity>(properties) {

    private val accepted = accepted.toSet()

    final override fun onInteraction(player: ServerPlayerEntity, entity: PokemonEntity, stack: ItemStack): Boolean {
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
    abstract fun processInteraction(player: ServerPlayerEntity, entity: PokemonEntity, stack: ItemStack): Boolean

    /**
     * Decreases the stack size by a given amount.
     * The stack size should be validated beforehand.
     * If the [player] is in creative mode the decrement won't be performed.
     *
     * @param player The [ServerPlayerEntity] that caused the interaction, this is used to check for creative mode.
     * @param stack The [ItemStack] being mutated.
     * @param amount The amount to deduct from the stack, default is 1.
     */
    protected fun consumeItem(player: ServerPlayerEntity, stack: ItemStack, amount: Int = 1) {
        if (!player.isCreative) {
            stack.decrement(amount)
        }
    }

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