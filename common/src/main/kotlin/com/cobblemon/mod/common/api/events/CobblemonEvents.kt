/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events

import com.cobblemon.mod.common.api.events.battles.BattleVictoryEvent
import com.cobblemon.mod.common.api.events.drops.LootDroppedEvent
import com.cobblemon.mod.common.api.events.entity.EntityAttributeEvent
import com.cobblemon.mod.common.api.events.entity.PokemonEntityLoadEvent
import com.cobblemon.mod.common.api.events.entity.PokemonEntitySaveEvent
import com.cobblemon.mod.common.api.events.entity.PokemonEntitySaveToWorldEvent
import com.cobblemon.mod.common.api.events.farming.ApricornHarvestEvent
import com.cobblemon.mod.common.api.events.item.LeftoversCreatedEvent
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
import com.cobblemon.mod.common.api.events.storage.ReleasePokemonEvent
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
    @JvmField
    val MESSAGE_BUILT = EventObservable<MessageBuiltEvent<*>>()
    @JvmField
    val DATA_SYNCHRONIZED = SimpleObservable<ServerPlayerEntity>()
    @JvmField
    val ENTITY_ATTRIBUTE = EventObservable<EntityAttributeEvent>()
    @JvmField
    val SHOULDER_MOUNT = CancelableObservable<ShoulderMountEvent>()
    @JvmField
    val FRIENDSHIP_UPDATED = EventObservable<FriendshipUpdatedEvent>()
    @JvmField
    val POKEMON_FAINTED = EventObservable<PokemonFaintedEvent>()
    @JvmField
    val EVOLUTION_ACCEPTED = CancelableObservable<EvolutionAcceptedEvent>()
    @JvmField
    val EVOLUTION_DISPLAY = EventObservable<EvolutionDisplayEvent>()
    @JvmField
    val EVOLUTION_COMPLETE = EventObservable<EvolutionCompleteEvent>()

    @JvmField
    val POKEMON_CAPTURED = EventObservable<PokemonCapturedEvent>()
//    @JvmField
//    val EGG_HATCH = EventObservable<HatchEggEvent>()
    @JvmField
    val BATTLE_VICTORY = EventObservable<BattleVictoryEvent>()

    @JvmField
    val LEVEL_UP_EVENT = EventObservable<LevelUpEvent>()

    @JvmField
    val POKEMON_ENTITY_SAVE = EventObservable<PokemonEntitySaveEvent>()
    @JvmField
    val POKEMON_ENTITY_LOAD = CancelableObservable<PokemonEntityLoadEvent>()
    @JvmField
    val POKEMON_ENTITY_SAVE_TO_WORLD = CancelableObservable<PokemonEntitySaveToWorldEvent>()

    @JvmField
    val EXPERIENCE_GAINED_EVENT_PRE = CancelableObservable<ExperienceGainedPreEvent>()
    @JvmField
    val EXPERIENCE_GAINED_EVENT_POST = EventObservable<ExperienceGainedPostEvent>()
    @JvmField
    val EXPERIENCE_CANDY_USE_PRE = CancelableObservable<ExperienceCandyUseEvent.Pre>()
    @JvmField
    val EXPERIENCE_CANDY_USE_POST = EventObservable<ExperienceCandyUseEvent.Post>()

    @JvmField
    val POKEMON_RELEASED_EVENT_PRE = CancelableObservable<ReleasePokemonEvent.Pre>()
    @JvmField
    val POKEMON_RELEASED_EVENT_POST = EventObservable<ReleasePokemonEvent.Post>()

    @JvmField
    val LOOT_DROPPED = CancelableObservable<LootDroppedEvent>()
    @JvmField
    val STARTER_CHOSEN = CancelableObservable<StarterChosenEvent>()

    @JvmField
    val APRICORN_HARVESTED = EventObservable<ApricornHarvestEvent>()
    @JvmField
    val LEFTOVERS_CREATED = CancelableObservable<LeftoversCreatedEvent>()

    @JvmField
    val SERVER_STARTING = LifecycleEvent.SERVER_STARTING.asServerObservable()
    @JvmField
    val SERVER_STOPPING = LifecycleEvent.SERVER_STOPPING.asServerObservable()

    @JvmField
    val SERVER_STARTED = LifecycleEvent.SERVER_STARTED.asServerObservable()
    @JvmField
    val SERVER_STOPPED = LifecycleEvent.SERVER_STOPPED.asServerObservable()

    @JvmField
    val TICK_PRE = TickEvent.SERVER_PRE.asTickObservable()
    @JvmField
    val TICK_POST = TickEvent.SERVER_POST.asTickObservable()

    @JvmField
    val PLAYER_JOIN = PlayerEvent.PLAYER_JOIN.asObservable<PlayerJoin, ServerPlayerEntity> { obs -> PlayerJoin { obs.emit(it) } }
    @JvmField
    val PLAYER_QUIT = PlayerEvent.PLAYER_QUIT.asObservable<PlayerQuit, ServerPlayerEntity> { obs -> PlayerQuit { obs.emit(it) } }

    @JvmField
    val LIVING_DEATH = EntityEvent.LIVING_DEATH.asObservable<LivingDeath, LivingEntity> { obs -> LivingDeath { entity, source -> obs.emit(entity); EventResult.pass() } }
}