/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.particle

import com.cobblemon.mod.common.api.snowstorm.BedrockParticleEffect
import com.cobblemon.mod.common.client.particle.ParticleStorm
import com.cobblemon.mod.common.client.render.SnowstormParticle
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.client.particle.Particle
import net.minecraft.client.particle.ParticleFactory
import net.minecraft.client.particle.SpriteProvider
import net.minecraft.client.texture.Sprite
import net.minecraft.client.world.ClientWorld
import net.minecraft.particle.ParticleType
import net.minecraft.util.math.Vec3d

class SnowstormParticleType : ParticleType<SnowstormParticleEffect>(false, SnowstormParticleEffect.PARAMETERS_FACTORY) {
    companion object {
        val CODEC: Codec<SnowstormParticleEffect> = RecordCodecBuilder.create { instance ->
            instance.group(
                BedrockParticleEffect.CODEC.fieldOf("effect").forGetter { it.effect }
            ).apply(instance, ::SnowstormParticleEffect)
        }
    }

    class Factory(val spriteProvider: SpriteProvider) : ParticleFactory<SnowstormParticleEffect> {
        override fun createParticle(
            parameters: SnowstormParticleEffect,
            world: ClientWorld,
            x: Double,
            y: Double,
            z: Double,
            velocityX: Double,
            velocityY: Double,
            velocityZ: Double
        ): Particle {
            return SnowstormParticle(
                ParticleStorm.contextStorm!!,
                world,
                x, y, z,
                Vec3d(velocityX, velocityY, velocityZ),
                invisible = false
            )
        }
    }

    override fun getCodec() = CODEC
}