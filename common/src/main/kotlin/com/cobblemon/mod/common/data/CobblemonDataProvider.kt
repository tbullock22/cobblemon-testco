/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.data

import com.cobblemon.mod.common.Cobblemon.LOGGER
import com.cobblemon.mod.common.api.abilities.Abilities
import com.cobblemon.mod.common.api.data.DataProvider
import com.cobblemon.mod.common.api.data.DataRegistry
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.moves.Moves
import com.cobblemon.mod.common.api.pokeball.PokeBalls
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.pokemon.feature.GlobalSpeciesFeatures
import com.cobblemon.mod.common.api.pokemon.feature.SpeciesFeatureAssignments
import com.cobblemon.mod.common.api.pokemon.feature.SpeciesFeatures
import com.cobblemon.mod.common.api.spawning.CobblemonSpawnPools
import com.cobblemon.mod.common.api.spawning.SpawnDetailPresets
import com.cobblemon.mod.common.net.messages.client.data.UnlockReloadPacket
import com.cobblemon.mod.common.pokemon.SpeciesAdditions
import com.cobblemon.mod.common.pokemon.properties.PropertiesCompletionProvider
import com.cobblemon.mod.common.util.getServer
import com.cobblemon.mod.common.util.ifClient
import dev.architectury.registry.ReloadListenerRegistry
import java.util.UUID
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceType
import net.minecraft.resource.SynchronousResourceReloader
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier

object CobblemonDataProvider : DataProvider {

    // Both Forge n Fabric keep insertion order so if a registry depends on another simply register it after
    internal var canReload = true
    // Both Forge n Fabric keep insertion order so if a registry depends on another simply register it after
    private val registries = mutableListOf<DataRegistry>()
    private val synchronizedPlayerIds = mutableListOf<UUID>()

    private val scheduledActions = mutableMapOf<UUID, MutableList<() -> Unit>>()

    fun registerDefaults() {
        this.register(SpeciesFeatures)
        this.register(GlobalSpeciesFeatures)
        this.register(SpeciesFeatureAssignments)
        this.register(Moves)
        this.register(Abilities)
        this.register(PokemonSpecies)
        this.register(SpeciesAdditions)
        this.register(PokeBalls)
        this.register(PropertiesCompletionProvider)
        this.register(SpawnDetailPresets)

        CobblemonSpawnPools.load()

        ifClient(){
            ReloadListenerRegistry.register(ResourceType.CLIENT_RESOURCES, SimpleResourceReloader(ResourceType.CLIENT_RESOURCES))
        }
        ReloadListenerRegistry.register(ResourceType.SERVER_DATA, SimpleResourceReloader(ResourceType.SERVER_DATA))

        CobblemonEvents.PLAYER_QUIT.subscribe {
            UnlockReloadPacket().sendToPlayer(it)
            synchronizedPlayerIds.remove(it.uuid)
        }
    }

    override fun <T : DataRegistry> register(registry: T): T {
        // Only send message once
        if (this.registries.isEmpty()) {
            LOGGER.info("Note: Cobblemon data registries are only loaded once per server instance as Pokémon species are not safe to reload.")
        }
        this.registries.add(registry)
        LOGGER.info("Registered the {} registry", registry.id.toString())
        LOGGER.debug("Registered the {} registry of class {}", registry.id.toString(), registry::class.qualifiedName)
        return registry
    }

    override fun fromIdentifier(registryIdentifier: Identifier): DataRegistry? = this.registries.find { it.id == registryIdentifier }

    override fun sync(player: ServerPlayerEntity) {
        if (!player.networkHandler.connection.isLocal) {
            this.registries.forEach { registry -> registry.sync(player) }
        }

        CobblemonEvents.DATA_SYNCHRONIZED.emit(player)
        val waitingActions = this.scheduledActions.remove(player.uuid) ?: return
        waitingActions.forEach { it() }
    }

    override fun doAfterSync(player: ServerPlayerEntity, action: () -> Unit) {
        if (player.uuid in synchronizedPlayerIds) {
            action()
        } else {
            this.scheduledActions.computeIfAbsent(player.uuid) { mutableListOf() }.add(action)
        }
    }

    private class SimpleResourceReloader(private val type: ResourceType) : SynchronousResourceReloader {
        override fun reload(manager: ResourceManager) {
            // Check for a server running, this is due to the create a world screen triggering datapack reloads, these are fine to happen as many times as needed as players may be in the process of adding their datapacks.
            val isInGame = getServer() != null
            if (isInGame && this.type == ResourceType.SERVER_DATA && !canReload) {
                return
            }
            registries.filter { it.type == this.type }
                .forEach { it.reload(manager) }
            if (isInGame && this.type == ResourceType.SERVER_DATA) {
                canReload = false
            }
        }
    }
}
