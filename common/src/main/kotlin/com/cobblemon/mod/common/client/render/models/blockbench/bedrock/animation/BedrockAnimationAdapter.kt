/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.bedrock.animation

import com.bedrockk.molang.MoLang
import com.bedrockk.molang.parser.MoLangParser
import com.bedrockk.molang.parser.tokenizer.TokenIterator
import com.bedrockk.molang.runtime.MoLangRuntime
import com.cobblemon.mod.common.Cobblemon.LOGGER
import com.cobblemon.mod.common.client.particle.BedrockParticleEffectRepository
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.lang.IllegalArgumentException
import java.lang.reflect.Type
import net.minecraft.util.Identifier

/**
 * Gson adapter for converting bedrock/blockbench json data into a friendlier object model.
 *
 * @author landonjw
 * @since  January 5, 2022
 */
object BedrockAnimationAdapter : JsonDeserializer<BedrockAnimation> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): BedrockAnimation {
        if (json is JsonObject) {
            val animationLength = json["animation_length"]?.asDouble ?: -1.0
            val shouldLoop = animationLength > 0 && json["loop"]?.asBoolean == true
            val boneTimelines = mutableMapOf<String, BedrockBoneTimeline>()
            val particleEffects = mutableListOf<BedrockParticleKeyframe>()
            json["bones"]?.asJsonObject?.entrySet()?.forEach { (boneName, timeline) ->
                boneTimelines[boneName] = deserializeBoneTimeline(timeline.asJsonObject)
            }
            json["particle_effects"]?.asJsonObject?.entrySet()?.forEach { (frame, effectJson) ->
                fun resolveEffect(jsonObject: JsonObject): BedrockParticleKeyframe {
                    val effectId = jsonObject.get("effect").asString.asIdentifierDefaultingNamespace()
                    val effect = BedrockParticleEffectRepository.getEffect(effectId)
                        ?: throw IllegalArgumentException("Unrecognized particle effect $effectId referenced in animation. Maybe your particle effect isn't named correctly inside the effect file?")
                    val locator = jsonObject.get("locator")?.asString ?: "root"
                    val seconds = frame.toFloat()
                    val scripts = jsonObject.get("pre_effect_script")?.asString?.split("\n")?.map { MoLang.createParser(it).parseExpression() } ?: emptyList()
                    return BedrockParticleKeyframe(
                        seconds = seconds,
                        effect = effect,
                        locator = locator,
                        scripts = scripts
                    )
                }

                if (effectJson is JsonObject) {
                    particleEffects.add(resolveEffect(effectJson))
                } else if (effectJson is JsonArray) {
                    for (effectJsonElement in effectJson) {
                        particleEffects.add(resolveEffect(effectJsonElement as JsonObject))
                    }
                }
            }
            return BedrockAnimation(shouldLoop, animationLength, particleEffects, boneTimelines)
        }
        else {
            throw IllegalStateException("animation json could not be parsed")
        }
    }

    private fun deserializeBoneTimeline(bone: JsonObject): BedrockBoneTimeline {
        val positions = if (bone.has("position")) {
            if (bone["position"].isJsonObject) {
                deserializeRotationKeyframes(bone["position"].asJsonObject, Transformation.POSITION)
            } else {
                deserializeMolangBoneValue(bone["position"].asJsonArray, Transformation.POSITION)
            }
        } else {
            EmptyBoneValue
        }
        val rotations = if (bone.has("rotation")) {
            if (bone["rotation"].isJsonObject) {
                deserializeRotationKeyframes(bone["rotation"].asJsonObject, Transformation.ROTATION)
            } else {
                deserializeMolangBoneValue(bone["rotation"].asJsonArray, Transformation.ROTATION)
            }
        } else {
            EmptyBoneValue
        }
        return BedrockBoneTimeline(positions, rotations)
    }

    fun cleanExpression(value: String) =
        (if (value.startsWith("+")) value.substring(1) else value).let {
            if (it.startsWith("-(")) it.replaceFirst("-(", "-1*(") else it
        }.replace("*+", "*").replace("q.", "query.")
            .replace("camera_rotation(0)", "camera_rotation_x").replace("camera_rotation(1)", "camera_rotation_y")

    fun deserializeMolangBoneValue(array: JsonArray, transformation: Transformation): MolangBoneValue {
        try {
            return MolangBoneValue(
                x = MoLang.createParser(cleanExpression(array[0].asString)).parseExpression(),
                y = MoLang.createParser(cleanExpression(array[1].asString)).parseExpression(),
                z = MoLang.createParser(cleanExpression(array[2].asString)).parseExpression(),
                transformation = transformation
            )
        } catch (e: Exception) {
            LOGGER.error(array.joinToString { it.toString() })
            throw e
        }
    }

    private fun deserializeRotationKeyframes(frames: JsonObject, transformation: Transformation): BedrockKeyFrameBoneValue {
        val keyframes = BedrockKeyFrameBoneValue()
        frames.entrySet().forEach { (time, keyframeJson) ->
            val timeDbl = time.toDouble()
            when {
                keyframeJson is JsonObject -> {
                    if (keyframeJson.has("post")) {
//                        val transformationData = keyframeJson["post"].asJsonArray.map { it.asDouble }
//                        val transformationVector = Vec3d(transformationData[0], transformationData[1], transformationData[2])
                        keyframes[timeDbl] = BedrockAnimationKeyFrame(
                            time = timeDbl,
                            transformation = transformation,
                            data = deserializeMolangBoneValue(keyframeJson["post"].asJsonArray, transformation),
                            interpolationType = InterpolationType.SMOOTH
                        )
                    }
                    else {
                        throw IllegalStateException("transformation data ('post') could not be found")
                    }
                }
                keyframeJson is JsonArray -> {
//                    val transformationData = keyframeJson.map { it.asDouble }
//                    val transformationVector = Vec3d(transformationData[0], transformationData[1], transformationData[2])
                    keyframes[timeDbl] = BedrockAnimationKeyFrame(
                        time = timeDbl,
                        transformation = transformation,
                        data = deserializeMolangBoneValue(keyframeJson, transformation),
                        interpolationType = InterpolationType.LINEAR
                    )
                }
                else -> throw IllegalStateException("keyframe json could not be parsed")
            }
        }
        return keyframes
    }

}