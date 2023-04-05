/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.effect

import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.client.net.ClientPacketHandler
import com.cobblemon.mod.common.client.particle.ParticleStorm
import com.cobblemon.mod.common.client.render.MatrixWrapper
import com.cobblemon.mod.common.net.messages.client.effect.SpawnSnowstormParticlePacket
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Vec3f.POSITIVE_X
import net.minecraft.util.math.Vec3f.POSITIVE_Y

object SpawnSnowstormParticleHandler : ClientPacketHandler<SpawnSnowstormParticlePacket> {
    override fun invokeOnClient(packet: SpawnSnowstormParticlePacket, ctx: CobblemonNetwork.NetworkContext) {
        val wrapper = MatrixWrapper()
        val matrix = MatrixStack()
        matrix.translate(packet.position.x, packet.position.y, packet.position.z)
        matrix.multiply(POSITIVE_Y.getDegreesQuaternion(packet.yawDegrees))
        matrix.multiply(POSITIVE_X.getDegreesQuaternion(packet.pitchDegrees))
        wrapper.updateMatrix(matrix.peek().positionMatrix)
        val world = MinecraftClient.getInstance().world ?: return
        ParticleStorm(packet.effect, wrapper, world).spawn()
    }
}