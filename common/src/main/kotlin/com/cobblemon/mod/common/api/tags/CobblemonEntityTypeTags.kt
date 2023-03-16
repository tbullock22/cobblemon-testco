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
 * A collection of the Cobblemon [TagKey]s related to the [Registry.ENTITY_TYPE].
 *
 * @author Licious
 * @since March 6th, 2023
 */
object CobblemonEntityTypeTags {


    /**
     * A tag that is used by Cobblemon to identify entities name tags can't be used on.
     */
    @JvmField
    val CANNOT_HAVE_NAME_TAG = create("cannot_have_name_tag")

    private fun create(path: String) = TagKey.of(Registry.ENTITY_TYPE_KEY, cobblemonResource(path))

}