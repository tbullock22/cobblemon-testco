/*
 * Copyright (C) 2022 Pokemon Cobbled Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cablemc.pokemod.common.events

import com.cablemc.pokemod.common.Pokemod
import com.cablemc.pokemod.common.battles.BattleRegistry
import com.cablemc.pokemod.common.client.visual.Visuals
import com.cablemc.pokemod.common.util.party
import net.minecraft.server.MinecraftServer

object ServerTickHandler {
    private var secondsTick = 0

    fun onTick(server: MinecraftServer) {
        Pokemod.bestSpawner.spawnerManagers.forEach { it.onServerTick() }
        Visuals.tick()
        BattleRegistry.tick()

        secondsTick++

        if (secondsTick == 20) {
            secondsTick = 0

            // Party tick
            for (player in server.playerManager.playerList) {
                player.party().onSecondPassed(player)
            }
        }
    }
}