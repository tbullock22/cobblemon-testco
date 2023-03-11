/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.tags

import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.tag.TagKey
import net.minecraft.util.registry.Registry

/**
 * A collection of the Cobblemon [TagKey]s related to the [Registry.BIOME_KEY].
 *
 * @author Veraxiel, Licious
 * @since July 8th, 2022
 */
object CobblemonBiomeTags {

    @JvmField
    val IS_ABYSS = create("is_abyss")
    @JvmField
    val IS_ARID = create("is_arid")
    @JvmField
    val IS_AUTUMN = create("is_autumn")
    @JvmField
    val IS_BADLANDS = create("is_badlands")
    @JvmField
    val IS_BAMBOO = create("is_bamboo")
    @JvmField
    val IS_CAVE = create("is_cave")
    @JvmField
    val IS_COAST = create("is_coast")
    @JvmField
    val IS_COLD = create("is_cold")
    @JvmField
    val IS_DEEP = create("is_deep")
    @JvmField
    val IS_DESERT = create("is_desert")
    @JvmField
    val IS_DRIPSTONE = create("is_dripstone")
    @JvmField
    val IS_FLORAL = create("is_floral")
    @JvmField
    val IS_FOREST = create("is_forest")
    @JvmField
    val IS_FREEZING = create("is_freezing")
    @JvmField
    val IS_FRESHWATER = create("is_freshwater")
    @JvmField
    val IS_FRIGID = create("is_frigid")
    @JvmField
    val IS_FROZEN = create("is_frozen")
    @JvmField
    val IS_GLACIAL = create("is_glacial")
    @JvmField
    val IS_GRASSLAND = create("is_grassland")
    @JvmField
    val IS_HIGHLANDS = create("is_highlands")
    @JvmField
    val IS_HILLS = create("is_hills")
    @JvmField
    val IS_ISLAND = create("is_island")
    @JvmField
    val IS_JUNGLE = create("is_jungle")
    @JvmField
    val IS_LUKEWARM = create("is_lukewarm")
    @JvmField
    val IS_LUSH = create("is_lush")
    @JvmField
    val IS_MAGICAL = create("is_magical")
    @JvmField
    val IS_MOUNTAIN = create("is_mountain")
    @JvmField
    val IS_MUSHROOM = create("is_mushroom")
    @JvmField
    val IS_OVERWORLD = create("is_overworld")
    @JvmField
    val IS_PEAK = create("is_peak")
    @JvmField
    val IS_PLAINS = create("is_plains")
    @JvmField
    val IS_PLATEAU = create("is_plateau")
    @JvmField
    val IS_REEF = create("is_reef")
    @JvmField
    val IS_RIVER = create("is_river")
    @JvmField
    val IS_SANDY = create("is_sandy")
    @JvmField
    val IS_SAVANNA = create("is_savanna")
    @JvmField
    val IS_SKY = create("is_sky")
    @JvmField
    val IS_SNOWY = create("is_snowy")
    @JvmField
    val IS_SPOOKY = create("is_spooky")
    @JvmField
    val IS_SPRING = create("is_spring")
    @JvmField
    val IS_SUMMER = create("is_summer")
    @JvmField
    val IS_SWAMP = create("is_swamp")
    @JvmField
    val IS_TAIGA = create("is_taiga")
    @JvmField
    val IS_TEMPERATE = create("is_temperate")
    @JvmField
    val IS_THERMAL = create("is_thermal")
    @JvmField
    val IS_TUNDRA = create("is_tundra")
    @JvmField
    val IS_VOID = create("is_void")
    @JvmField
    val IS_VOlCANIC = create("is_volcanic")
    @JvmField
    val IS_WINTER = create("is_winter")

    // Has Feature tags
    @JvmField
    val HAS_APRICORNS_DENSE = create("has_feature/apricorns_dense")
    @JvmField
    val HAS_APRICORNS_NORMAL = create("has_feature/apricorns_normal")
    @JvmField
    val HAS_APRICORNS_SPARSE = create("has_feature/apricorns_sparse")

    // Has Ore tags
    @JvmField
    val HAS_DAWN_STONE_ORE = create("has_ore/ore_dawn_stone_normal")
    @JvmField
    val HAS_DAWN_STONE_ORE_RARE = create("has_ore/ore_dawn_stone_rare")
    @JvmField
    val HAS_DUSK_STONE_ORE = create("has_ore/ore_dusk_stone_normal")
    @JvmField
    val HAS_DUSK_STONE_ORE_RARE = create("has_ore/ore_dusk_stone_rare")
    @JvmField
    val HAS_FIRE_STONE_ORE = create("has_ore/ore_fire_stone_normal")
    @JvmField
    val HAS_FIRE_STONE_ORE_RARE = create("has_ore/ore_fire_stone_rare")
    @JvmField
    val HAS_ICE_STONE_ORE = create("has_ore/ore_ice_stone_normal")
    @JvmField
    val HAS_ICE_STONE_ORE_RARE = create("has_ore/ore_ice_stone_rare")
    @JvmField
    val HAS_LEAF_STONE_ORE = create("has_ore/ore_leaf_stone_normal")
    @JvmField
    val HAS_LEAF_STONE_ORE_RARE = create("has_ore/ore_leaf_stone_rare")
    @JvmField
    val HAS_MOON_STONE_ORE = create("has_ore/ore_moon_stone_normal")
    @JvmField
    val HAS_MOON_STONE_ORE_RARE = create("has_ore/ore_moon_stone_rare")
    @JvmField
    val HAS_MOON_STONE_ORE_DRIPSTONE = create("has_ore/ore_moon_stone_dripstone")
    @JvmField
    val HAS_SHINY_STONE_ORE = create("has_ore/ore_shiny_stone_normal")
    @JvmField
    val HAS_SHINY_STONE_ORE_RARE = create("has_ore/ore_shiny_stone_rare")
    @JvmField
    val HAS_SUN_STONE_ORE = create("has_ore/ore_sun_stone_normal")
    @JvmField
    val HAS_SUN_STONE_ORE_RARE = create("has_ore/ore_sun_stone_rare")
    @JvmField
    val HAS_THUNDER_STONE_ORE = create("has_ore/ore_thunder_stone_normal")
    @JvmField
    val HAS_THUNDER_STONE_ORE_RARE = create("has_ore/ore_thunder_stone_rare")
    @JvmField
    val HAS_WATER_STONE_ORE = create("has_ore/ore_water_stone_normal")
    @JvmField
    val HAS_WATER_STONE_ORE_RARE = create("has_ore/ore_water_stone_rare")

    // ToDo remove in 1.3
    @Deprecated(message = "This tag no longer exists", level = DeprecationLevel.ERROR)
    @JvmField
    val IS_CRAG = create("is_crag")
    @Deprecated(message = "This tag no longer exists", level = DeprecationLevel.ERROR)
    @JvmField
    val IS_ICY = create("is_icy")
    @Deprecated(message = "This tag no longer exists", replaceWith = ReplaceWith("com.cobblemon.mod.common.api.tags.CobblemonBiomeTags.IS_CAVE"), level = DeprecationLevel.ERROR)
    @JvmField
    val IS_UNDERGROUND = create("is_underground")

    private fun create(path: String) = TagKey.of(Registry.BIOME_KEY, cobblemonResource(path))
}