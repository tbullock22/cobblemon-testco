/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events

import com.cobblemon.mod.common.api.events.battles.BattleVictoryEvent
import com.cobblemon.mod.common.api.events.drops.LootDroppedEvent
import com.cobblemon.mod.common.api.events.entity.EntityAttributeEvent
import com.cobblemon.mod.common.api.events.net.MessageBuiltEvent
import com.cobblemon.mod.common.api.events.pokemon.ExperienceGainedPostEvent
import com.cobblemon.mod.common.api.events.pokemon.ExperienceGainedPreEvent
import com.cobblemon.mod.common.api.events.pokemon.FriendshipUpdatedEvent
import com.cobblemon.mod.common.api.events.pokemon.LevelUpEvent
import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent
import com.cobblemon.mod.common.api.events.pokemon.PokemonFaintedEvent
import com.cobblemon.mod.common.api.events.pokemon.ShoulderMountEvent
import com.cobblemon.mod.common.api.events.pokemon.evolution.EvolutionAcceptedEvent
import com.cobblemon.mod.common.api.events.pokemon.evolution.EvolutionCompleteEvent
import com.cobblemon.mod.common.api.events.pokemon.evolution.EvolutionDisplayEvent
import com.cobblemon.mod.common.api.events.pokemon.interaction.ExperienceCandyUseEvent
import com.cobblemon.mod.common.api.events.starter.StarterChosenEvent
import com.cobblemon.mod.common.api.reactive.CancelableObservable
import com.cobblemon.mod.common.api.reactive.EventObservable
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.util.asObservable
import com.cobblemon.mod.common.util.asServerObservable
import com.cobblemon.mod.common.util.asTickObservable
import dev.architectury.event.EventResult
import dev.architectury.event.events.common.EntityEvent
import dev.architectury.event.events.common.EntityEvent.LivingDeath
import dev.architectury.event.events.common.LifecycleEvent
import dev.architectury.event.events.common.PlayerEvent
import dev.architectury.event.events.common.PlayerEvent.PlayerJoin
import dev.architectury.event.events.common.PlayerEvent.PlayerQuit
import dev.architectury.event.events.common.TickEvent
import net.minecraft.entity.LivingEntity
import net.minecraft.server.network.ServerPlayerEntity

object CobblemonEvents {
    val MESSAGE_BUILT = EventObservable<MessageBuiltEvent<*>>()
    val DATA_SYNCHRONIZED = SimpleObservable<ServerPlayerEntity>()
    val ENTITY_ATTRIBUTE = EventObservable<EntityAttributeEvent>()
    val SHOULDER_MOUNT = CancelableObservable<ShoulderMountEvent>()
    val FRIENDSHIP_UPDATED = EventObservable<FriendshipUpdatedEvent>()
    val POKEMON_FAINTED = EventObservable<PokemonFaintedEvent>()
    val EVOLUTION_ACCEPTED = CancelableObservable<EvolutionAcceptedEvent>()
    val EVOLUTION_DISPLAY = EventObservable<EvolutionDisplayEvent>()
    val EVOLUTION_COMPLETE = EventObservable<EvolutionCompleteEvent>()

    val POKEMON_CAPTURED = EventObservable<PokemonCapturedEvent>()
//    val EGG_HATCH = EventObservable<HatchEggEvent>()
    val BATTLE_VICTORY = EventObservable<BattleVictoryEvent>()

    val LEVEL_UP_EVENT = EventObservable<LevelUpEvent>()

    val EXPERIENCE_GAINED_EVENT_PRE = CancelableObservable<ExperienceGainedPreEvent>()
    val EXPERIENCE_GAINED_EVENT_POST = EventObservable<ExperienceGainedPostEvent>()
    val EXPERIENCE_CANDY_USE_PRE = CancelableObservable<ExperienceCandyUseEvent.Pre>()
    val EXPERIENCE_CANDY_USE_POST = EventObservable<ExperienceCandyUseEvent.Post>()

    val LOOT_DROPPED = CancelableObservable<LootDroppedEvent>()
    val STARTER_CHOSEN = CancelableObservable<StarterChosenEvent>()

    val SERVER_STARTING = LifecycleEvent.SERVER_STARTING.asServerObservable()
    val SERVER_STOPPING = LifecycleEvent.SERVER_STOPPING.asServerObservable()

    val SERVER_STARTED = LifecycleEvent.SERVER_STARTED.asServerObservable()
    val SERVER_STOPPED = LifecycleEvent.SERVER_STOPPED.asServerObservable()

    val TICK_PRE = TickEvent.SERVER_PRE.asTickObservable()
    val TICK_POST = TickEvent.SERVER_POST.asTickObservable()

    val PLAYER_JOIN = PlayerEvent.PLAYER_JOIN.asObservable<PlayerJoin, ServerPlayerEntity> { obs -> PlayerJoin { obs.emit(it) } }
    val PLAYER_QUIT = PlayerEvent.PLAYER_QUIT.asObservable<PlayerQuit, ServerPlayerEntity> { obs -> PlayerQuit { obs.emit(it) } }

    val LIVING_DEATH = EntityEvent.LIVING_DEATH.asObservable<LivingDeath, LivingEntity> { obs -> LivingDeath { entity, source -> obs.emit(entity); EventResult.pass() } }
}