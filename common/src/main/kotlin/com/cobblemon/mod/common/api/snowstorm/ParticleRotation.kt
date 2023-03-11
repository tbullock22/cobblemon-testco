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
import com.bedrockk.molang.runtime.MoLangRuntime
import com.cobblemon.mod.common.api.codec.CodecMapped
import com.cobblemon.mod.common.api.data.ArbitrarilyMappedSerializableCompanion
import com.cobblemon.mod.common.util.codec.EXPRESSION_CODEC
import com.cobblemon.mod.common.util.getString
import com.cobblemon.mod.common.util.resolveDouble
import com.mojang.serialization.Codec
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.codecs.PrimitiveCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import kotlin.math.abs
import net.minecraft.network.PacketByteBuf

interface ParticleRotation : CodecMapped {
    companion object : ArbitrarilyMappedSerializableCompanion<ParticleRotation, ParticleRotationType>(
        keyFromValue = { it.type },
        keyFromString = ParticleRotationType::valueOf,
        stringFromKey = { it.name }
    ) {
        init {
            registerSubtype(ParticleRotationType.DYNAMIC, DynamicParticleRotation::class.java, DynamicParticleRotation.CODEC)
        }
    }

    val type: ParticleRotationType

    fun getInitialRotation(runtime: MoLangRuntime): Double
    fun getInitialAngularVelocity(runtime: MoLangRuntime): Double
    fun getAngularAcceleration(runtime: MoLangRuntime, angularVelocity: Double): Double
}

class DynamicParticleRotation(
    var startRotation: Expression = NumberExpression(0.0),
    var speed: Expression = NumberExpression(0.0),
    var acceleration: Expression = NumberExpression(0.0),
    var drag: Expression = NumberExpression(0.0)
): ParticleRotation {
    companion object {
        val CODEC: Codec<DynamicParticleRotation> = RecordCodecBuilder.create { instance ->
            instance.group(
                PrimitiveCodec.STRING.fieldOf("type").forGetter { it.type.name },
                EXPRESSION_CODEC.fieldOf("startRotation").forGetter { it.startRotation },
                EXPRESSION_CODEC.fieldOf("speed").forGetter { it.speed },
                EXPRESSION_CODEC.fieldOf("acceleration").forGetter { it.acceleration },
                EXPRESSION_CODEC.fieldOf("drag").forGetter { it.drag }
            ).apply(instance) { _, startRotation, speed, acceleration, drag ->
                DynamicParticleRotation(startRotation, speed, acceleration, drag)
            }
        }
    }

    override val type = ParticleRotationType.DYNAMIC
    override fun <T> encode(ops: DynamicOps<T>) = CODEC.encodeStart(ops, this)
    override fun getInitialRotation(runtime: MoLangRuntime) = runtime.resolveDouble(startRotation)
    override fun getInitialAngularVelocity(runtime: MoLangRuntime) = runtime.resolveDouble(speed)
    override fun getAngularAcceleration(runtime: MoLangRuntime, angularVelocity: Double): Double {
        val acceleration = runtime.resolveDouble(acceleration)
        val nextVelocity = angularVelocity + acceleration
        val drag = nextVelocity * runtime.resolveDouble(drag)
        return if (abs(drag) > abs(nextVelocity)) {
            0.0
        } else {
            nextVelocity - drag - angularVelocity
        }
    }

    override fun readFromBuffer(buffer: PacketByteBuf) {
        startRotation = MoLang.createParser(buffer.readString()).parseExpression()
        speed = MoLang.createParser(buffer.readString()).parseExpression()
        acceleration = MoLang.createParser(buffer.readString()).parseExpression()
        drag = MoLang.createParser(buffer.readString()).parseExpression()
    }

    override fun writeToBuffer(buffer: PacketByteBuf) {
        buffer.writeString(startRotation.getString())
        buffer.writeString(speed.getString())
        buffer.writeString(acceleration.getString())
        buffer.writeString(drag.getString())
    }
}

enum class ParticleRotationType {
    DYNAMIC,
    PARAMETRIC
}