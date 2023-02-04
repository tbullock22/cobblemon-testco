/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.evolution.progress

import com.cobblemon.mod.common.api.moves.MoveTemplate
import com.cobblemon.mod.common.api.moves.Moves
import com.cobblemon.mod.common.api.pokemon.evolution.progress.EvolutionProgress
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.evolution.requirements.UseMoveRequirement
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.gson.JsonObject
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Identifier

/**
 * An [EvolutionProgress] meant to keep track of the amount of times a specific move in battle.
 *
 * @author Licious
 * @since January 28th, 2022
 */
class UseMoveEvolutionProgress : EvolutionProgress<UseMoveEvolutionProgress.Progress> {

    private var progress = Progress(MoveTemplate.dummy(""), 0)

    override fun id(): Identifier = ID

    override fun currentProgress(): Progress = this.progress

    override fun updateProgress(progress: Progress) {
        this.progress = progress
    }

    override fun reset() {
        this.progress = Progress(MoveTemplate.dummy(""), 0)
    }

    override fun shouldKeep(pokemon: Pokemon): Boolean {
        val evolutionController = pokemon.evolutionProxy.server()
        return pokemon.form.evolutions.any { evolution ->
            evolution.requirements.any { requirement ->
                requirement is UseMoveRequirement && requirement.move == this.progress.move && requirement.amount == this.currentProgress().amount && !evolutionController.contains(evolution)
            }
        }
    }

    override fun loadFromNBT(nbt: NbtCompound) {
        val moveId = nbt.getString(MOVE)
        val move = Moves.getByName(moveId) ?: return
        val amount = nbt.getInt(AMOUNT)
        this.updateProgress(Progress(move, amount))
    }

    override fun saveToNBT(): NbtCompound {
        val nbt = NbtCompound()
        nbt.putString(MOVE, this.currentProgress().move.name)
        nbt.putInt(AMOUNT, this.currentProgress().amount)
        return nbt
    }

    override fun loadFromJson(json: JsonObject) {
        val moveId = json.get(MOVE).asString
        val move = Moves.getByName(moveId) ?: return
        val amount = json.get(AMOUNT).asInt
        this.updateProgress(Progress(move, amount))
    }

    override fun saveToJson(): JsonObject {
        val jObject = JsonObject()
        jObject.addProperty(MOVE, this.currentProgress().move.name)
        jObject.addProperty(AMOUNT, this.currentProgress().amount)
        return jObject
    }

    data class Progress(
        val move: MoveTemplate,
        val amount: Int
    )

    companion object {
        val ID = cobblemonResource("use_move")
        private const val MOVE = "move"
        private const val AMOUNT = "amount"
    }

}