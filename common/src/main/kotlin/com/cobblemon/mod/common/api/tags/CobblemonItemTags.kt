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
 * A collection of the Cobblemon [TagKey]s related to the [Registry.ITEM].
 *
 * @author Licious
 * @since January 8th, 2023
 */
object CobblemonItemTags {

    @JvmField
    val APRICORN_LOGS = create("apricorn_logs")
    @JvmField
    val APRICORN_SEEDS = create("apricorn_seeds")
    @JvmField
    val APRICORNS = create("apricorns")
    @JvmField
    val EXPERIENCE_CANDIES = create("experience_candies")
    @JvmField
    val POKEBALLS = create("poke_balls")
    @JvmField
    val ANY_HELD_ITEM = create("held/is_held_item")
    @JvmField
    val EXPERIENCE_SHARE = create("held/experience_share")
    @JvmField
    val LUCKY_EGG = create("held/lucky_egg")
    @JvmField
    val EVOLUTION_STONES = create("evolution_stones")
    @JvmField
    val EVOLUTION_ITEMS = create("evolution_items")

    /**
     * This tag is only used for a Torterra aspect based easter egg evolution at the moment.
     * It simply includes the 'minecraft:azalea' and 'minecraft:flowering_azalea' items by default.
     */
    val AZALEA_TREE = create("azalea_tree")

    private fun create(path: String) = TagKey.of(Registry.ITEM_KEY, cobblemonResource(path))

}