/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.advancement

import com.cobblemon.mod.common.advancement.criterion.CaughtPokemonCriterionCondition
import com.cobblemon.mod.common.advancement.criterion.PickStarterCriterionCondition
import com.cobblemon.mod.common.advancement.criterion.SimpleCountableCriterionCondition
import com.cobblemon.mod.common.advancement.criterion.SimpleCriterionTrigger
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.advancement.criterion.Criteria

/**
 * Contains all the advancement criteria in Cobblemon.
 *
 * @author Licious
 * @since October 26th, 2022
 */
object CobblemonCriteria {
    val PICK_STARTER = Criteria.register(
        SimpleCriterionTrigger(
            cobblemonResource("pick_starter"),
            PickStarterCriterionCondition::class.java
        )
    )
    val CATCH_POKEMON = Criteria.register(
        SimpleCriterionTrigger(
            cobblemonResource("catch_pokemon"),
            CaughtPokemonCriterionCondition::class.java
        )
    )
    val CATCH_SHINY_POKEMON = Criteria.register(
        SimpleCriterionTrigger(
            cobblemonResource("catch_shiny_pokemon"),
            SimpleCountableCriterionCondition::class.java
        )
    )
    val EGG_HATCH = Criteria.register(
        SimpleCriterionTrigger(
            cobblemonResource("eggs_hatched"),
            SimpleCountableCriterionCondition::class.java
        )
    )
    val EVOLVE_POKEMON = Criteria.register(
        SimpleCriterionTrigger(
            cobblemonResource("pokemon_evolved"),
            SimpleCountableCriterionCondition::class.java
        )
    )
    val WIN_BATTLE = Criteria.register(
        SimpleCriterionTrigger(
            cobblemonResource("battles_won"),
            SimpleCountableCriterionCondition::class.java
        )
    )
}