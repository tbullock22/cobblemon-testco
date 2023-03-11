/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage.pc.link

import com.cobblemon.mod.common.api.storage.pc.PCStore
import com.cobblemon.mod.common.util.toVec3d
import com.cobblemon.mod.common.block.entity.PCBlockEntity
import java.util.UUID
import net.minecraft.server.network.ServerPlayerEntity

/**
 * A [PCLink] that is based on proximity to a specific PC. If the PC is broken or the player is moved
 * beyond [maxDistance] from it, they will not be able to make PC changes to this linked PC anymore and
 * the next attempt will actively remove the link.
 *
 * @author Hiroku
 * @since June 20th, 2022
 */
class ProximityPCLink(
    pc: PCStore,
    playerID: UUID,
    pcBlockEntity: PCBlockEntity,
    val maxDistance: Double = 10.0
) : PCLink(pc, playerID) {
    val world = pcBlockEntity.world
    val pos = pcBlockEntity.pos

    override fun isPermitted(player: ServerPlayerEntity): Boolean {
        val isWithinRange = player.world == world && player.pos.isInRange(pos.toVec3d(), maxDistance)
        val pcStillStanding = player.world.getBlockEntity(pos) is PCBlockEntity
        if (!isWithinRange || !pcStillStanding) {
            PCLinkManager.removeLink(playerID)
        }
        return isWithinRange && pcStillStanding
    }
}