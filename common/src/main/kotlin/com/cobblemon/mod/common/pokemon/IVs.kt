/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon

import com.cobblemon.mod.common.Cobblemon

class IVs : PokemonStats() {
    override val acceptableRange = 0..MAX_VALUE
    override val defaultValue = 0
    // TODO: Hyper training

    companion object {
        const val MAX_VALUE = 31

        fun createRandomIVs(minPerfectIVs : Int = 0) : IVs = Cobblemon.statProvider.createEmptyIVs(minPerfectIVs)
    }
}