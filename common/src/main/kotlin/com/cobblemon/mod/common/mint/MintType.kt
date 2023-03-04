/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mint

import com.cobblemon.mod.common.CobblemonBlocks
import com.cobblemon.mod.common.CobblemonItems
import net.minecraft.block.Block
import net.minecraft.item.Item

enum class MintType {
    RED,
    BLUE,
    CYAN,
    PINK,
    GREEN,
    WHITE;

    fun getSeed(): Item {
        return when (this) {
            RED -> CobblemonItems.RED_MINT_SEEDS.get()
            BLUE -> CobblemonItems.BLUE_MINT_SEEDS.get()
            CYAN -> CobblemonItems.CYAN_MINT_SEEDS.get()
            PINK -> CobblemonItems.PINK_MINT_SEEDS.get()
            GREEN -> CobblemonItems.GREEN_MINT_SEEDS.get()
            WHITE -> CobblemonItems.WHITE_MINT_SEEDS.get()
        }
    }

    fun getLeaf(): Item {
        return when (this) {
            RED -> CobblemonItems.RED_MINT_LEAF.get()
            BLUE -> CobblemonItems.BLUE_MINT_LEAF.get()
            CYAN -> CobblemonItems.CYAN_MINT_LEAF.get()
            PINK -> CobblemonItems.PINK_MINT_LEAF.get()
            GREEN -> CobblemonItems.GREEN_MINT_LEAF.get()
            WHITE -> CobblemonItems.WHITE_MINT_LEAF.get()
        }
    }

    fun getCropBlock(): Block {
        return when (this) {
            RED -> CobblemonBlocks.RED_MINT.get()
            BLUE -> CobblemonBlocks.BLUE_MINT.get()
            CYAN -> CobblemonBlocks.CYAN_MINT.get()
            PINK -> CobblemonBlocks.PINK_MINT.get()
            GREEN -> CobblemonBlocks.GREEN_MINT.get()
            WHITE -> CobblemonBlocks.WHITE_MINT.get()
        }
    }
}