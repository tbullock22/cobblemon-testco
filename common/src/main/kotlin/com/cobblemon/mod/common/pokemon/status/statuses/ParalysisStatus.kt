/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.status.statuses

import com.cobblemon.mod.common.pokemon.status.PersistentStatus
import com.cobblemon.mod.common.util.cobblemonResource
class ParalysisStatus : PersistentStatus(
    name = cobblemonResource("paralysis"),
    showdownName = "par",
    applyMessage = "cobblemon.status.paralysis.apply",
    removeMessage = null,
    defaultDuration = IntRange(180, 300)
)