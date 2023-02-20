/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.fabric

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonConfiguredFeatures
import com.cobblemon.mod.common.CobblemonImplementation
import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.CobblemonPlacements
import com.cobblemon.mod.common.ModAPI
import com.cobblemon.mod.common.util.didSleep
import com.cobblemon.mod.fabric.net.CobblemonFabricNetworkDelegate
import com.cobblemon.mod.fabric.permission.FabricPermissionValidator
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.network.ServerPlayerEntity

object CobblemonFabric : CobblemonImplementation {
    override val modAPI = ModAPI.FABRIC
    override fun isModInstalled(id: String) = FabricLoader.getInstance().isModLoaded(id)
    fun initialize() {
        CobblemonNetwork.networkDelegate = CobblemonFabricNetworkDelegate
        Cobblemon.preInitialize(this)

        CobblemonConfiguredFeatures.register()
        CobblemonPlacements.register()

        Cobblemon.initialize()
        /*
        if (FabricLoader.getInstance().getModContainer("luckperms").isPresent) {
            Cobblemon.permissionValidator = LuckPermsPermissionValidator()
        }
         */
        if (FabricLoader.getInstance().getModContainer("fabric-permissions-api-v0").isPresent) {
            Cobblemon.permissionValidator = FabricPermissionValidator()
        }
        EntitySleepEvents.STOP_SLEEPING.register { playerEntity, _ ->
            if (playerEntity !is ServerPlayerEntity) {
                return@register
            }

            playerEntity.didSleep()
        }

        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register { player, isLogin ->
            if (isLogin) {
                Cobblemon.dataProvider.sync(player)
            }
        }
    }
}