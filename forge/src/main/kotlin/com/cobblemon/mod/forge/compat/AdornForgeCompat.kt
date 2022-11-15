/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.forge.compat

import com.cobblemon.mod.common.Cobblemon
import juuxel.adorn.api.block.BlockVariant
import juuxel.adorn.compat.BlockVariantSet

object AdornForgeCompat : BlockVariantSet {

    init {
        Cobblemon.LOGGER.info("Adding compatibility for Adorn")
    }

    override val woodVariants = listOf(
        BlockVariant.Wood("${Cobblemon.MODID}/apricorn")
    )

}