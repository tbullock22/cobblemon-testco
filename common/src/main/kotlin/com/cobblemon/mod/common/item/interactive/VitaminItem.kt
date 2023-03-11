/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.interactive

import com.cobblemon.mod.common.api.pokemon.stats.Stat
import com.cobblemon.mod.common.api.text.gray
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.item.CobblemonItemGroups
import com.cobblemon.mod.common.util.asTranslated
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.world.World

class VitaminItem(
    val stat: Stat
) : PokemonInteractiveItem(Settings().group(CobblemonItemGroups.MEDICINE_ITEM_GROUP), Ownership.OWNER) {
    companion object {
        const val EV_YIELD = 10
    }

    override fun processInteraction(player: ServerPlayerEntity, entity: PokemonEntity, stack: ItemStack): Boolean {
        val pokemon = entity.pokemon
        val evsGained = pokemon.evs.add(stat, EV_YIELD)
        if (evsGained > 0) {
            this.consumeItem(player, stack)
            return true
        }
        return false
    }
}