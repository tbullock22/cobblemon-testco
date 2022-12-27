/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.tags

import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.tag.TagKey
import net.minecraft.util.registry.Registry

/**
 * Contains all the Cobblemon biome category tag keys.
 *
 * @author Veraxiel, Licious
 * @since July 8th, 2022
 */
object CobblemonBiomeTags {

    val IS_ARID = create("is_arid")
    val IS_BAMBOO = create("is_bamboo")
    val IS_COAST = create("is_coast")
    val IS_COLD = create("is_cold")
    val IS_CRAG = create("is_crag")
    val IS_DESERT = create("is_desert")
    val IS_FLORAL = create("is_floral")
    val IS_FRESHWATER = create("is_freshwater")
    val IS_FRIGID = create("is_frigid")
    val IS_FROZEN = create("is_frozen")
    val IS_GRASSLAND = create("is_grassland")
    val IS_ICY = create("is_icy")
    val IS_LUKEWARM = create("is_lukewarm")
    val IS_MAGICAL = create("is_magical")
    val IS_MOUNTAIN = create("is_mountain")
    val IS_MUSHROOM = create("is_mushroom")
    val IS_PEAK = create("is_peak")
    val IS_PLAINS = create("is_plains")
    val IS_PLATEAU = create("is_plateau")
    val IS_REEF = create("is_reef")
    val IS_SANDY = create("is_sandy")
    val IS_SNOWY = create("is_snowy")
    val IS_SPOOKY = create("is_spooky")
    val IS_SWAMP = create("is_swamp")
    val IS_TAIGA = create("is_taiga")
    val IS_TEMPERATE = create("is_temperate")
    val IS_TUNDRA = create("is_tundra")
    val IS_UNDERGROUND = create("is_underground")
    val IS_VOID = create("is_void")
    val IS_SUMMER = create("is_summer")
    val IS_AUTUMN = create("is_fall")
    val IS_WINTER = create("is_winter")
    val IS_SPRING = create("is_spring")

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

    private fun create(path: String) = TagKey.of(Registry.BIOME_KEY, cobblemonResource(path))
}