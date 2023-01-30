/*
 * Copyright (C) 2022 Cobblemon Contributors
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

    val IS_ABYSS = create("is_abyss")
    val IS_ARID = create("is_arid")
    val IS_AUTUMN = create("is_autumn")
    val IS_BADLANDS = create("is_badlands")
    val IS_BAMBOO = create("is_bamboo")
    val IS_CAVE = create("is_cave")
    val IS_COAST = create("is_coast")
    val IS_COLD = create("is_cold")
    val IS_DEEP = create("is_deep")
    val IS_DESERT = create("is_desert")
    val IS_DRIPSTONE = create("is_dripstone")
    val IS_FLORAL = create("is_floral")
    val IS_FOREST = create("is_forest")
    val IS_FREEZING = create("is_freezing")
    val IS_FRESHWATER = create("is_freshwater")
    val IS_FRIGID = create("is_frigid")
    val IS_FROZEN = create("is_frozen")
    val IS_GLACIAL = create("is_glacial")
    val IS_GRASSLAND = create("is_grassland")
    val IS_HIGHLANDS = create("is_highlands")
    val IS_HILLS = create("is_hills")
    val IS_ISLAND = create("is_island")
    val IS_JUNGLE = create("is_jungle")
    val IS_LUKEWARM = create("is_lukewarm")
    val IS_LUSH = create("is_lush")
    val IS_MAGICAL = create("is_magical")
    val IS_MOUNTAIN = create("is_mountain")
    val IS_MUSHROOM = create("is_mushroom")
    val IS_OVERWORLD = create("is_overworld")
    val IS_PEAK = create("is_peak")
    val IS_PLAINS = create("is_plains")
    val IS_PLATEAU = create("is_plateau")
    val IS_REEF = create("is_reef")
    val IS_RIVER = create("is_river")
    val IS_SANDY = create("is_sandy")
    val IS_SAVANNA = create("is_savanna")
    val IS_SKY = create("is_sky")
    val IS_SNOWY = create("is_snowy")
    val IS_SPOOKY = create("is_spooky")
    val IS_SPRING = create("is_spring")
    val IS_SUMMER = create("is_summer")
    val IS_SWAMP = create("is_swamp")
    val IS_TAIGA = create("is_taiga")
    val IS_TEMPERATE = create("is_temperate")
    val IS_THERMAL = create("is_thermal")
    val IS_TUNDRA = create("is_tundra")
    val IS_VOID = create("is_void")
    val IS_VOlCANIC = create("is_volcanic")
    val IS_WINTER = create("is_winter")

    // Has Feature tags
    val HAS_APRICORNS_DENSE = create("has_feature/apricorns_dense")
    val HAS_APRICORNS_NORMAL = create("has_feature/apricorns_normal")
    val HAS_APRICORNS_SPARSE = create("has_feature/apricorns_sparse")

    // Has Ore tags
    val HAS_DAWN_STONE_ORE = create("has_ore/ore_dawn_stone_normal")
    val HAS_DAWN_STONE_ORE_RARE = create("has_ore/ore_dawn_stone_rare")
    val HAS_DUSK_STONE_ORE = create("has_ore/ore_dusk_stone_normal")
    val HAS_DUSK_STONE_ORE_RARE = create("has_ore/ore_dusk_stone_rare")
    val HAS_FIRE_STONE_ORE = create("has_ore/ore_fire_stone_normal")
    val HAS_FIRE_STONE_ORE_RARE = create("has_ore/ore_fire_stone_rare")
    val HAS_ICE_STONE_ORE = create("has_ore/ore_ice_stone_normal")
    val HAS_ICE_STONE_ORE_RARE = create("has_ore/ore_ice_stone_rare")
    val HAS_LEAF_STONE_ORE = create("has_ore/ore_leaf_stone_normal")
    val HAS_LEAF_STONE_ORE_RARE = create("has_ore/ore_leaf_stone_rare")
    val HAS_MOON_STONE_ORE = create("has_ore/ore_moon_stone_normal")
    val HAS_MOON_STONE_ORE_RARE = create("has_ore/ore_moon_stone_rare")
    val HAS_MOON_STONE_ORE_DRIPSTONE = create("has_ore/ore_moon_stone_dripstone")
    val HAS_SHINY_STONE_ORE = create("has_ore/ore_shiny_stone_normal")
    val HAS_SHINY_STONE_ORE_RARE = create("has_ore/ore_shiny_stone_rare")
    val HAS_SUN_STONE_ORE = create("has_ore/ore_sun_stone_normal")
    val HAS_SUN_STONE_ORE_RARE = create("has_ore/ore_sun_stone_rare")
    val HAS_THUNDER_STONE_ORE = create("has_ore/ore_thunder_stone_normal")
    val HAS_THUNDER_STONE_ORE_RARE = create("has_ore/ore_thunder_stone_rare")
    val HAS_WATER_STONE_ORE = create("has_ore/ore_water_stone_normal")
    val HAS_WATER_STONE_ORE_RARE = create("has_ore/ore_water_stone_rare")

    // ToDo remove in 1.3
    @Deprecated(message = "This tag no longer exists", level = DeprecationLevel.ERROR)
    val IS_CRAG = create("is_crag")
    @Deprecated(message = "This tag no longer exists", level = DeprecationLevel.ERROR)
    val IS_ICY = create("is_icy")
    @Deprecated(message = "This tag no longer exists", replaceWith = ReplaceWith("com.cobblemon.mod.common.api.tags.CobblemonBiomeTags.IS_CAVE"), level = DeprecationLevel.ERROR)
    val IS_UNDERGROUND = create("is_underground")

    private fun create(path: String) = TagKey.of(Registry.BIOME_KEY, cobblemonResource(path))
}