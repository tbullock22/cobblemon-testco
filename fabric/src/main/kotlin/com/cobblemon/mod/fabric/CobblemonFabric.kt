/*
 * Copyright (C) 2022 Cobblemon Contributors
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
import com.cobblemon.mod.fabric.compat.AdornFabricCompat
import com.cobblemon.mod.fabric.net.CobblemonFabricNetworkDelegate
import com.cobblemon.mod.fabric.permission.FabricPermissionValidator
import juuxel.adorn.compat.CompatBlocks
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader

object CobblemonFabric : CobblemonImplementation {

    private val modCompat = mapOf(
        "adorn" to {
            CompatBlocks.addVariants(AdornFabricCompat)
            CompatBlocks.register()
        },
        "fabric-permissions-api-v0" to { Cobblemon.permissionValidator = FabricPermissionValidator() }
    )

    override fun isModInstalled(id: String) = FabricLoader.getInstance().isModLoaded(id)
    fun initialize() {
        CobblemonNetwork.networkDelegate = CobblemonFabricNetworkDelegate
        Cobblemon.preinitialize(this)

        CobblemonConfiguredFeatures.register()
        CobblemonPlacements.register()

        Cobblemon.initialize()
        /*
        if (FabricLoader.getInstance().getModContainer("luckperms").isPresent) {
            Cobblemon.permissionValidator = LuckPermsPermissionValidator()
        }
         */
        this.modCompat.forEach { (modId, compatSupplier) ->
            if (this.isModInstalled(modId)) {
                compatSupplier.invoke()
            }
        }
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register { player, isLogin ->
            if (isLogin) {
                Cobblemon.dataProvider.sync(player)
            }
        }
    }
}