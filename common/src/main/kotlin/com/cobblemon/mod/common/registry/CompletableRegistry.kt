/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.registry

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.registry.CompletableRegistry.Companion.allRegistriesCompleted
import dev.architectury.registry.registries.DeferredRegister
import dev.architectury.registry.registries.RegistrySupplier
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryKey

/**
 * A registry which queues up for a [DeferredRegister] and will eventually be complete.
 * This wraps logic around checking for which registry entries still need to be registered
 * and completes the [completed] future once all entries are finished.
 *
 * When all the [CompletableRegistry] instances have completed, the [allRegistriesCompleted]
 * future will complete.
 *
 * This is a solution to deferred registries having inconsistent completion moments across
 * platforms as they lack a unified event that guarantees standard registrations are complete.
 *
 * @author Hiroku
 * @since June 26th, 2022
 */
open class CompletableRegistry<T>(val key: RegistryKey<Registry<T>>) {
    companion object {
        private val allRegistries = mutableListOf<Any>()
        val allRegistriesCompleted = CompletableFuture<Unit>()
    }

    val completed = CompletableFuture<Unit>()
    protected val deferredRegistry = DeferredRegister.create(Cobblemon.MODID, key)
    protected val pendingRegistryEntries = mutableListOf<RegistrySupplier<out T>>()

    init {
        allRegistries.add(key)
        completed.thenAccept {
            allRegistries.remove(key)
            if (allRegistries.isEmpty()) {
                allRegistriesCompleted.complete(Unit)
            }
        }
    }

    fun <E : T> queue(name: String, block: Supplier<E>): RegistrySupplier<E> {
        val entry = deferredRegistry.register(name, block)
        pendingRegistryEntries.add(entry)
        entry.listen {
            pendingRegistryEntries.remove(entry)
            if (pendingRegistryEntries.isEmpty()) {
                completed.complete(Unit)
            }
        }
        return entry
    }

    open fun register() {
        deferredRegistry.register()
    }
}