/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.bedrock.animation

import com.bedrockk.molang.Expression
import com.bedrockk.molang.runtime.MoLangRuntime
import com.bedrockk.molang.runtime.MoScope
import com.bedrockk.molang.runtime.value.DoubleValue
import com.cobblemon.mod.common.api.snowstorm.BedrockParticleEffect
import com.cobblemon.mod.common.client.particle.ParticleStorm
import com.cobblemon.mod.common.client.render.models.blockbench.PoseableEntityModel
import com.cobblemon.mod.common.client.render.models.blockbench.PoseableEntityState
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.math.geometry.getOrigin
import com.cobblemon.mod.common.util.math.geometry.toRadians
import com.cobblemon.mod.common.util.resolveDouble
import java.util.SortedMap
import net.minecraft.client.MinecraftClient
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.Entity
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents
import net.minecraft.util.crash.CrashException
import net.minecraft.util.crash.CrashReport
import net.minecraft.util.math.Matrix4f
import net.minecraft.util.math.Vec3d

data class BedrockAnimationGroup(
    val formatVersion: String,
    val animations: Map<String, BedrockAnimation>
)

data class BedrockParticleKeyframe(
    val seconds: Float,
    val effect: BedrockParticleEffect,
    val locator: String,
    val scripts: List<Expression>
)

data class BedrockAnimation(
    val shouldLoop: Boolean,
    val animationLength: Double,
    val particleEffects: List<BedrockParticleKeyframe>,
    val boneTimelines: Map<String, BedrockBoneTimeline>
) {
    companion object {
        val sharedRuntime = MoLangRuntime().also {
            it.environment.structs["query"] = it.environment.structs["variable"]
        }
    }

    fun run(model: PoseableEntityModel<*>, entity: Entity?, state: PoseableEntityState<*>?, previousSecondsPassed: Double, secondsPassed: Double): Boolean {
        var animationSeconds = secondsPassed
        if (shouldLoop) {
            animationSeconds %= animationLength
        } else if (animationSeconds > animationLength && animationLength > 0) {
            return false
        }

        if (entity != null && state != null) {
            val particleEffectsToPlay = mutableListOf<BedrockParticleKeyframe>()
            if (previousSecondsPassed > animationSeconds) {
                particleEffectsToPlay.addAll(particleEffects.filter { it.seconds >= previousSecondsPassed || it.seconds <= animationSeconds })
            } else {
                particleEffectsToPlay.addAll(particleEffects.filter { it.seconds in previousSecondsPassed..animationSeconds })
            }

            for (particleEffect in particleEffectsToPlay) {
                val world = entity.world as ClientWorld
                val matrixWrapper = model.locatorStates[particleEffect.locator] ?: model.locatorStates["root"]!!
                val effect = particleEffect.effect

                if (particleEffect in state.poseParticles) {
                    continue
                }

                val storm = ParticleStorm(
                    effect = effect,
                    matrixWrapper = matrixWrapper,
                    world = world,
                    sourceVelocity = { entity.velocity },
                    sourceAlive = { !entity.isRemoved && particleEffect in state.poseParticles }
                )

                state.poseParticles.add(particleEffect)
                storm.runtime.execute(particleEffect.scripts)
                storm.spawn()
            }
        }

        boneTimelines.forEach { (boneName, timeline) ->
            val part = model.relevantPartsByName[boneName]
            if (part != null) {
                if (!timeline.position.isEmpty()) {
                    val position = timeline.position.resolve(animationSeconds, state?.runtime ?: sharedRuntime).multiply(model.getChangeFactor(part.modelPart).toDouble())
                    part.modelPart.apply {
                        pivotX += position.x.toFloat()
                        pivotY += position.y.toFloat()
                        pivotZ += position.z.toFloat()
                    }
                }

                if (!timeline.rotation.isEmpty()) {
                    try {
                        val rotation = timeline.rotation.resolve(animationSeconds, state?.runtime ?: sharedRuntime).multiply(model.getChangeFactor(part.modelPart).toDouble())
                        part.modelPart.apply {
                            pitch += rotation.x.toFloat().toRadians()
                            yaw += rotation.y.toFloat().toRadians()
                            roll += rotation.z.toFloat().toRadians()
                        }
                    } catch (e: Exception) {
                        val exception = IllegalStateException("Bad animation for species: ${((model.currentEntity)!! as PokemonEntity).pokemon.species.name}", e)
                        val crash = CrashReport("Cobblemon encountered an unexpected crash", exception)
                        val section = crash.addElement("Animation Details")
                        state?.let {
                            section.add("Pose", state.currentPose!!)
                        }
                        section.add("Bone", boneName)

                        throw CrashException(crash)
                    }
                }
            }
        }
        return true
    }
}

interface BedrockBoneValue {
    fun resolve(time: Double, runtime: MoLangRuntime): Vec3d
    fun isEmpty(): Boolean
}

object EmptyBoneValue : BedrockBoneValue {
    override fun resolve(time: Double, runtime: MoLangRuntime) = Vec3d.ZERO
    override fun isEmpty() = true
}

data class BedrockBoneTimeline (
    val position: BedrockBoneValue,
    val rotation: BedrockBoneValue
)
class MolangBoneValue(
    val x: Expression,
    val y: Expression,
    val z: Expression,
    transformation: Transformation
) : BedrockBoneValue {
    val yMul = if (transformation == Transformation.POSITION) -1 else 1
    override fun isEmpty() = false
    override fun resolve(time: Double, runtime: MoLangRuntime): Vec3d {
        val environment = runtime.environment
        val scope = MoScope()
        environment.setSimpleVariable("anim_time", DoubleValue(time))
        environment.setSimpleVariable("camera_rotation_x", DoubleValue(MinecraftClient.getInstance().gameRenderer.camera.rotation.x.toDouble()))
        environment.setSimpleVariable("camera_rotation_y", DoubleValue(MinecraftClient.getInstance().gameRenderer.camera.rotation.y.toDouble()))
        return Vec3d(
            runtime.resolveDouble(x),
            runtime.resolveDouble(y) * yMul,
            runtime.resolveDouble(z)
        )
    }

}
class BedrockKeyFrameBoneValue : HashMap<Double, BedrockAnimationKeyFrame>(), BedrockBoneValue {
    fun SortedMap<Double, BedrockAnimationKeyFrame>.getAtIndex(index: Int?): BedrockAnimationKeyFrame? {
        if (index == null) return null
        val key = this.keys.elementAtOrNull(index)
        return if (key != null) this[key] else null
    }

    override fun resolve(time: Double, runtime: MoLangRuntime): Vec3d {
        val sortedTimeline = toSortedMap()

        var afterIndex : Int? = sortedTimeline.keys.indexOfFirst { it > time }
        if (afterIndex == -1) afterIndex = null
        val beforeIndex = when (afterIndex) {
            null -> sortedTimeline.size - 1
            0 -> null
            else -> afterIndex - 1
        }
        val after = sortedTimeline.getAtIndex(afterIndex)
        val before = sortedTimeline.getAtIndex(beforeIndex)

        val afterData = after?.data?.resolve(time, runtime) ?: Vec3d.ZERO
        val beforeData = before?.data?.resolve(time, runtime) ?: Vec3d.ZERO

        if (before != null || after != null) {
            if (before != null && before.interpolationType == InterpolationType.SMOOTH || after != null && after.interpolationType == InterpolationType.SMOOTH) {
                when {
                    before != null && after != null -> {
                        val beforePlusIndex = if (beforeIndex == null || beforeIndex == 0) null else beforeIndex - 1
                        val beforePlus = sortedTimeline.getAtIndex(beforePlusIndex)
                        val afterPlusIndex = if (afterIndex == null || afterIndex == size - 1) null else afterIndex + 1
                        val afterPlus = sortedTimeline.getAtIndex(afterPlusIndex)
                        return catmullromLerp(beforePlus, before, after, afterPlus, time, runtime)
                    }
                    before != null -> return beforeData
                    else -> return afterData
                }
            }
            else {
                when {
                    before != null && after != null -> {
                        return Vec3d(
                            beforeData.x + (afterData.x - beforeData.x) * linearLerpAlpha(before.time, after.time, time),
                            beforeData.y + (afterData.y - beforeData.y) * linearLerpAlpha(before.time, after.time, time),
                            beforeData.z + (afterData.z - beforeData.z) * linearLerpAlpha(before.time, after.time, time)
                        )
                    }
                    before != null -> return beforeData
                    else -> return afterData
                }
            }
        }
        else {
            return Vec3d(0.0, 0.0, 0.0)
        }
    }

}

data class BedrockAnimationKeyFrame(
    val time: Double,
    val transformation: Transformation,
    val data: MolangBoneValue,
    val interpolationType: InterpolationType
)

enum class InterpolationType {
    SMOOTH, LINEAR
}

enum class Transformation {
    POSITION, ROTATION
}