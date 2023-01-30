/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.moves

import com.cobblemon.mod.common.api.moves.categories.DamageCategories
import com.cobblemon.mod.common.api.moves.categories.DamageCategory
import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.api.types.ElementalTypes
import com.cobblemon.mod.common.battles.MoveTarget
import com.cobblemon.mod.common.util.lang
import com.google.gson.annotations.SerializedName
import net.minecraft.text.MutableText

/**
 * This class represents the base of a Move.
 * To build a Move you need to use its template
 *
 * @param name The name in Showdown ID form.
 * @param elementalType The [ElementalType] of this move.
 * @param damageCategory The [DamageCategory] of this move.
 * @param power The base power of this move.
 * @param target The [MoveTarget] of this move.
 * @param accuracy The accuracy of this move.
 * @param pp The power points of this move, these mean how many times the move can be used before some manner of restoration is required.
 * @param priority The priority of this move.
 * @param critRatio The ratio at which this move will land a critical hit.
 * @param effectChances The effect chances if any ordered by effect.
 */
open class MoveTemplate(
    val name: String,
    @SerializedName("type")
    val elementalType: ElementalType,
    val damageCategory: DamageCategory,
    val power: Double,
    val target: MoveTarget,
    val accuracy: Double,
    val pp: Int,
    val priority: Int,
    val critRatio: Double,
    val effectChances: Array<Double>
) {
    /** Don't change this from 0. It was -1, but then the data JSON reader was making this 0 instead of -1, ignoring our original video. */
    var id = 0
    val displayName: MutableText
        get() = lang("move.$name")
    val description: MutableText
        get() = lang("move.$name.desc")
    val maxPp: Int
        get() = 8 * pp / 5
    class Dummy(name: String) : MoveTemplate(
        name = name,
        elementalType = ElementalTypes.NORMAL,
        damageCategory = DamageCategories.STATUS,
        power = 0.0,
        target = MoveTarget.all,
        accuracy = 100.0,
        pp = 5,
        priority = 0,
        critRatio = 0.0,
        effectChances = emptyArray()
    )

    companion object {
        fun dummy(name: String) = Dummy(name)
    }

    /**
     * Creates the Move with full PP
     */
    fun create() = create(pp)

    /**
     * Creates the Move with given PP out of the normal maximum
     */
    fun create(currentPp: Int) = create(currentPp, 0)

    /**
     * Creates the Move with given current PP and the given raised PP stages.
     */
    fun create(currentPp: Int, raisedPpStages: Int): Move {
        return Move(
            currentPp = currentPp,
            raisedPpStages = raisedPpStages,
            template = this
        )
    }
}
