/*
 * Copyright (C) 2022 Pokemon Cobbled Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cablemc.pokemoncobbled.common.api.reactive.pipes

import com.cablemc.pokemoncobbled.common.api.reactive.Transform

/**
 * A transform that will only emit received values that match the given predicate.
 *
 * @author Hiroku
 * @since November 26th, 2021
 */
class FilterTransform<I>(private val predicate: (I) -> Boolean) : Transform<I, I> {
    override fun invoke(input: I): I {
        if (predicate(input)) {
            return input
        } else {
            noTransform(terminate = false)
        }
    }
}