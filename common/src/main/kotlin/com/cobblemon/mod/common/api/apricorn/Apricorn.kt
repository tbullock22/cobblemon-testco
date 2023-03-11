/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.apricorn

import com.cobblemon.mod.common.CobblemonBlocks
import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.item.ApricornItem
import com.cobblemon.mod.common.block.ApricornBlock
import com.cobblemon.mod.common.block.ApricornSaplingBlock
import net.minecraft.block.MapColor
import net.minecraft.item.BlockItem

/**
 * Contains the different Apricorn variants and util methods to get their item, block, seedling and sapling form.
 *
 * @author Licious
 * @since October 29th, 2022
 */
enum class Apricorn {

    BLACK,
    BLUE,
    GREEN,
    PINK,
    RED,
    WHITE,
    YELLOW;

    fun item(): ApricornItem = when(this) {
        BLACK -> CobblemonItems.BLACK_APRICORN.get()
        BLUE -> CobblemonItems.BLUE_APRICORN.get()
        GREEN -> CobblemonItems.GREEN_APRICORN.get()
        PINK -> CobblemonItems.PINK_APRICORN.get()
        RED -> CobblemonItems.RED_APRICORN.get()
        WHITE -> CobblemonItems.WHITE_APRICORN.get()
        YELLOW -> CobblemonItems.YELLOW_APRICORN.get()
    }

    fun seed(): BlockItem = when(this) {
        BLACK -> CobblemonItems.BLACK_APRICORN_SEED.get()
        BLUE -> CobblemonItems.BLUE_APRICORN_SEED.get()
        GREEN -> CobblemonItems.GREEN_APRICORN_SEED.get()
        PINK -> CobblemonItems.PINK_APRICORN_SEED.get()
        RED -> CobblemonItems.RED_APRICORN_SEED.get()
        WHITE -> CobblemonItems.WHITE_APRICORN_SEED.get()
        YELLOW -> CobblemonItems.YELLOW_APRICORN_SEED.get()
    }

    fun block(): ApricornBlock = when(this) {
        BLACK -> CobblemonBlocks.BLACK_APRICORN.get()
        BLUE -> CobblemonBlocks.BLUE_APRICORN.get()
        GREEN -> CobblemonBlocks.GREEN_APRICORN.get()
        PINK -> CobblemonBlocks.PINK_APRICORN.get()
        RED -> CobblemonBlocks.RED_APRICORN.get()
        WHITE -> CobblemonBlocks.WHITE_APRICORN.get()
        YELLOW -> CobblemonBlocks.YELLOW_APRICORN.get()
    }

    fun sapling(): ApricornSaplingBlock = when(this) {
        BLACK -> CobblemonBlocks.BLACK_APRICORN_SAPLING.get()
        BLUE -> CobblemonBlocks.BLUE_APRICORN_SAPLING.get()
        GREEN -> CobblemonBlocks.GREEN_APRICORN_SAPLING.get()
        PINK -> CobblemonBlocks.PINK_APRICORN_SAPLING.get()
        RED -> CobblemonBlocks.RED_APRICORN_SAPLING.get()
        WHITE -> CobblemonBlocks.WHITE_APRICORN_SAPLING.get()
        YELLOW -> CobblemonBlocks.YELLOW_APRICORN_SAPLING.get()
    }

    fun mapColor(): MapColor = when(this) {
        BLACK -> MapColor.BLACK
        BLUE ->  MapColor.BLUE
        GREEN ->  MapColor.GREEN
        PINK ->  MapColor.PINK
        RED ->  MapColor.RED
        WHITE ->  MapColor.WHITE
        YELLOW ->  MapColor.YELLOW
    }

}