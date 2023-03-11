/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.snowstorm

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.ListCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier

/**
 * This is an interpretation of the Bedrock Edition particle system. The behaviour of the effects are
 * intended to be the same, but the grouping of elements and the transfer JSON format is custom so that
 * it actually makes some damn sense.
 *
 * @author Hiroku
 * @since January 2nd, 2023
 */
class BedrockParticleEffect(
    var id: Identifier = Identifier("effect"),
    var emitter: BedrockParticleEmitter = BedrockParticleEmitter(),
    var particle: BedrockParticle = BedrockParticle(),
    var curves: MutableList<MoLangCurve> = mutableListOf(),
    var space: ParticleSpace = ParticleSpace()
) {
    companion object {
        val CODEC: Codec<BedrockParticleEffect> = RecordCodecBuilder.create { instance ->
            instance.group(
                Identifier.CODEC.fieldOf("id").forGetter { it.id },
                BedrockParticleEmitter.CODEC.fieldOf("emitter").forGetter { it.emitter },
                BedrockParticle.CODEC.fieldOf("particle").forGetter { it.particle },
                ListCodec(MoLangCurve.codec).fieldOf("curves").forGetter { it.curves },
                ParticleSpace.CODEC.fieldOf("space").forGetter { it.space }
            ).apply(instance) { id, emitter, particle, curves, space ->
                BedrockParticleEffect(
                    id = id,
                    emitter = emitter,
                    particle = particle,
                    curves = curves.toMutableList(),
                    space = space
                )
            }
        }
    }

    fun writeToBuffer(buffer: PacketByteBuf) {
        buffer.writeIdentifier(id)
        emitter.writeToBuffer(buffer)
        particle.writeToBuffer(buffer)
        buffer.writeCollection(curves) { pb, curve -> MoLangCurve.writeToBuffer(buffer, curve) }
        space.writeToBuffer(buffer)
    }

    fun readFromBuffer(buffer: PacketByteBuf) {
        id = buffer.readIdentifier()
        emitter.readFromBuffer(buffer)
        particle.readFromBuffer(buffer)
        curves = buffer.readList { MoLangCurve.readFromBuffer(buffer) }
        space.readFromBuffer(buffer)
    }
}