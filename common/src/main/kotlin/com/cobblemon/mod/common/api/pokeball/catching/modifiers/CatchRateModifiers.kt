/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokeball.catching.modifiers

import com.cobblemon.mod.common.api.pokeball.PokeBalls
import com.cobblemon.mod.common.api.pokeball.catching.CatchRateModifier
import com.cobblemon.mod.common.api.pokemon.status.Status
import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.battles.BattleRegistry
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.tags.CobblemonBiomeTags
import net.minecraft.entity.LivingEntity

/**
 * A collection of some default usages of [CatchRateModifier]s.
 *
 * @author Licious
 * @since May 7th, 2022
 */
object CatchRateModifiers {

    /**
     * Used by [PokeBalls.LEVEL_BALL].
     */
    val LEVEL = BattleModifier { currentCatchRate, _, playerPokemon, pokemon ->
        val highestLevel = playerPokemon.maxOf { bp -> bp.battlePokemon?.originalPokemon?.level ?: 1 }
        val multiplier = when {
            highestLevel > (pokemon.level * 4) -> 4F
            highestLevel > (pokemon.level * 2) -> 3F
            highestLevel > pokemon.level -> 2F
            else -> 1F
        }
        return@BattleModifier currentCatchRate * multiplier
    }

    /**
     * Used by [PokeBalls.DIVE_BALL].
     * This is the Minecraft interpretation of the generation 3 mechanic.
     */
    val SUBMERGED_IN_WATER = WorldStateModifier { currentCatchRate, _, entity -> if (entity.isSubmergedInWater) currentCatchRate * 3.5F else currentCatchRate }

    /**
     * Used by [PokeBalls.NEST_BALL].
     * See the [Bulbapedia article](https://bulbapedia.bulbagarden.net/wiki/Nest_Ball) for more info.
     */
    val NEST: CatchRateModifier = BasicCatchRateModifier { currentCatchRate, _, pokemon, host ->
        return@BasicCatchRateModifier if (pokemon.level >= 30) {
            currentCatchRate
        } else {
            currentCatchRate * ((41 - pokemon.level) / 10F)
        }
    }

    /**
     * Used by [PokeBalls.LOVE_BALL].
     * Boosts the catch rate if the Pokémon is of the same species and opposite gender
     */
    val LOVE = BattleModifier { currentCatchRate, _, playerPokemon, pokemon ->
        if (
            pokemon.gender != Gender.GENDERLESS
            && playerPokemon.mapNotNull { it.battlePokemon?.originalPokemon }
                .any {
                    it.gender != Gender.GENDERLESS &&
                            it.species.name.equals(pokemon.species.name, true) &&
                            it.gender != pokemon.gender
                }
        ) {
            currentCatchRate * (if (playerPokemon.any { it.battlePokemon?.originalPokemon?.species?.resourceIdentifier == pokemon.species.resourceIdentifier }) 8F else 2.5F)
        }
        else {
            currentCatchRate
        }
    }

    /**
     * Used by [PokeBalls.MOON_BALL].
     * Boosts the catch rate 1.5× during Moon Phases 3 and 7 between the times of 12000 – 24000.
     * 2.5× during Moon Phases 2 and 8 between the times of 12000 – 24000.
     * 4× during Moon Phase 1 between the times of 12000 – 24000.
     * 1× otherwise.
     */
    val MOON_PHASES: CatchRateModifier = WorldStateModifier { currentCatchRate, _, entity ->
        if (entity.world.time in 12000..24000)
            return@WorldStateModifier currentCatchRate
        when (entity.world.moonPhase) {
            3, 7 -> currentCatchRate * 1.5F
            2, 8 -> currentCatchRate * 2.5F
            1 -> currentCatchRate * 4F
            else -> currentCatchRate
        }
    }

    /**
     * Used by [PokeBalls.DUSK_BALL].
     * Boosts the catch rate by various values depending on the light level.
     * *3 on light level of 0.
     * *1.5 on light level of 7 or lower, excluding 0.
     * *1 otherwise
     */
    val LIGHT_LEVEL = WorldStateModifier { currentCatchRate, _, entity ->
        when (entity.world.getLightLevel(entity.blockPos)) {
            0 -> currentCatchRate * 3F
            in 1..7 -> currentCatchRate * 1.5F
            else -> currentCatchRate
        }
    }

    /**
     * Used by [PokeBalls.SAFARI_BALL].
     * Checks if the target is not battling, if true boost by *1.5
     */
    val SAFARI = WorldStateModifier { currentCatchRate, _, entity -> if (!entity.isBattling) currentCatchRate * 1.5F else currentCatchRate }

    /**
     * Used by [PokeBalls.PARK_BALL].
     * Checks if the entity is in a biome valid for the tag [CobblemonBiomeTags.IS_TEMPERATE].
     * If yes boosts the catch rate by *2.5
     */
    val PARK = WorldStateModifier { currentCatchRate, _, entity ->
        if (entity.world.getBiome(entity.blockPos).isIn(CobblemonBiomeTags.IS_TEMPERATE))
            currentCatchRate * 2.5F
        else
            currentCatchRate
    }

    /**
     * Used by [PokeBalls.HEAVY_BALL].
     * The base implementation of the heavy ball modifier mechanics.
     */
    val WEIGHT_BASED: CatchRateModifier = BasicCatchRateModifier { currentCatchRate, _, pokemon, _ ->
        // Remember we use hectograms not kilograms
        when {
            pokemon.form.weight >= 3000F -> currentCatchRate * 4F
            pokemon.form.weight in 2000F..2999F -> currentCatchRate * 2.5F
            pokemon.form.weight in 1000F..1999F -> currentCatchRate * 1.5F
            else -> currentCatchRate
        }
    }

    /**
     * Used by [PokeBalls.NET_BALL].
     * Boosts the catch rate if the target is of the given types.
     *
     * @param multiplier The multiplier to be applied if the target has any of the accepted [types].
     * @param types The [ElementalType]s that will trigger the multiplier.
     * @return The multiplier modifier.
     */
    fun typeBoosting(multiplier: Float, vararg types: ElementalType): CatchRateModifier = BasicCatchRateModifier { currentCatchRate, _, pokemon, host ->
        return@BasicCatchRateModifier if (pokemon.form.types.any { types.contains(it) }) currentCatchRate * multiplier else currentCatchRate
    }

    /**
     * Used by [PokeBalls.DREAM_BALL].
     * Boosts the catch rate if the target has any of the given status conditions.
     *
     * @param multiplier The multiplier to be applied if the target has any of the accepted [status].
     * @param status The [Status]' that will trigger the multiplier.
     * @return The multiplier modifier.
     */
    fun statusBoosting(multiplier: Float, vararg status: Status): CatchRateModifier = BasicCatchRateModifier { currentCatchRate, _, pokemon, host ->
        val pokemonStatus = pokemon.status?.status ?: return@BasicCatchRateModifier currentCatchRate
        return@BasicCatchRateModifier if (status.contains(pokemonStatus)) currentCatchRate * multiplier else currentCatchRate
    }

    /**
     * Used by [PokeBalls.QUICK_BALL] and [PokeBalls.TIMER_BALL].
     * Boosts the catch rate based on the number of turns a battle has lasted.
     *
     * @param multiplierCalculator Resolves the multiplier based on the number of turns.
     * @return The multiplier modifier.
     */
    fun turnBased(multiplierCalculator: (turn: Int) -> Float) = BattleModifier { currentCatchRate, player, playerPokemon, pokemon ->
        val battle = BattleRegistry.getBattleByParticipatingPlayer(player) ?: return@BattleModifier currentCatchRate
        multiplierCalculator.invoke(battle.turn)
    }

    private class BasicCatchRateModifier(private val calculator: (currentCatchRate: Float, thrower: LivingEntity, pokemon: Pokemon, host: Pokemon?) -> Float) : CatchRateModifier {

        override fun modifyCatchRate(currentCatchRate: Float, thrower: LivingEntity, pokemon: Pokemon, host: Pokemon?) = this.calculator(currentCatchRate, thrower, pokemon, host)

    }

}
