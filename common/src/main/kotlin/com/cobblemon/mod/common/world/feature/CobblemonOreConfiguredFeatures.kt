/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.world.feature

import com.cobblemon.mod.common.CobblemonBlocks
import com.cobblemon.mod.common.api.tags.CobblemonBlockTags
import com.cobblemon.mod.common.util.cobblemonResource
import dev.architectury.registry.registries.RegistrySupplier
import net.minecraft.block.OreBlock
import net.minecraft.structure.rule.RuleTest
import net.minecraft.structure.rule.TagMatchRuleTest
import net.minecraft.world.gen.feature.ConfiguredFeatures
import net.minecraft.world.gen.feature.Feature
import net.minecraft.world.gen.feature.OreConfiguredFeatures
import net.minecraft.world.gen.feature.OreFeatureConfig

object CobblemonOreConfiguredFeatures {

    // Rule Tests
    val DRIPSTONE_ORE_REPLACEABLES = TagMatchRuleTest(CobblemonBlockTags.DRIPSTONE_REPLACEABLES)
    
    // Targets
    val DAWN_STONE_ORES = this.createTargets(CobblemonBlocks.DAWN_STONE_ORE, CobblemonBlocks.DEEPSLATE_DAWN_STONE_ORE)
    val DUSK_STONE_ORES = this.createTargets(CobblemonBlocks.DUSK_STONE_ORE, CobblemonBlocks.DEEPSLATE_DUSK_STONE_ORE)
    val FIRE_STONE_ORES = this.createTargets(CobblemonBlocks.FIRE_STONE_ORE, CobblemonBlocks.DEEPSLATE_FIRE_STONE_ORE)
    val ICE_STONE_ORES = this.createTargets(CobblemonBlocks.ICE_STONE_ORE, CobblemonBlocks.DEEPSLATE_ICE_STONE_ORE)
    val LEAF_STONE_ORES = this.createTargets(CobblemonBlocks.LEAF_STONE_ORE, CobblemonBlocks.DEEPSLATE_LEAF_STONE_ORE)
    val MOON_STONE_ORES = this.createTargets(CobblemonBlocks.MOON_STONE_ORE, CobblemonBlocks.DEEPSLATE_MOON_STONE_ORE)
    val SHINY_STONE_ORES = this.createTargets(CobblemonBlocks.SHINY_STONE_ORE, CobblemonBlocks.DEEPSLATE_SHINY_STONE_ORE)
    val SUN_STONE_ORES = this.createTargets(CobblemonBlocks.SUN_STONE_ORE, CobblemonBlocks.DEEPSLATE_SUN_STONE_ORE)
    val THUNDER_STONE_ORES = this.createTargets(CobblemonBlocks.THUNDER_STONE_ORE, CobblemonBlocks.DEEPSLATE_THUNDER_STONE_ORE)
    val WATER_STONE_ORES = this.createTargets(CobblemonBlocks.WATER_STONE_ORE, CobblemonBlocks.DEEPSLATE_WATER_STONE_ORE)

    // Features
    val ORE_DAWN_STONE = this.createFeature("ore_dawn_stone", DAWN_STONE_ORES)
    val ORE_DUSK_STONE = this.createFeature("ore_dusk_stone", DUSK_STONE_ORES)
    val ORE_FIRE_STONE = this.createFeature("ore_fire_stone", FIRE_STONE_ORES)
    val ORE_ICE_STONE = this.createFeature("ore_ice_stone", ICE_STONE_ORES)
    val ORE_LEAF_STONE = this.createFeature("ore_leaf_stone", LEAF_STONE_ORES)
    val ORE_MOON_STONE = this.createFeature("ore_moon_stone", MOON_STONE_ORES)
    // We want this to be a separate configured feature to avoid generating too much moon stone
    val ORE_MOON_STONE_DRIPSTONE = this.createFeature("ore_moon_stone_dripstone", listOf(this.createTarget(CobblemonBlocks.DRIPSTONE_MOON_STONE_ORE, DRIPSTONE_ORE_REPLACEABLES)))
    val ORE_SHINY_STONE = this.createFeature("ore_shiny_stone", SHINY_STONE_ORES)
    val ORE_SUN_STONE = this.createFeature("ore_sun_stone", SUN_STONE_ORES)
    val ORE_THUNDER_STONE = this.createFeature("ore_thunder_stone", THUNDER_STONE_ORES)
    val ORE_WATER_STONE = this.createFeature("ore_water_stone", WATER_STONE_ORES)


    private fun createTargets(stoneOre: RegistrySupplier<OreBlock>, deepslateOre: RegistrySupplier<OreBlock>) = listOf(
        this.createTarget(stoneOre, OreConfiguredFeatures.STONE_ORE_REPLACEABLES),
        this.createTarget(deepslateOre, OreConfiguredFeatures.DEEPSLATE_ORE_REPLACEABLES)
    )

    private fun createTarget(ore: RegistrySupplier<OreBlock>, ruleTest: RuleTest) = OreFeatureConfig.createTarget(ruleTest, ore.get().defaultState)

    private fun createFeature(name: String, targets: List<OreFeatureConfig.Target>) = ConfiguredFeatures.register(cobblemonResource(name).toString(), Feature.ORE, OreFeatureConfig(targets, 3))

}