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
 * A collection of the Cobblemon [TagKey]s related to the [Registry.ITEM].
 *
 * @author Licious
 * @since January 8th, 2023
 */
object CobblemonItemTags {

    val APRICORN_LOGS = create("apricorn_logs")
    val APRICORN_SEEDS = create("apricorn_seeds")
    val APRICORNS = create("apricorns")
    val EXPERIENCE_CANDIES = create("experience_candies")
    val POKEBALLS = create("pokeballs")
    val ANY_HELD_ITEM = create("held/is_held_item")
    val EXPERIENCE_SHARE = create("held/experience_share")
    val LUCKY_EGG = create("held/lucky_egg")

    private fun create(path: String) = TagKey.of(Registry.ITEM_KEY, cobblemonResource(path))

}