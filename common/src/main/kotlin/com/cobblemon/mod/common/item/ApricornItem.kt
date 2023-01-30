/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item

import com.cobblemon.mod.common.block.ApricornBlock
import net.minecraft.block.BlockState
import net.minecraft.block.ComposterBlock
import net.minecraft.item.AliasedBlockItem
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemPlacementContext

class ApricornItem(block: ApricornBlock) : AliasedBlockItem(block, Settings().group(ItemGroup.MISC)) {

    init {
        // 65% to raise composter level
        ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE[this] = .65F
    }

    override fun canPlace(context: ItemPlacementContext, state: BlockState) = context.player?.isCreative != false && super.canPlace(context, state)

}