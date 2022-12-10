/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.entity.EntityAttributeEvent
import com.cobblemon.mod.common.api.pokeball.PokeBalls
import com.cobblemon.mod.common.entity.pokeball.EmptyPokeBallEntity
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.registry.CompletableRegistry
import com.cobblemon.mod.common.util.cobblemonResource
import dev.architectury.registry.level.entity.EntityAttributeRegistry
import dev.architectury.registry.registries.RegistrySupplier
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.SpawnGroup
import net.minecraft.entity.attribute.DefaultAttributeContainer
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.util.registry.Registry

object CobblemonEntities : CompletableRegistry<EntityType<*>>(Registry.ENTITY_TYPE_KEY) {
    override fun register() {
        super.register()

        EntityAttributeRegistry.register(
            { POKEMON.get() },
            {
                LivingEntity.createLivingAttributes()
                    .add(EntityAttributes.GENERIC_FOLLOW_RANGE)
                    .also { CobblemonEvents.ENTITY_ATTRIBUTE.post(EntityAttributeEvent(POKEMON.get(), it)) }
            }
        )
    }
    private fun <T : Entity> entity(
        name: String,
        entityTypeBuilder: EntityType.Builder<T>
    ): RegistrySupplier<EntityType<T>> {
        return queue(name) { entityTypeBuilder.build(cobblemonResource(name).toString()) }
    }

    private fun <T : LivingEntity> livingEntity(
        name: String,
        entityTypeBuilder: EntityType.Builder<T>
    ): RegistrySupplier<EntityType<T>> {
        return queue(name) { entityTypeBuilder.build(cobblemonResource("pokemon").toString()) }
    }

    val POKEMON = livingEntity(
        name = "pokemon",
        entityTypeBuilder = EntityType.Builder.create<PokemonEntity>(
            { _, world -> PokemonEntity(world) },
            SpawnGroup.CREATURE
        )
    )

    val EMPTY_POKEBALL = entity(
        name = "empty_pokeball",
        entityTypeBuilder = EntityType.Builder.create<EmptyPokeBallEntity>(
            { _, world -> EmptyPokeBallEntity(PokeBalls.POKE_BALL, world) },
            SpawnGroup.MISC
        )
    )
}