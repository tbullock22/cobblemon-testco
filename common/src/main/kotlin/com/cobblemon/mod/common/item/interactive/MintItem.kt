/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.interactive

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Nature
import com.cobblemon.mod.common.util.asTranslated
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity

class MintItem(val nature: Nature) : PokemonInteractiveItem(Settings().group(ItemGroup.MISC), Ownership.OWNER) {

    override fun processInteraction(player: ServerPlayerEntity, entity: PokemonEntity, stack: ItemStack): Boolean {
        entity.pokemon.mintedNature = nature
        player.sendMessage("cobblemon.mint.interact".asTranslated(entity.pokemon.displayName, stack.name))
        consumeItem(player, stack)
        return true
    }

}