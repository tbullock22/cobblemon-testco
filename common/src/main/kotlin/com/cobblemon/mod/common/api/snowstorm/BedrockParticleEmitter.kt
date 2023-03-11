/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.snowstorm

import com.bedrockk.molang.Expression
import com.bedrockk.molang.MoLang
import com.bedrockk.molang.ast.NumberExpression
import com.cobblemon.mod.common.util.codec.EXPRESSION_CODEC
import com.cobblemon.mod.common.util.getString
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.ListCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.PacketByteBuf

/**
 * Configuration of the emitter component for a particle effect.
 *
 * @author Hiroku
 * @since January 4th, 2023
 */
class BedrockParticleEmitter(
    var startExpressions: MutableList<Expression> = mutableListOf(),
    var updateExpressions: MutableList<Expression> = mutableListOf(),
    var rate: ParticleEmitterRate = InstantParticleEmitterRate(),
    var shape: ParticleEmitterShape = SphereParticleEmitterShape(),
    var lifetime: ParticleEmitterLifetime = OnceEmitterLifetime(NumberExpression(1.0))
) {
    companion object {
        val CODEC: Codec<BedrockParticleEmitter> = RecordCodecBuilder.create { instance ->
            instance.group(
                ListCodec(EXPRESSION_CODEC).fieldOf("startExpressions").forGetter { it.startExpressions },
                ListCodec(EXPRESSION_CODEC).fieldOf("updateExpressions").forGetter { it.updateExpressions },
                ParticleEmitterRate.codec.fieldOf("rate").forGetter { it.rate },
                ParticleEmitterShape.codec.fieldOf("shape").forGetter { it.shape },
                ParticleEmitterLifetime.codec.fieldOf("lifetime").forGetter { it.lifetime }
            ).apply(instance) { startExpressions, updateExpressions, rate, shape, lifetime ->
                BedrockParticleEmitter(
                    startExpressions = startExpressions,
                    updateExpressions = updateExpressions,
                    shape = shape,
                    rate = rate,
                    lifetime = lifetime
                )
            }
        }
    }

    fun writeToBuffer(buffer: PacketByteBuf) {
        buffer.writeCollection(startExpressions) { pb, expression -> pb.writeString(expression.getString()) }
        buffer.writeCollection(updateExpressions) { pb, expression -> pb.writeString(expression.getString()) }
        ParticleEmitterRate.writeToBuffer(buffer, rate)
        ParticleEmitterShape.writeToBuffer(buffer, shape)
        ParticleEmitterLifetime.writeToBuffer(buffer, lifetime)
    }

    fun readFromBuffer(buffer: PacketByteBuf) {
        startExpressions = buffer.readList { MoLang.createParser(buffer.readString()).parseExpression() }
        updateExpressions = buffer.readList { MoLang.createParser(buffer.readString()).parseExpression() }
        rate = ParticleEmitterRate.readFromBuffer(buffer)
        shape = ParticleEmitterShape.readFromBuffer(buffer)
        lifetime = ParticleEmitterLifetime.readFromBuffer(buffer)
    }
}