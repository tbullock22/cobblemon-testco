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
import com.cobblemon.mod.common.pokemon.evolution.requirements.RecoilRequirement
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.gson.JsonObject
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Identifier

/**
 * An [EvolutionProgress] meant to keep track of recoil taken in battle without fainting.
 *
 * @author Licious
 * @since January 27th, 2022
 */
class RecoilEvolutionProgress : EvolutionProgress<RecoilEvolutionProgress.Progress> {

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
                requirement is RecoilRequirement && requirement.amount == this.currentProgress().recoil && !evolutionController.contains(evolution)
            }
        }
    }

    override fun loadFromNBT(nbt: NbtCompound) {
        val recoil = nbt.getInt(RECOIL)
        this.updateProgress(Progress(recoil))
    }

    override fun saveToNBT(): NbtCompound = NbtCompound().apply { putInt(RECOIL, currentProgress().recoil) }

    override fun loadFromJson(json: JsonObject) {
        val recoil = json.get(RECOIL).asInt
        this.updateProgress(Progress(recoil))
    }

    override fun saveToJson(): JsonObject = JsonObject().apply { addProperty(RECOIL, currentProgress().recoil) }

    data class Progress(val recoil: Int)

    companion object {

        val ID = cobblemonResource(RecoilRequirement.ADAPTER_VARIANT)
        private const val RECOIL = "recoil"

    }

}