/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.evolution.progress

import com.cobblemon.mod.common.api.pokemon.evolution.progress.EvolutionProgress
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.evolution.requirements.DamageTakenRequirement
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.gson.JsonObject
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Identifier

/**
 * An [EvolutionProgress] meant to keep track of damage taken in battle without fainting.
 *
 * @author Licious
 * @since January 28th, 2022
 */
class DamageTakenEvolutionProgress : EvolutionProgress<DamageTakenEvolutionProgress.Progress> {

    private var progress = Progress(0)

    override fun id(): Identifier = ID

    override fun currentProgress(): Progress = this.progress

    override fun updateProgress(progress: Progress) {
        this.progress = progress
    }

    override fun reset() {
        this.updateProgress(Progress(0))
    }

    override fun shouldKeep(pokemon: Pokemon): Boolean {
        val evolutionController = pokemon.evolutionProxy.server()
        return pokemon.form.evolutions.any { evolution ->
            evolution.requirements.any { requirement ->
                requirement is DamageTakenRequirement && requirement.amount == this.currentProgress().amount && !evolutionController.contains(evolution)
            }
        }
    }

    override fun loadFromNBT(nbt: NbtCompound) {
        val amount = nbt.getInt(AMOUNT)
        this.updateProgress(Progress(amount))
    }

    override fun saveToNBT(): NbtCompound = NbtCompound().apply { putInt(AMOUNT, currentProgress().amount) }

    override fun loadFromJson(json: JsonObject) {
        val amount = json.get(AMOUNT).asInt
        this.updateProgress(Progress(amount))
    }

    override fun saveToJson(): JsonObject = JsonObject().apply { addProperty(AMOUNT, currentProgress().amount) }

    data class Progress(val amount: Int)

    companion object {

        val ID = cobblemonResource(DamageTakenRequirement.ADAPTER_VARIANT)
        private const val AMOUNT = "amount"

    }

}