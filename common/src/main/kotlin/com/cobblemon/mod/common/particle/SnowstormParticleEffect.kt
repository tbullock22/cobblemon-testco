/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.particle

import com.cobblemon.mod.common.api.snowstorm.BedrockParticleEffect
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.minecraft.network.PacketByteBuf
import net.minecraft.particle.ParticleEffect
import net.minecraft.particle.ParticleType

class SnowstormParticleEffect(val effect: BedrockParticleEffect) : ParticleEffect {

    override fun getType(): ParticleType<*> {
        return CobblemonParticles.SNOWSTORM_PARTICLE
    }

    override fun write(buf: PacketByteBuf) {
        effect.writeToBuffer(buf)
    }

    override fun asString(): String {
        return ""
    }

    companion object {
        val PARAMETERS_FACTORY: ParticleEffect.Factory<SnowstormParticleEffect> = object : ParticleEffect.Factory<SnowstormParticleEffect> {
            @Throws(CommandSyntaxException::class)
            override fun read(
                particleType: ParticleType<SnowstormParticleEffect>,
                stringReader: StringReader
            ): SnowstormParticleEffect {
                stringReader.expect(' ')
                // TODO load from file, probably.
                return SnowstormParticleEffect(
                    BedrockParticleEffect()
                )
            }

            override fun read(
                particleType: ParticleType<SnowstormParticleEffect?>,
                packetByteBuf: PacketByteBuf
            ): SnowstormParticleEffect {
                return SnowstormParticleEffect(
                    BedrockParticleEffect().also { it.readFromBuffer(packetByteBuf) }
                )
            }
        }
    }
}