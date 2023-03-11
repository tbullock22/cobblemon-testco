/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.status

import com.cobblemon.mod.common.pokemon.status.statuses.BurnStatus
import com.cobblemon.mod.common.pokemon.status.statuses.FrozenStatus
import com.cobblemon.mod.common.pokemon.status.statuses.ParalysisStatus
import com.cobblemon.mod.common.pokemon.status.statuses.PoisonBadlyStatus
import com.cobblemon.mod.common.pokemon.status.statuses.PoisonStatus
import com.cobblemon.mod.common.pokemon.status.statuses.SleepStatus
import net.minecraft.util.Identifier

/**
 * Main API point for Statuses
 * Get or register Statuses
 *
 * @author Deltric
 */
object Statuses {
    private val allStatuses = mutableListOf<Status>()

    val POISON = registerStatus(PoisonStatus())
    val POISON_BADLY = registerStatus(PoisonBadlyStatus())
    val PARALYSIS = registerStatus(ParalysisStatus())
    val SLEEP = registerStatus(SleepStatus())
    val FROZEN = registerStatus(FrozenStatus())
    val BURN = registerStatus(BurnStatus())

    fun <T: Status> registerStatus(status: T) : T {
        allStatuses.add(status)
        return status
    }

    fun getStatus(name: Identifier) = allStatuses.find { status -> status.name == name }
    fun getStatus(showdownName: String) = allStatuses.find { it.showdownName == showdownName }
}