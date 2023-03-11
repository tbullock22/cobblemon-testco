/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common

import com.cobblemon.mod.common.util.cobblemonResource
import dev.architectury.registry.level.biome.BiomeModifications
import net.minecraft.util.registry.RegistryEntry
import net.minecraft.util.registry.RegistryEntryList
import net.minecraft.world.gen.GenerationStep
import net.minecraft.world.gen.feature.ConfiguredFeatures
import net.minecraft.world.gen.feature.Feature
import net.minecraft.world.gen.feature.PlacedFeature
import net.minecraft.world.gen.feature.PlacedFeatures
import net.minecraft.world.gen.feature.SimpleRandomFeatureConfig
import net.minecraft.world.gen.feature.VegetationPlacedFeatures
import net.minecraft.world.gen.placementmodifier.RarityFilterPlacementModifier

object CobblemonPlacements {

    lateinit var BLACK_APRICORN_TREE: RegistryEntry<PlacedFeature>
    lateinit var BLUE_APRICORN_TREE: RegistryEntry<PlacedFeature>
    lateinit var GREEN_APRICORN_TREE: RegistryEntry<PlacedFeature>
    lateinit var PINK_APRICORN_TREE: RegistryEntry<PlacedFeature>
    lateinit var RED_APRICORN_TREE: RegistryEntry<PlacedFeature>
    lateinit var WHITE_APRICORN_TREE: RegistryEntry<PlacedFeature>
    lateinit var YELLOW_APRICORN_TREE: RegistryEntry<PlacedFeature>

    lateinit var APRICORN_TREES: RegistryEntry<PlacedFeature>

    fun register() {
        BLACK_APRICORN_TREE = PlacedFeatures.register(cobblemonResource("black_apricorn_tree").toString(), CobblemonConfiguredFeatures.BLACK_APRICORN_TREE, PlacedFeatures.wouldSurvive(
            CobblemonBlocks.BLACK_APRICORN_SAPLING.get()))
        BLUE_APRICORN_TREE = PlacedFeatures.register(cobblemonResource("blue_apricorn_tree").toString(), CobblemonConfiguredFeatures.BLUE_APRICORN_TREE, PlacedFeatures.wouldSurvive(
            CobblemonBlocks.BLUE_APRICORN_SAPLING.get()))
        GREEN_APRICORN_TREE = PlacedFeatures.register(cobblemonResource("green_apricorn_tree").toString(), CobblemonConfiguredFeatures.GREEN_APRICORN_TREE, PlacedFeatures.wouldSurvive(
            CobblemonBlocks.GREEN_APRICORN_SAPLING.get()))
        PINK_APRICORN_TREE = PlacedFeatures.register(cobblemonResource("pink_apricorn_tree").toString(), CobblemonConfiguredFeatures.PINK_APRICORN_TREE, PlacedFeatures.wouldSurvive(
            CobblemonBlocks.PINK_APRICORN_SAPLING.get()))
        RED_APRICORN_TREE = PlacedFeatures.register(cobblemonResource("red_apricorn_tree").toString(), CobblemonConfiguredFeatures.RED_APRICORN_TREE, PlacedFeatures.wouldSurvive(
            CobblemonBlocks.RED_APRICORN_SAPLING.get()))
        WHITE_APRICORN_TREE = PlacedFeatures.register(cobblemonResource("white_apricorn_tree").toString(), CobblemonConfiguredFeatures.WHITE_APRICORN_TREE, PlacedFeatures.wouldSurvive(
            CobblemonBlocks.WHITE_APRICORN_SAPLING.get()))
        YELLOW_APRICORN_TREE = PlacedFeatures.register(cobblemonResource("yellow_apricorn_tree").toString(), CobblemonConfiguredFeatures.YELLOW_APRICORN_TREE, PlacedFeatures.wouldSurvive(
            CobblemonBlocks.YELLOW_APRICORN_SAPLING.get()))

        val apricornTreeVariety = ConfiguredFeatures.register(
            cobblemonResource("apricorn_trees").toString(), Feature.SIMPLE_RANDOM_SELECTOR, SimpleRandomFeatureConfig(
                RegistryEntryList.of(
                    BLACK_APRICORN_TREE,
                    BLUE_APRICORN_TREE,
                    GREEN_APRICORN_TREE,
                    PINK_APRICORN_TREE,
                    RED_APRICORN_TREE,
                    WHITE_APRICORN_TREE,
                    YELLOW_APRICORN_TREE
                )
            )
        )

        APRICORN_TREES = PlacedFeatures.register(cobblemonResource("apricorn_trees").toString(), apricornTreeVariety, VegetationPlacedFeatures.modifiers(RarityFilterPlacementModifier.of(8)))

        BiomeModifications.addProperties({ context -> true}, { context, properties ->
            properties.generationProperties.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, APRICORN_TREES)
        })
    }

}