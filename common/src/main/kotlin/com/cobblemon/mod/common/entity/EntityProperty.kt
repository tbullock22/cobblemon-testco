/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity

import com.cobblemon.mod.common.api.reactive.SettableObservable
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedData

/**
 * Entity properties are a wrapping around entityData that can handle subscriptions on either Side.
 */
class EntityProperty<T>(
    private val dataTracker: DataTracker,
    private val accessor: TrackedData<T>,
    initialValue: T
): SettableObservable<T>(initialValue) {
    init {
        dataTracker.startTracking(accessor, initialValue)
    }

    fun checkForUpdate() {
        val newValue = dataTracker.get(accessor)
        if (newValue != get()) {
            super.set(newValue)
        }
    }

    override fun set(newValue: T) {
        dataTracker.set(accessor, newValue)
        super.set(newValue)
    }
}