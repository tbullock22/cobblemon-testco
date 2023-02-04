/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item

import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.util.cobblemonResource
import dev.architectury.registry.CreativeTabRegistry.create
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack

object CobblemonItemGroups {
    val POKE_BALL_GROUP: ItemGroup = create(cobblemonResource("pokeball")) { ItemStack(CobblemonItems.POKE_BALL.get()) }
    val EVOLUTION_ITEM_GROUP: ItemGroup = create(cobblemonResource("evolution_item")) { ItemStack(CobblemonItems.BLACK_AUGURITE.get()) }
    val MEDICINE_ITEM_GROUP: ItemGroup = create(cobblemonResource("medicine")) { ItemStack(CobblemonItems.RARE_CANDY.get()) }
    val HELD_ITEM_GROUP: ItemGroup = create(cobblemonResource("held_item")) { ItemStack(CobblemonItems.EXP_SHARE.get()) }
}