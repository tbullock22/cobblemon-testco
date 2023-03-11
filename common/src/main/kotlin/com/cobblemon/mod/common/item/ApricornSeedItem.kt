/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item

import com.cobblemon.mod.common.block.ApricornSaplingBlock
import net.minecraft.block.ComposterBlock
import net.minecraft.item.AliasedBlockItem
import net.minecraft.item.ItemGroup

class ApricornSeedItem(block: ApricornSaplingBlock, group: ItemGroup) : AliasedBlockItem(block, Settings().group(group)) {

    init {
        // 65% to raise composter level
        ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE[this] = .65F
    }

}