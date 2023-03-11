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
import com.bedrockk.molang.ast.BooleanExpression
import com.bedrockk.molang.ast.NumberExpression
import com.cobblemon.mod.common.util.codec.EXPRESSION_CODEC
import com.cobblemon.mod.common.util.getString
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.ListCodec
import com.mojang.serialization.codecs.PrimitiveCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier

/**
 * Effect details for the actual particles of a particle effect.
 *
 * @author Hiroku
 * @since January 4th, 2023
 */
class BedrockParticle(
    var texture: Identifier = Identifier("minecraft:textures/particle/bubble.png"),
    var material: ParticleMaterial = ParticleMaterial.ALPHA,
    var uvMode: ParticleUVMode = StaticParticleUVMode(),
    var sizeX: Expression = NumberExpression(0.15),
    var sizeY: Expression = NumberExpression(0.15),
    var maxAge: Expression = NumberExpression(1.0),
    var killExpression: Expression = BooleanExpression(false),
    var updateExpressions: MutableList<Expression> = mutableListOf(),
    var renderExpressions: MutableList<Expression> = mutableListOf(),
    var motion: ParticleMotion = StaticParticleMotion(),
    var rotation: ParticleRotation = DynamicParticleRotation(),
    var cameraMode: ParticleCameraMode = RotateXYZCameraMode(),
    var tinting: ParticleTinting = ExpressionParticleTinting(),
    var collision: ParticleCollision = ParticleCollision(),
    var environmentLighting: Boolean = false
) {
    // kill plane maybe
    companion object {
        val CODEC: Codec<BedrockParticle> = RecordCodecBuilder.create { instance ->
            instance.group(
                Identifier.CODEC.fieldOf("texture").forGetter { it.texture },
                PrimitiveCodec.STRING.fieldOf("material").forGetter { it.material.name },
                ParticleUVMode.codec.fieldOf("uvMode").forGetter { it.uvMode },
                EXPRESSION_CODEC.fieldOf("sizeX").forGetter { it.sizeX },
                EXPRESSION_CODEC.fieldOf("sizeY").forGetter { it.sizeY },
                EXPRESSION_CODEC.fieldOf("maxAge").forGetter { it.maxAge },
                EXPRESSION_CODEC.fieldOf("killExpression").forGetter { it.killExpression },
                ListCodec(EXPRESSION_CODEC).fieldOf("updateExpressions").forGetter { it.updateExpressions },
                ListCodec(EXPRESSION_CODEC).fieldOf("renderExpressions").forGetter { it.renderExpressions },
                ParticleMotion.codec.fieldOf("motion").forGetter { it.motion },
                ParticleRotation.codec.fieldOf("rotation").forGetter { it.rotation },
                ParticleCameraMode.codec.fieldOf("cameraMode").forGetter { it.cameraMode },
                ParticleTinting.codec.fieldOf("tinting").forGetter { it.tinting },
                ParticleCollision.CODEC.fieldOf("collision").forGetter { it.collision },
                PrimitiveCodec.BOOL.fieldOf("environmentLighting").forGetter { it.environmentLighting }
            ).apply(instance) {
                    texture, materialStr, uvMode, sizeX, sizeY, maxAge, killExpression, updateExpressions,
                    renderExpressions, motion, rotation, cameraMode, tinting, collision, environmentLighting ->
                BedrockParticle(
                    texture = texture,
                    material = ParticleMaterial.valueOf(materialStr),
                    uvMode = uvMode,
                    sizeX = sizeX,
                    sizeY = sizeY,
                    maxAge = maxAge,
                    killExpression = killExpression,
                    updateExpressions = updateExpressions,
                    renderExpressions = renderExpressions,
                    motion = motion,
                    rotation = rotation,
                    cameraMode = cameraMode,
                    tinting = tinting,
                    collision = collision,
                    environmentLighting = environmentLighting
                )
            }
        }
    }


    fun writeToBuffer(buffer: PacketByteBuf) {
        buffer.writeIdentifier(texture)
        buffer.writeString(material.name)
        ParticleUVMode.writeToBuffer(buffer, uvMode)
        buffer.writeString(sizeX.getString())
        buffer.writeString(sizeY.getString())
        buffer.writeString(maxAge.getString())
        buffer.writeString(killExpression.getString())
        buffer.writeCollection(updateExpressions) { pb, expression -> pb.writeString(expression.getString()) }
        buffer.writeCollection(renderExpressions) { pb, expression -> pb.writeString(expression.getString()) }
        ParticleMotion.writeToBuffer(buffer, motion)
        ParticleRotation.writeToBuffer(buffer, rotation)
        ParticleCameraMode.writeToBuffer(buffer, cameraMode)
        ParticleTinting.writeToBuffer(buffer, tinting)
        collision.writeToBuffer(buffer)
        buffer.writeBoolean(environmentLighting)
    }

    fun readFromBuffer(buffer: PacketByteBuf) {
        texture = buffer.readIdentifier()
        material = ParticleMaterial.valueOf(buffer.readString())
        uvMode = ParticleUVMode.readFromBuffer(buffer)
        sizeX = MoLang.createParser(buffer.readString()).parseExpression()
        sizeY = MoLang.createParser(buffer.readString()).parseExpression()
        maxAge = MoLang.createParser(buffer.readString()).parseExpression()
        killExpression = MoLang.createParser(buffer.readString()).parseExpression()
        updateExpressions = buffer.readList { MoLang.createParser(it.readString()).parseExpression() }
        renderExpressions = buffer.readList { MoLang.createParser(it.readString()).parseExpression() }
        motion = ParticleMotion.readFromBuffer(buffer)
        rotation = ParticleRotation.readFromBuffer(buffer)
        cameraMode = ParticleCameraMode.readFromBuffer(buffer)
        tinting = ParticleTinting.readFromBuffer(buffer)
        collision.readFromBuffer(buffer)
        environmentLighting = buffer.readBoolean()
    }
}