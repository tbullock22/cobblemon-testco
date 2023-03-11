/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.evolution.progress

import com.cobblemon.mod.common.api.pokemon.evolution.EvolutionController
import com.cobblemon.mod.common.api.serialization.DataSerializer
import com.cobblemon.mod.common.pokemon.Pokemon
import com.google.gson.JsonObject
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Identifier

/**
 * Represents a container of progression towards the completion of an evolution requirement.
 * To register an evolution progress use [EvolutionProgressFactory.registerVariant].
 *
 * @param T The type of the progression.
 *
 * @author Licious
 * @since January 27th, 2023
 */
interface EvolutionProgress<T> : DataSerializer<NbtCompound, JsonObject> {

    /**
     * Returns the unique ID of the evolution progress.
     *
     * @return The [Identifier] of this [EvolutionProgress].
     */
    fun id(): Identifier

    /**
     * Gets the current progress of type [T].
     *
     * @return The progress of type [T].
     */
    fun currentProgress(): T

    /**
     * Updates the current progress with the given [progress].
     *
     * @param progress The new state of the progress of type [T].
     */
    fun updateProgress(progress: T)

    /**
     * Resets the current progress.
     * This will create the updated instance and invoke [updateProgress].
     */
    fun reset()

    /**
     * Invoked during [EvolutionController.loadFromNBT] or [EvolutionController.loadFromJson].
     * This is meant to decide if the progress saved to disk should be discarded based on the current evolution requirements of a Pokémon.
     *
     * @param pokemon The [Pokemon] being queried.
     * @return True if the progress is still required otherwise false.
     */
    fun shouldKeep(pokemon: Pokemon): Boolean

}