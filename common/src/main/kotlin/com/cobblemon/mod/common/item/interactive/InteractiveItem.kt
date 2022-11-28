/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.interactive

import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity

/**
 * An item that will affect an [Entity].
 *
 * @param T The type of the [Entity] this item will affect.
 */
interface InteractiveItem<T : Entity> {

    /**
     * Fired when a [ServerPlayerEntity] interacts with the target entity.
     *
     * @param player The [ServerPlayerEntity] interacting with the [entity].
     * @param entity The [Entity] of type [T] being interacted.
     * @param stack The [ItemStack] used in this interaction. [ItemStack.getItem] will always be of the same type as this [InteractiveItem].
     * @return true if the interaction was successful and no further interactions should be processed
     */
    fun onInteraction(player: ServerPlayerEntity, entity: T, stack: ItemStack): Boolean

    /**
     * Decreases the stack size by a given amount.
     * The stack size should be validated beforehand.
     * If the [player] is in creative mode the decrement won't be performed.
     *
     * @param player The [ServerPlayerEntity] that caused the interaction, this is used to check for creative mode.
     * @param stack The [ItemStack] being mutated.
     * @param amount The amount to deduct from the stack, default is 1.
     */
    fun consumeItem(player: ServerPlayerEntity, stack: ItemStack, amount: Int = 1) {
        if (!player.isCreative) {
            stack.decrement(amount)
        }
    }

}