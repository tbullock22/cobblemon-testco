/*
 * Copyright (C) 2023 Cobblemon Contributors
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
import com.cobblemon.mod.common.util.didSleep
import com.cobblemon.mod.forge.compat.AdornForgeCompat
import com.cobblemon.mod.forge.net.CobblemonForgeNetworkDelegate
import com.cobblemon.mod.forge.permission.ForgePermissionValidator
import dev.architectury.event.events.common.LifecycleEvent
import dev.architectury.platform.forge.EventBuses
import juuxel.adorn.compat.CompatBlocks
import java.util.*
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraftforge.common.ForgeMod
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.OnDatapackSyncEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent
import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent

@Mod(Cobblemon.MODID)
class CobblemonForge : CobblemonImplementation {
    override val modAPI = ModAPI.FABRIC
    private val hasBeenSynced = hashSetOf<UUID>()


    private val modCompat = mapOf(
        "adorn" to {
            CompatBlocks.addVariants(AdornForgeCompat)
            CompatBlocks.register()
        }
    )

    init {
        with(thedarkcolour.kotlinforforge.forge.MOD_BUS) {
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

            Cobblemon.preInitialize(this@CobblemonForge)

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
            addListener(this@CobblemonForge::wakeUp)
        }
        Cobblemon.permissionValidator = ForgePermissionValidator
        this.modCompat.forEach { (modId, compatSupplier) ->
            if (this.isModInstalled(modId)) {
                compatSupplier.invoke()
            }
        }
    }

    fun wakeUp(event: PlayerWakeUpEvent) {
        val playerEntity = event.entity as? ServerPlayerEntity ?: return
        playerEntity.didSleep()
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