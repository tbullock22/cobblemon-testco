/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block.grower

import com.cobblemon.mod.common.CobblemonConfiguredFeatures
import com.cobblemon.mod.common.api.apricorn.Apricorn
import net.minecraft.block.sapling.SaplingGenerator
import net.minecraft.util.math.random.Random
class ApricornTreeGrower(private val apricorn: Apricorn) : SaplingGenerator() {
    override fun getTreeFeature(random: Random, bl: Boolean) = when (this.apricorn) {
        Apricorn.BLACK -> CobblemonConfiguredFeatures.BLACK_APRICORN_TREE
        Apricorn.BLUE -> CobblemonConfiguredFeatures.BLUE_APRICORN_TREE
        Apricorn.GREEN -> CobblemonConfiguredFeatures.GREEN_APRICORN_TREE
        Apricorn.PINK -> CobblemonConfiguredFeatures.PINK_APRICORN_TREE
        Apricorn.RED -> CobblemonConfiguredFeatures.RED_APRICORN_TREE
        Apricorn.WHITE -> CobblemonConfiguredFeatures.WHITE_APRICORN_TREE
        Apricorn.YELLOW -> CobblemonConfiguredFeatures.YELLOW_APRICORN_TREE
    }
}