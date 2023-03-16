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
 * A collection of the Cobblemon [TagKey]s related to the [Registry.BLOCK].
 *
 * @author Licious
 * @since October 29th, 2022
 */
object CobblemonBlockTags {

    @JvmField
    val APRICORN_LEAVES = createTag("apricorn_leaves")
    @JvmField
    val APRICORN_LOGS = createTag("apricorn_logs")
    @JvmField
    val APRICORN_SAPLINGS = createTag("apricorn_saplings")
    @JvmField
    val APRICORNS = createTag("apricorns")
    @JvmField
    val DRIPSTONE_GROWABLE = createTag("dripstone_growable")
    @JvmField
    val DRIPSTONE_REPLACEABLES = createTag("dripstone_replaceables")

    // ToDo remove in 1.3
    @Deprecated(message = "This tag no longer exists", level = DeprecationLevel.ERROR)
    @JvmField
    val FENCE_GATES = createTag("fence_gates")
    @Deprecated(message = "This tag no longer exists", level = DeprecationLevel.ERROR)
    @JvmField
    val FENCES = createTag("fences")
    @Deprecated(message = "This tag no longer exists", level = DeprecationLevel.ERROR)
    @JvmField
    val LEAVES = createTag("leaves")
    @Deprecated(message = "This tag no longer exists", level = DeprecationLevel.ERROR)
    @JvmField
    val LOGS = createTag("logs")
    @Deprecated(message = "This tag no longer exists", level = DeprecationLevel.ERROR)
    @JvmField
    val LOGS_THAT_BURN = createTag("logs_that_burn")
    @Deprecated(message = "This tag no longer exists", level = DeprecationLevel.ERROR)
    @JvmField
    val PLANKS = createTag("planks")
    @Deprecated(message = "This tag no longer exists", level = DeprecationLevel.ERROR)
    @JvmField
    val SAPLINGS = createTag("saplings")
    @Deprecated(message = "This tag no longer exists", level = DeprecationLevel.ERROR)
    @JvmField
    val STANDING_SIGNS = createTag("standing_signs")
    @Deprecated(message = "This tag no longer exists", level = DeprecationLevel.ERROR)
    @JvmField
    val WALL_SIGNS = createTag("wall_signs")
    @Deprecated(message = "This tag no longer exists", level = DeprecationLevel.ERROR)
    @JvmField
    val WOODEN_BUTTONS = createTag("wooden_buttons")
    @Deprecated(message = "This tag no longer exists", level = DeprecationLevel.ERROR)
    @JvmField
    val WOODEN_FENCES = createTag("wooden_fences")
    @Deprecated(message = "This tag no longer exists", level = DeprecationLevel.ERROR)
    @JvmField
    val WOODEN_PRESSURE_PLATES = createTag("wooden_pressure_plates")
    @Deprecated(message = "This tag no longer exists", level = DeprecationLevel.ERROR)
    @JvmField
    val WOODEN_SLABS = createTag("wooden_slabs")
    @Deprecated(message = "This tag no longer exists", level = DeprecationLevel.ERROR)
    @JvmField
    val WOODEN_STAIRS = createTag("wooden_stairs")

    private fun createTag(name: String) = TagKey.of(Registry.BLOCK_KEY, cobblemonResource(name))

}