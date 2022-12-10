/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokeball

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.api.data.JsonDataRegistry
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.pokeball.catching.CaptureEffect
import com.cobblemon.mod.common.api.pokeball.catching.CatchRateModifier
import com.cobblemon.mod.common.api.pokeball.catching.effects.CaptureEffects
import com.cobblemon.mod.common.api.pokeball.catching.effects.FriendshipEarningBoostEffect
import com.cobblemon.mod.common.api.pokeball.catching.modifiers.*
import com.cobblemon.mod.common.api.pokemon.labels.CobblemonPokemonLabels
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.api.pokemon.status.Statuses
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.api.types.ElementalTypes
import com.cobblemon.mod.common.pokeball.PokeBall
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.minecraft.resource.ResourceType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import java.io.File
import kotlin.io.path.Path
import kotlin.math.roundToInt

/**
 * The data registry for [PokeBall]s.
 * All the pokeball fields are guaranteed to exist
 */
object PokeBalls : JsonDataRegistry<PokeBall> {

    override val id = cobblemonResource("pokeballs")
    override val type = ResourceType.SERVER_DATA
    override val observable = SimpleObservable<PokeBalls>()

    // ToDo once datapack pokeball is implemented add required adapters here
    override val gson: Gson = GsonBuilder()
        .disableHtmlEscaping()
        .setPrettyPrinting()
        .create()
    override val typeToken: TypeToken<PokeBall> = TypeToken.get(PokeBall::class.java)
    override val resourcePath = "pokeballs"

    private val defaults = hashMapOf<Identifier, PokeBall>()
    // ToDo datapack pokeball type here instead
    private val custom = hashMapOf<Identifier, PokeBall>()

    val POKE_BALL
        get() = this.byName("poke_ball")
    val SLATE_BALL
        get() = this.byName("slate_ball")
    val AZURE_BALL
        get() = this.byName("azure_ball")
    val VERDANT_BALL
        get() = this.byName("verdant_ball")
    val ROSEATE_BALL
        get() = this.byName("roseate_ball")
    val CITRINE_BALL
        get() = this.byName("citrine_ball")
    val GREAT_BALL
        get() = this.byName("great_ball")
    val ULTRA_BALL
        get() = this.byName("ultra_ball")
    val MASTER_BALL
        get() = this.byName("master_ball")
    val SAFARI_BALL
        get() = this.byName("safari_ball")
    val FAST_BALL
        get() = this.byName("fast_ball")
    val LEVEL_BALL
        get() = this.byName("level_ball")
    val LURE_BALL
        get() = this.byName("lure_ball")
    val HEAVY_BALL
        get() = this.byName("heavy_ball")
    val LOVE_BALL
        get() = this.byName("love_ball")
    val FRIEND_BALL
        get() = this.byName("friend_ball")
    val MOON_BALL
        get() = this.byName("moon_ball")
    val SPORT_BALL
        get() = this.byName("sport_ball")
    val NET_BALL
        get() = this.byName("net_ball")
    val DIVE_BALL
        get() = this.byName("dive_ball")
    val NEST_BALL
        get() = this.byName("nest_ball")
    val REPEAT_BALL
        get() = this.byName("repeat_ball")
    val TIMER_BALL
        get() = this.byName("timer_ball")
    val LUXURY_BALL
        get() = this.byName("luxury_ball")
    val PREMIER_BALL
        get() = this.byName("premier_ball")
    val DUSK_BALL
        get() = this.byName("dusk_ball")
    val HEAL_BALL
        get() = this.byName("heal_ball")
    val QUICK_BALL
        get() = this.byName("quick_ball")
    val CHERISH_BALL
        get() = this.byName("cherish_ball")
    val PARK_BALL
        get() = this.byName("park_ball")
    val DREAM_BALL
        get() = this.byName("dream_ball")
    val BEAST_BALL
        get() = this.byName("beast_ball")

    init {
        createDefault("poke_ball")
        createDefault("slate_ball")
        createDefault("azure_ball")
        createDefault("verdant_ball")
        createDefault("roseate_ball")
        createDefault("citrine_ball")
        createDefault("great_ball", listOf(MultiplierModifier(1.5F)))
        createDefault("ultra_ball", listOf(MultiplierModifier(2F)))
        createDefault("master_ball", listOf(GuaranteedModifier()), appendUltraBeastPenalty = false)
        createDefault("safari_ball", listOf(CatchRateModifiers.SAFARI))
        createDefault("fast_ball", listOf(BaseStatModifier(Stats.SPEED, { it >= 100 }, 4F)))
        createDefault("level_ball", listOf(CatchRateModifiers.LEVEL))
        // ToDo we will need fishing context here once fishing is implemented for a multiplier
        createDefault("lure_ball", listOf(CatchRateModifiers.typeBoosting(2F, ElementalTypes.WATER)))
        createDefault("heavy_ball", listOf(CatchRateModifiers.WEIGHT_BASED))
        createDefault("love_ball", listOf(CatchRateModifiers.LOVE))
        createDefault("friend_ball", effects = listOf(CaptureEffects.friendshipSetter(150)))
        createDefault("moon_ball", listOf(CatchRateModifiers.MOON_PHASES))
        createDefault("sport_ball", listOf(MultiplierModifier(1.5F)))
        createDefault("net_ball", listOf(CatchRateModifiers.typeBoosting(3F, ElementalTypes.BUG, ElementalTypes.WATER)))
        createDefault("dive_ball", listOf(CatchRateModifiers.SUBMERGED_IN_WATER))
        createDefault("nest_ball", listOf(CatchRateModifiers.NEST))
        // ToDo implement effect once pokedex is implemented, we have a custom multiplier of 2.5 instead of the official pokeball
        createDefault("repeat_ball")
        createDefault("timer_ball", listOf(CatchRateModifiers.turnBased { turn -> (1F * turn * (1229F / 4096F)).coerceAtMost(4F) }))
        createDefault("luxury_ball", effects = listOf(FriendshipEarningBoostEffect(2F)))
        createDefault("premier_ball")
        createDefault("dusk_ball", listOf(CatchRateModifiers.LIGHT_LEVEL))
        createDefault("heal_ball", effects = listOf(CaptureEffects.FULL_RESTORE))
        createDefault("quick_ball", listOf(CatchRateModifiers.turnBased { turn -> if (turn == 1) 5F else 1F }))
        createDefault("cherish_ball")
        createDefault("park_ball", listOf(CatchRateModifiers.PARK))
        createDefault("dream_ball", listOf(CatchRateModifiers.statusBoosting(4F, Statuses.SLEEP)))
        createDefault("beast_ball", listOf(LabelModifier(5F, true, CobblemonPokemonLabels.ULTRA_BEAST), LabelModifier(0.1F, false, CobblemonPokemonLabels.ULTRA_BEAST)), appendUltraBeastPenalty = false)
        // Luxury ball effect
        CobblemonEvents.FRIENDSHIP_UPDATED.subscribe(priority = Priority.LOWEST) { event ->
            var increment = (event.newFriendship - event.pokemon.friendship).toFloat()
            if (increment <= 1F) {
                event.pokemon.caughtBall.effects.filterIsInstance<FriendshipEarningBoostEffect>()
                    .forEach { increment *= it.multiplier }
            }
            event.newFriendship = event.pokemon.friendship + increment.roundToInt()
        }
    }

    override fun reload(data: Map<Identifier, PokeBall>) {
        this.custom.clear()
        // ToDo once datapack pokeball is implemented load them here, we will want datapacks to be able to override our default pokeballs too, however they will never be able to disable them
    }

    override fun sync(player: ServerPlayerEntity) {
        // ToDo once datapack pokeball is implemented sync them here
    }

    /**
     * Gets a Pokeball from registry name.
     * @return the pokeball object if found otherwise null.
     */
    fun getPokeBall(name : Identifier): PokeBall? = this.custom[name] ?: this.defaults[name]

    fun all() = this.defaults.filterKeys { !this.custom.containsKey(it) }.values + this.custom.values

    private fun createDefault(
        name: String,
        modifiers: List<CatchRateModifier> = emptyList(),
        effects: List<CaptureEffect> = emptyList(),
        model2d: String = "${Cobblemon.MODID}:${name}#inventory",
        model3d: String = "${Cobblemon.MODID}:${name}_in_hand#inventory",
        appendUltraBeastPenalty: Boolean = true
    ): PokeBall {
        val identifier = cobblemonResource(name)
        val finalModifiers = if (appendUltraBeastPenalty) modifiers + listOf(LabelModifier(0.1F, true, CobblemonPokemonLabels.ULTRA_BEAST)) else modifiers
        val pokeball = PokeBall(identifier, finalModifiers, effects, model2d, model3d)
        this.defaults[identifier] = pokeball
        return pokeball
    }

    private fun byName(name: String): PokeBall {
        val identifier = cobblemonResource(name)
        return this.custom[identifier] ?: this.defaults[identifier]!!
    }

}