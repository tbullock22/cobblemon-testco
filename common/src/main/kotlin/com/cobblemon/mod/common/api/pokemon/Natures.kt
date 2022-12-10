/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon

import com.cobblemon.mod.common.api.item.Flavor
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.pokemon.Nature
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.lang
import net.minecraft.util.Identifier

/**
 * Registry for all Nature types
 * Get or register nature types
 *
 * @author Deltric
 * @since January 13th, 2022
 */
object Natures {
    private val allNatures = mutableListOf<Nature>()

    val HARDY = registerNature(
        Nature(cobblemonResource("hardy"), lang("nature.hardy"),
        null, null, null, null)
    )

    val LONELY = registerNature(
        Nature(cobblemonResource("lonely"), lang("nature.lonely"),
        Stats.ATTACK, Stats.DEFENCE, Flavor.SPICY, Flavor.SOUR)
    )

    val BRAVE = registerNature(
        Nature(cobblemonResource("brave"), lang("nature.brave"),
            Stats.ATTACK, Stats.SPEED, Flavor.SPICY, Flavor.SWEET)
    )

    val ADAMANT = registerNature(
        Nature(cobblemonResource("adamant"), lang("nature.adamant"),
        Stats.ATTACK, Stats.SPECIAL_ATTACK, Flavor.SPICY, Flavor.DRY)
    )

    val NAUGHTY = registerNature(
        Nature(cobblemonResource("naughty"), lang("nature.naughty"),
        Stats.ATTACK, Stats.SPECIAL_DEFENCE, Flavor.SPICY, Flavor.BITTER)
    )

    val BOLD = registerNature(
        Nature(cobblemonResource("bold"), lang("nature.bold"),
        Stats.DEFENCE, Stats.ATTACK, Flavor.SOUR, Flavor.SPICY)
    )

    val DOCILE = registerNature(
        Nature(cobblemonResource("docile"), lang("nature.docile"),
        null, null, null, null)
    )

    val RELAXED = registerNature(
        Nature(cobblemonResource("relaxed"), lang("nature.relaxed"),
        Stats.DEFENCE, Stats.SPEED, Flavor.SOUR, Flavor.SWEET)
    )

    val IMPISH = registerNature(
        Nature(cobblemonResource("impish"), lang("nature.impish"),
        Stats.DEFENCE, Stats.SPECIAL_ATTACK, Flavor.SOUR, Flavor.DRY)
    )

    val LAX = registerNature(
        Nature(cobblemonResource("lax"), lang("nature.lax"),
        Stats.DEFENCE, Stats.SPECIAL_DEFENCE, Flavor.SOUR, Flavor.BITTER)
    )

    val TIMID = registerNature(
        Nature(cobblemonResource("timid"), lang("nature.timid"),
        Stats.SPEED, Stats.ATTACK, Flavor.SWEET, Flavor.SPICY)
    )

    val HASTY = registerNature(
        Nature(cobblemonResource("hasty"), lang("nature.hasty"),
        Stats.SPEED, Stats.DEFENCE, Flavor.SWEET, Flavor.SOUR)
    )

    val SERIOUS = registerNature(
        Nature(cobblemonResource("serious"), lang("nature.serious"),
        null, null, null, null)
    )

    val JOLLY = registerNature(
        Nature(cobblemonResource("jolly"), lang("nature.jolly"),
        Stats.SPEED, Stats.SPECIAL_ATTACK, Flavor.SWEET, Flavor.DRY)
    )

    val NAIVE = registerNature(
        Nature(cobblemonResource("naive"), lang("nature.naive"),
        Stats.SPEED, Stats.SPECIAL_DEFENCE, Flavor.SWEET, Flavor.BITTER)
    )

    val MODEST = registerNature(
        Nature(cobblemonResource("modest"), lang("nature.modest"),
        Stats.SPECIAL_ATTACK, Stats.ATTACK, null, null)
    )

    val MILD = registerNature(
        Nature(cobblemonResource("mild"), lang("nature.mild"),
        Stats.SPECIAL_ATTACK, Stats.DEFENCE, Flavor.DRY, Flavor.SOUR)
    )

    val QUIET = registerNature(
        Nature(cobblemonResource("quiet"), lang("nature.quiet"),
        Stats.SPECIAL_ATTACK, Stats.SPEED, Flavor.DRY, Flavor.SWEET)
    )

    val BASHFUL = registerNature(
        Nature(cobblemonResource("bashful"), lang("nature.bashful"),
        null, null, null, null)
    )

    val RASH = registerNature(
        Nature(cobblemonResource("rash"), lang("nature.rash"),
        Stats.SPECIAL_ATTACK, Stats.SPECIAL_DEFENCE, Flavor.DRY, Flavor.BITTER)
    )

    val CALM = registerNature(
        Nature(cobblemonResource("calm"), lang("nature.calm"),
        Stats.SPECIAL_DEFENCE, Stats.ATTACK, Flavor.BITTER, Flavor.SPICY)
    )

    val GENTLE = registerNature(
        Nature(cobblemonResource("gentle"), lang("nature.gentle"),
        Stats.SPECIAL_DEFENCE, Stats.DEFENCE, Flavor.BITTER, Flavor.SOUR)
    )

    val SASSY = registerNature(
        Nature(cobblemonResource("sassy"), lang("nature.sassy"),
        Stats.SPECIAL_DEFENCE, Stats.SPEED, Flavor.BITTER, Flavor.SWEET)
    )

    val CAREFUL = registerNature(
        Nature(cobblemonResource("careful"), lang("nature.careful"),
        Stats.SPECIAL_DEFENCE, Stats.SPECIAL_ATTACK, Flavor.BITTER, Flavor.DRY)
    )

    val QUIRKY = registerNature(
        Nature(cobblemonResource("quirky"), lang("nature.quirky"),
        null, null, null, null)
    )


    /**
     * Registers a new nature type
     */
    fun registerNature(nature: Nature): Nature {
        allNatures.add(nature)
        return nature
    }

    /**
     * Gets a nature by registry name
     * @return a nature type or null
     */
    fun getNature(name: Identifier): Nature? {
        return allNatures.find { nature -> nature.name == name }
    }

    /**
     * Helper function for a random Nature
     * @return a random nature type
     */
    fun getRandomNature(): Nature {
        return allNatures.random()
    }

    fun all(): Collection<Nature> = this.allNatures.toList()

}