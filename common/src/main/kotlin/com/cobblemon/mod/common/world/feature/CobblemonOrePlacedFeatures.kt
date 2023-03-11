/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.world.feature

import com.cobblemon.mod.common.api.tags.CobblemonBiomeTags
import com.cobblemon.mod.common.util.cobblemonResource
import dev.architectury.registry.level.biome.BiomeModifications
import net.minecraft.tag.TagKey
import net.minecraft.util.registry.RegistryEntry
import net.minecraft.world.biome.Biome
import net.minecraft.world.gen.GenerationStep
import net.minecraft.world.gen.YOffset
import net.minecraft.world.gen.feature.ConfiguredFeature
import net.minecraft.world.gen.feature.OreFeatureConfig
import net.minecraft.world.gen.feature.PlacedFeature
import net.minecraft.world.gen.feature.PlacedFeatures
import net.minecraft.world.gen.placementmodifier.BiomePlacementModifier
import net.minecraft.world.gen.placementmodifier.CountPlacementModifier
import net.minecraft.world.gen.placementmodifier.HeightRangePlacementModifier
import net.minecraft.world.gen.placementmodifier.PlacementModifier
import net.minecraft.world.gen.placementmodifier.SquarePlacementModifier

object CobblemonOrePlacedFeatures {

    private val UPPER_RANGE = HeightRangePlacementModifier.trapezoid(YOffset.fixed(64), YOffset.fixed(320))
    private val LOWER_RANGE = HeightRangePlacementModifier.trapezoid(YOffset.fixed(-64), YOffset.fixed(192))
    private val DRIPSTONE_RANGE = HeightRangePlacementModifier.uniform(YOffset.fixed(-64), YOffset.fixed(256))
    private const val NORMAL_PLACEMENT_AMOUNT = 8
    private const val RARE_PLACEMENT_AMOUNT = 4
    private val features = arrayListOf<FeatureHolder>()

    // Dawn Stone
    private val DAWN_STONES = this.createOreFeatures("ore_dawn_stone", CobblemonOreConfiguredFeatures.ORE_DAWN_STONE, CobblemonBiomeTags.HAS_DAWN_STONE_ORE, CobblemonBiomeTags.HAS_DAWN_STONE_ORE_RARE)
    val DAWN_STONE_UPPER = DAWN_STONES[0]
    val DAWN_STONE_LOWER = DAWN_STONES[1]
    val DAWN_STONE_UPPER_RARE = DAWN_STONES[2]
    val DAWN_STONE_LOWER_RARE = DAWN_STONES[3]

    // Dusk Stone
    private val DUSK_STONES = this.createOreFeatures("ore_dusk_stone", CobblemonOreConfiguredFeatures.ORE_DUSK_STONE, CobblemonBiomeTags.HAS_DUSK_STONE_ORE, CobblemonBiomeTags.HAS_DUSK_STONE_ORE_RARE)
    val DUSK_STONE_UPPER = DUSK_STONES[0]
    val DUSK_STONE_LOWER = DUSK_STONES[1]
    val DUSK_STONE_UPPER_RARE = DUSK_STONES[2]
    val DUSK_STONE_LOWER_RARE = DUSK_STONES[3]

    // Fire Stone
    private val FIRE_STONES = this.createOreFeatures("ore_fire_stone", CobblemonOreConfiguredFeatures.ORE_FIRE_STONE, CobblemonBiomeTags.HAS_FIRE_STONE_ORE, CobblemonBiomeTags.HAS_FIRE_STONE_ORE_RARE)
    val FIRE_STONE_UPPER = FIRE_STONES[0]
    val FIRE_STONE_LOWER = FIRE_STONES[1]
    val FIRE_STONE_UPPER_RARE = FIRE_STONES[2]
    val FIRE_STONE_LOWER_RARE = FIRE_STONES[3]

    // Ice Stone
    private val ICE_STONES = this.createOreFeatures("ore_ice_stone", CobblemonOreConfiguredFeatures.ORE_ICE_STONE, CobblemonBiomeTags.HAS_ICE_STONE_ORE, CobblemonBiomeTags.HAS_ICE_STONE_ORE_RARE)
    val ICE_STONE_UPPER = ICE_STONES[0]
    val ICE_STONE_LOWER = ICE_STONES[1]
    val ICE_STONE_UPPER_RARE = ICE_STONES[2]
    val ICE_STONE_LOWER_RARE = ICE_STONES[3]

    // Leaf Stone
    private val LEAF_STONES = this.createOreFeatures("ore_leaf_stone", CobblemonOreConfiguredFeatures.ORE_LEAF_STONE, CobblemonBiomeTags.HAS_LEAF_STONE_ORE, CobblemonBiomeTags.HAS_LEAF_STONE_ORE_RARE)
    val LEAF_STONE_UPPER = LEAF_STONES[0]
    val LEAF_STONE_LOWER = LEAF_STONES[1]
    val LEAF_STONE_UPPER_RARE = LEAF_STONES[2]
    val LEAF_STONE_LOWER_RARE = LEAF_STONES[3]

    // Moon Stone
    private val MOON_STONES = this.createOreFeatures("ore_moon_stone", CobblemonOreConfiguredFeatures.ORE_MOON_STONE, CobblemonBiomeTags.HAS_MOON_STONE_ORE, CobblemonBiomeTags.HAS_MOON_STONE_ORE_RARE)
    val MOON_STONE_UPPER = MOON_STONES[0]
    val MOON_STONE_LOWER = MOON_STONES[1]
    val MOON_STONE_UPPER_RARE = MOON_STONES[2]
    val MOON_STONE_LOWER_RARE = MOON_STONES[3]
    val MOON_STONE_DRIPSTONE = this.createFeature("ore_moon_stone_dripstone", CobblemonOreConfiguredFeatures.ORE_MOON_STONE_DRIPSTONE, { it.hasTag(CobblemonBiomeTags.HAS_MOON_STONE_ORE_DRIPSTONE) }, CountPlacementModifier.of(256), SquarePlacementModifier.of(), DRIPSTONE_RANGE, BiomePlacementModifier.of())

    // Shiny Stone
    private val SHINY_STONES = this.createOreFeatures("ore_shiny_stone", CobblemonOreConfiguredFeatures.ORE_SHINY_STONE, CobblemonBiomeTags.HAS_SHINY_STONE_ORE, CobblemonBiomeTags.HAS_SHINY_STONE_ORE_RARE)
    val SHINY_STONE_UPPER = SHINY_STONES[0]
    val SHINY_STONE_LOWER = SHINY_STONES[1]
    val SHINY_STONE_UPPER_RARE = SHINY_STONES[2]
    val SHINY_STONE_LOWER_RARE = SHINY_STONES[3]

    // Sun Stone
    private val SUN_STONES = this.createOreFeatures("ore_sun_stone", CobblemonOreConfiguredFeatures.ORE_SUN_STONE, CobblemonBiomeTags.HAS_SUN_STONE_ORE, CobblemonBiomeTags.HAS_SUN_STONE_ORE_RARE)
    val SUN_STONE_UPPER = SUN_STONES[0]
    val SUN_STONE_LOWER = SUN_STONES[1]
    val SUN_STONE_UPPER_RARE = SUN_STONES[2]
    val SUN_STONE_LOWER_RARE = SUN_STONES[3]

    // Thunder Stone
    private val THUNDER_STONES = this.createOreFeatures("ore_thunder_stone", CobblemonOreConfiguredFeatures.ORE_THUNDER_STONE, CobblemonBiomeTags.HAS_THUNDER_STONE_ORE, CobblemonBiomeTags.HAS_THUNDER_STONE_ORE_RARE)
    val THUNDER_STONE_UPPER = THUNDER_STONES[0]
    val THUNDER_STONE_LOWER = THUNDER_STONES[1]
    val THUNDER_STONE_UPPER_RARE = THUNDER_STONES[2]
    val THUNDER_STONE_LOWER_RARE = THUNDER_STONES[3]

    // Water Stone
    private val WATER_STONES = this.createOreFeatures("ore_water_stone", CobblemonOreConfiguredFeatures.ORE_WATER_STONE, CobblemonBiomeTags.HAS_WATER_STONE_ORE, CobblemonBiomeTags.HAS_WATER_STONE_ORE_RARE)
    val WATER_STONE_UPPER = WATER_STONES[0]
    val WATER_STONE_LOWER = WATER_STONES[1]
    val WATER_STONE_UPPER_RARE = WATER_STONES[2]
    val WATER_STONE_LOWER_RARE = WATER_STONES[3]

    fun register() {
        BiomeModifications.addProperties { context, properties ->
            this.features.forEach { holder ->
                if (holder.condition(context)) {
                    properties.generationProperties.addFeature(GenerationStep.Feature.UNDERGROUND_ORES, holder.feature)
                }
            }
        }
    }

    private fun createOreFeatures(name: String, registryEntry: RegistryEntry<ConfiguredFeature<OreFeatureConfig, *>>, commonConditionTag: TagKey<Biome>, rareConditionTag: TagKey<Biome>): Array<RegistryEntry<PlacedFeature>> = arrayOf(
        this.createFeature(name + "_upper", registryEntry, { it.hasTag(commonConditionTag) }, CountPlacementModifier.of(NORMAL_PLACEMENT_AMOUNT), SquarePlacementModifier.of(), UPPER_RANGE, BiomePlacementModifier.of()),
        this.createFeature(name + "_lower", registryEntry, { it.hasTag(commonConditionTag) }, CountPlacementModifier.of(NORMAL_PLACEMENT_AMOUNT), SquarePlacementModifier.of(), LOWER_RANGE, BiomePlacementModifier.of()),
        this.createFeature(name + "_upper_rare", registryEntry, { it.hasTag(rareConditionTag) }, CountPlacementModifier.of(RARE_PLACEMENT_AMOUNT), SquarePlacementModifier.of(), UPPER_RANGE, BiomePlacementModifier.of()),
        this.createFeature(name + "_lower_rare", registryEntry, { it.hasTag(rareConditionTag) }, CountPlacementModifier.of(RARE_PLACEMENT_AMOUNT), SquarePlacementModifier.of(), LOWER_RANGE, BiomePlacementModifier.of())
    )

    private fun createFeature(name: String, registryEntry: RegistryEntry<ConfiguredFeature<OreFeatureConfig, *>>, condition: (context: BiomeModifications.BiomeContext) -> Boolean, vararg modifiers: PlacementModifier): RegistryEntry<PlacedFeature> {
        val feature = PlacedFeatures.register(cobblemonResource(name).toString(), registryEntry, *modifiers)
        this.features += FeatureHolder(feature, condition)
        return feature
    }

    private data class FeatureHolder(
        val feature: RegistryEntry<PlacedFeature>,
        val condition: (context: BiomeModifications.BiomeContext) -> Boolean
    )

}