/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.evolution.progress

import com.cobblemon.mod.common.pokemon.evolution.progress.RecoilEvolutionProgress
import com.cobblemon.mod.common.pokemon.evolution.progress.UseMoveEvolutionProgress

object EvolutionProgressFactory {

    private val variants = hashMapOf<String, () -> EvolutionProgress<*>>()

    init {
        registerVariant(RecoilEvolutionProgress.ID.toString()) { RecoilEvolutionProgress() }
        registerVariant(UseMoveEvolutionProgress.ID.toString()) { UseMoveEvolutionProgress() }
    }

    fun registerVariant(variant: String, factory: () -> EvolutionProgress<*>) {
        variants[variant] = factory
    }

    fun create(variant: String): EvolutionProgress<*>? {
        val factory = variants[variant] ?: return null
        return factory.invoke()
    }

}