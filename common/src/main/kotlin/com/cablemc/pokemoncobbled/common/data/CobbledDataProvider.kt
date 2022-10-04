/*
 * Copyright (C) 2022 Pokemon Cobbled Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cablemc.pokemoncobbled.common.data

import com.cablemc.pokemoncobbled.common.PokemonCobbled.LOGGER
import com.cablemc.pokemoncobbled.common.api.abilities.Abilities
import com.cablemc.pokemoncobbled.common.api.data.DataProvider
import com.cablemc.pokemoncobbled.common.api.data.DataRegistry
import com.cablemc.pokemoncobbled.common.api.moves.Moves
import com.cablemc.pokemoncobbled.common.api.pokemon.PokemonSpecies
import dev.architectury.registry.ReloadListenerRegistry
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.SynchronousResourceReloader
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier

internal object CobbledDataProvider : DataProvider {

    // Both Forge n Fabric keep insertion order so if a registry depends on another simply register it after
    var canReload = true
    private val registries = hashMapOf<Identifier, DataRegistry>()

    fun registerDefaults() {
        this.register(Moves)
        this.register(Abilities)
        this.register(PokemonSpecies)
    }

    override fun register(registry: DataRegistry) {
        // Only send message once
        if (this.registries.isEmpty()) {
            LOGGER.info("Note: Pokémon Cobbled data registries are only loaded once per server instance as Pokémon species are not safe to reload.")
        }
        ReloadListenerRegistry.register(registry.type, SimpleResourceReloader(registry))
        this.registries[registry.id] = registry
        LOGGER.info("Registered the {} registry", registry.id.toString())
        LOGGER.debug("Registered the {} registry of class {}", registry.id.toString(), registry::class.qualifiedName)
    }

    override fun fromIdentifier(registryIdentifier: Identifier): DataRegistry? = this.registries[registryIdentifier]

    override fun sync(player: ServerPlayerEntity) {
        this.registries.values.forEach { registry -> registry.sync(player) }
    }

    private class SimpleResourceReloader(private val registry: DataRegistry) : SynchronousResourceReloader {
        override fun reload(manager: ResourceManager) {
            if (canReload) {
                this.registry.reload(manager)
            }
        }
    }
}