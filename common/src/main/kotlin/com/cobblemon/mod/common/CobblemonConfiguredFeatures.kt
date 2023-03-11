/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common

import net.minecraft.util.registry.RegistryEntry
import net.minecraft.world.gen.feature.ConfiguredFeature
import net.minecraft.world.gen.feature.ConfiguredFeatures
import net.minecraft.world.gen.feature.SingleStateFeatureConfig

object CobblemonConfiguredFeatures {
    lateinit var BLACK_APRICORN_TREE: RegistryEntry<ConfiguredFeature<SingleStateFeatureConfig, *>>
    lateinit var BLUE_APRICORN_TREE: RegistryEntry<ConfiguredFeature<SingleStateFeatureConfig, *>>
    lateinit var GREEN_APRICORN_TREE: RegistryEntry<ConfiguredFeature<SingleStateFeatureConfig, *>>
    lateinit var PINK_APRICORN_TREE: RegistryEntry<ConfiguredFeature<SingleStateFeatureConfig, *>>
    lateinit var RED_APRICORN_TREE: RegistryEntry<ConfiguredFeature<SingleStateFeatureConfig, *>>
    lateinit var WHITE_APRICORN_TREE: RegistryEntry<ConfiguredFeature<SingleStateFeatureConfig, *>>
    lateinit var YELLOW_APRICORN_TREE: RegistryEntry<ConfiguredFeature<SingleStateFeatureConfig, *>>

    fun register() {
        BLACK_APRICORN_TREE = ConfiguredFeatures.register("black_apricorn_tree", CobblemonFeatures.APRICORN_TREE_FEATURE.get(), SingleStateFeatureConfig(
            CobblemonBlocks.BLACK_APRICORN.get().defaultState))
        BLUE_APRICORN_TREE = ConfiguredFeatures.register("blue_apricorn_tree", CobblemonFeatures.APRICORN_TREE_FEATURE.get(), SingleStateFeatureConfig(
            CobblemonBlocks.BLUE_APRICORN.get().defaultState))
        GREEN_APRICORN_TREE = ConfiguredFeatures.register("green_apricorn_tree", CobblemonFeatures.APRICORN_TREE_FEATURE.get(), SingleStateFeatureConfig(
            CobblemonBlocks.GREEN_APRICORN.get().defaultState))
        PINK_APRICORN_TREE = ConfiguredFeatures.register("pink_apricorn_tree", CobblemonFeatures.APRICORN_TREE_FEATURE.get(), SingleStateFeatureConfig(
            CobblemonBlocks.PINK_APRICORN.get().defaultState))
        RED_APRICORN_TREE = ConfiguredFeatures.register("red_apricorn_tree", CobblemonFeatures.APRICORN_TREE_FEATURE.get(), SingleStateFeatureConfig(
            CobblemonBlocks.RED_APRICORN.get().defaultState))
        WHITE_APRICORN_TREE = ConfiguredFeatures.register("white_apricorn_tree", CobblemonFeatures.APRICORN_TREE_FEATURE.get(), SingleStateFeatureConfig(
            CobblemonBlocks.WHITE_APRICORN.get().defaultState))
        YELLOW_APRICORN_TREE = ConfiguredFeatures.register("yellow_apricorn_tree", CobblemonFeatures.APRICORN_TREE_FEATURE.get(), SingleStateFeatureConfig(
            CobblemonBlocks.YELLOW_APRICORN.get().defaultState))
    }
}