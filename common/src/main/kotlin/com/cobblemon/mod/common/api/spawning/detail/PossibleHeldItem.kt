/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.detail

import com.cobblemon.mod.common.Cobblemon.LOGGER
import com.cobblemon.mod.common.api.spawning.context.SpawningContext
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.tag.TagKey
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

class PossibleHeldItem(
    val item: String,
    val nbt: NbtCompound? = null,
    val percentage: Double = 100.0
) {
    fun createStack(ctx: SpawningContext): ItemStack? {
        val itemRegistry = ctx.world.registryManager.get(Registry.ITEM_KEY)
        val item = if (item.startsWith("#")) {
            val tag = TagKey.of(Registry.ITEM_KEY, Identifier(item.substring(1)))

            val opt = itemRegistry.getEntryList(tag)
            if (opt.isPresent && opt.get().size() > 0) {
                val entryList = opt.get()
                entryList.getRandom(ctx.world.random).get().value()
            } else {
                LOGGER.error("Unable to find matching spawn held items for tag: $item")
                null
            }
        } else {
            itemRegistry.get(Identifier(item))?.takeIf { it != Items.AIR }
        } ?: return run {
            LOGGER.error("Unable to find matching spawn held item for ID: $item")
            null
        }

        val stack = ItemStack(item, 1)

        if (nbt != null) {
            stack.nbt = nbt
        }

        return stack
    }
}