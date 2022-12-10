/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.forge

import com.cobblemon.mod.common.*
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.reactive.Observable.Companion.filter
import com.cobblemon.mod.common.api.reactive.Observable.Companion.takeFirst
import com.cobblemon.mod.forge.net.CobblemonForgeNetworkDelegate
import com.cobblemon.mod.forge.permission.ForgePermissionValidator
import dev.architectury.event.events.common.LifecycleEvent
import dev.architectury.platform.forge.EventBuses
import java.util.*
import net.minecraftforge.common.ForgeMod
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.OnDatapackSyncEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext

@Mod(Cobblemon.MODID)
class CobblemonForge : CobblemonImplementation {
    init {
        with(FMLJavaModLoadingContext.get().modEventBus) {
            EventBuses.registerModEventBus(Cobblemon.MODID, this)

            CobblemonEvents.ENTITY_ATTRIBUTE.pipe(filter { it.entityType == CobblemonEntities.POKEMON.get() }, takeFirst())
                .subscribe {
                    it.attributeSupplier
                        .add(ForgeMod.ENTITY_GRAVITY.get())
                        .add(ForgeMod.NAMETAG_DISTANCE.get())
                        .add(ForgeMod.SWIM_SPEED.get())
                }

            addListener(this@CobblemonForge::initialize)
            addListener(this@CobblemonForge::serverInit)
            CobblemonNetwork.networkDelegate = CobblemonForgeNetworkDelegate

            Cobblemon.preinitialize(this@CobblemonForge)

            LifecycleEvent.SETUP.register {
                CobblemonConfiguredFeatures.register()
                CobblemonPlacements.register()
            }

            // TODO: Make listener for BiomeLoadingEvent to register feature to biomes
        }
        with(MinecraftForge.EVENT_BUS) {
            addListener(this@CobblemonForge::onDataPackSync)
            addListener(this@CobblemonForge::onLogin)
            addListener(this@CobblemonForge::onLogout)
        }
        Cobblemon.permissionValidator = ForgePermissionValidator
    }

    fun serverInit(event: FMLDedicatedServerSetupEvent) {
    }

    fun initialize(event: FMLCommonSetupEvent) {
        Cobblemon.LOGGER.info("Initializing...")
        Cobblemon.initialize()
        //if (ModList.get().isLoaded("luckperms")) { PokemonCobblemon.permissionValidator = LuckPermsPermissionValidator() }
        //else {
        //}
    }

    private val hasBeenSynced = hashSetOf<UUID>()

    fun onDataPackSync(event: OnDatapackSyncEvent) {
        Cobblemon.dataProvider.sync(event.player ?: return)
    }

    fun onLogin(event: PlayerEvent.PlayerLoggedInEvent) {
        this.hasBeenSynced.add(event.entity.uuid)
    }

    fun onLogout(event: PlayerEvent.PlayerLoggedOutEvent) {
        this.hasBeenSynced.remove(event.entity.uuid)
    }

    override fun isModInstalled(id: String) = ModList.get().isLoaded(id)
}