/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage.player

import com.cobblemon.mod.common.api.types.ElementalType

class PlayerAdvancementData {

    var totalCaptureCount: Int = 0
        private set
    var totalEggsHatched: Int = 0
        private set
    var totalEvolvedCount: Int = 0
        private set
    var totalBattleVictoryCount: Int = 0
        private set
    var totalShinyCaptureCount: Int = 0
        private set

    private var totalTypeCaptureCounts = mutableMapOf<String, Int>()

    fun updateTotalCaptureCount() {
        totalCaptureCount++
    }

    fun updateTotalEggsHatched() {
        totalEggsHatched++
    }

    fun updateTotalEvolvedCount() {
        totalEvolvedCount++
    }

    fun updateTotalBattleVictoryCount() {
        totalEvolvedCount++
    }

    fun updateTotalShinyCaptureCount() {
        totalShinyCaptureCount++
    }

    fun getTotalTypeCaptureCount(type: ElementalType): Int {
        if (!totalTypeCaptureCounts.containsKey(key = type.name)) {
            totalTypeCaptureCounts[type.name] = 0
        }
        return totalTypeCaptureCounts.get(key = type.name) ?: 0
    }

    fun updateTotalTypeCaptureCount(type: ElementalType) {
        val count = totalTypeCaptureCounts[type.name] ?: 0
        if (count == 0) {
            totalTypeCaptureCounts[type.name] = 1
        } else {
            totalTypeCaptureCounts.replace(type.name, count + 1)
        }
    }
}