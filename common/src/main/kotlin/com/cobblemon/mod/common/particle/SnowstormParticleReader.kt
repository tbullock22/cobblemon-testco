/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.particle

import com.bedrockk.molang.Expression
import com.bedrockk.molang.ast.NumberExpression
import com.cobblemon.mod.common.api.snowstorm.AnimatedParticleUVMode
import com.cobblemon.mod.common.api.snowstorm.BedrockParticle
import com.cobblemon.mod.common.api.snowstorm.BedrockParticleEffect
import com.cobblemon.mod.common.api.snowstorm.BedrockParticleEmitter
import com.cobblemon.mod.common.api.snowstorm.BezierMoLangCurve
import com.cobblemon.mod.common.api.snowstorm.BoxParticleEmitterShape
import com.cobblemon.mod.common.api.snowstorm.CatmullRomMoLangCurve
import com.cobblemon.mod.common.api.snowstorm.CustomMotionDirection
import com.cobblemon.mod.common.api.snowstorm.DiscParticleEmitterShape
import com.cobblemon.mod.common.api.snowstorm.DynamicParticleMotion
import com.cobblemon.mod.common.api.snowstorm.DynamicParticleRotation
import com.cobblemon.mod.common.api.snowstorm.EntityBoundingBoxParticleEmitterShape
import com.cobblemon.mod.common.api.snowstorm.ExpressionEmitterLifetime
import com.cobblemon.mod.common.api.snowstorm.ExpressionParticleTinting
import com.cobblemon.mod.common.api.snowstorm.GradientParticleTinting
import com.cobblemon.mod.common.api.snowstorm.InstantParticleEmitterRate
import com.cobblemon.mod.common.api.snowstorm.InwardsMotionDirection
import com.cobblemon.mod.common.api.snowstorm.LinearMoLangCurve
import com.cobblemon.mod.common.api.snowstorm.LoopingEmitterLifetime
import com.cobblemon.mod.common.api.snowstorm.OnceEmitterLifetime
import com.cobblemon.mod.common.api.snowstorm.OutwardsMotionDirection
import com.cobblemon.mod.common.api.snowstorm.ParticleCollision
import com.cobblemon.mod.common.api.snowstorm.ParticleMaterial
import com.cobblemon.mod.common.api.snowstorm.ParticleMotionDirection
import com.cobblemon.mod.common.api.snowstorm.ParticleSpace
import com.cobblemon.mod.common.api.snowstorm.PointParticleEmitterShape
import com.cobblemon.mod.common.api.snowstorm.RotateXYZCameraMode
import com.cobblemon.mod.common.api.snowstorm.RotateYCameraMode
import com.cobblemon.mod.common.api.snowstorm.SphereParticleEmitterShape
import com.cobblemon.mod.common.api.snowstorm.StaticParticleMotion
import com.cobblemon.mod.common.api.snowstorm.StaticParticleUVMode
import com.cobblemon.mod.common.api.snowstorm.SteadyParticleEmitterRate
import com.cobblemon.mod.common.util.asExpression
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vector4f

object SnowstormParticleReader {
    fun loadEffect(json: JsonObject): BedrockParticleEffect {
        val effectJson = json.get("particle_effect").asJsonObject
        val descJson = effectJson.get("description").asJsonObject
        val basicRenderParametersJson = descJson.get("basic_render_parameters").asJsonObject
        val curvesJson = effectJson.get("curves")?.asJsonObject ?: JsonObject()
        val componentsJson = effectJson.get("components")?.asJsonObject ?: JsonObject()
        val emitterInitializationJson = componentsJson.get("minecraft:emitter_initialization")?.asJsonObject ?: JsonObject()
        val particleInitializationJson = componentsJson.get("minecraft:particle_initialization")?.asJsonObject ?: JsonObject()
        val steadyRateJson = componentsJson.get("minecraft:emitter_rate_steady")?.asJsonObject
        val instantRateJson = componentsJson.get("minecraft:emitter_rate_instant")?.asJsonObject
        val emitterLifetimeOnceJson = componentsJson.get("minecraft:emitter_lifetime_once")?.asJsonObject
        val emitterLifetimeLoopingJson = componentsJson.get("minecraft:emitter_lifetime_looping")?.asJsonObject
        val emitterLifetimeExpressionJson = componentsJson.get("minecraft:emitter_lifetime_expression")?.asJsonObject
        val emitterShapePointJson = componentsJson.get("minecraft:emitter_shape_point")?.asJsonObject
        val emitterShapeSphereJson = componentsJson.get("minecraft:emitter_shape_sphere")?.asJsonObject
        val emitterShapeDiscJson = componentsJson.get("minecraft:emitter_shape_disc")?.asJsonObject
        val emitterShapeBoxJson = componentsJson.get("minecraft:emitter_shape_box")?.asJsonObject
        val emitterShapeEntityBoundingBoxJson = componentsJson.get("minecraft:emitter_shape_entity_aabb")?.asJsonObject
        val dynamicMotionJson = componentsJson.get("minecraft:particle_motion_dynamic")?.asJsonObject
        val particleAppearanceJson = componentsJson.get("minecraft:particle_appearance_billboard").asJsonObject
        val sizeJson = particleAppearanceJson.get("size")?.asJsonArray
        val particleLifetimeJson = componentsJson.get("minecraft:particle_lifetime_expression")?.asJsonObject
        val cameraModeJson = particleAppearanceJson.get("facing_camera_mode") ?: JsonPrimitive("rotate_xyz")
        val uvModeJson = particleAppearanceJson.get("uv").asJsonObject
        val particleInitialSpinJson = componentsJson.get("minecraft:particle_initial_spin")?.asJsonObject
        val tintingJson = componentsJson.get("minecraft:particle_appearance_tinting")?.asJsonObject
        val colourJson = tintingJson?.get("color")
        val collisionJson = componentsJson.get("minecraft:particle_motion_collision")?.asJsonObject
        val spaceJson = componentsJson.get("minecraft:emitter_local_space")?.asJsonObject

        val id = Identifier(descJson.get("identifier").asString)
        val maxAge = particleLifetimeJson?.get("max_lifetime")?.asString?.asExpression() ?: 0.0.asExpression()
        val killExpression = particleLifetimeJson?.get("expiration_expression")?.asString?.asExpression() ?: 0.0.asExpression()
        val material = ParticleMaterial.valueOf(basicRenderParametersJson.get("material").asString.substringAfter("_").uppercase())
        val texture = basicRenderParametersJson.get("texture").asString.let { if (it.endsWith(".png")) it.replace(".png", "") else it }.replace("particles/", "particle/").replace("textures/", "").asIdentifierDefaultingNamespace()
        val sizeX = sizeJson?.get(0)?.asString?.asExpression() ?: 1.0.asExpression()
        val sizeY = sizeJson?.get(1)?.asString?.asExpression() ?: 1.0.asExpression()

        val startRotation = particleInitialSpinJson?.get("rotation")?.asString?.asExpression() ?: 0.0.asExpression()
        val rotationSpeed = particleInitialSpinJson?.get("rotation_rate")?.asString?.asExpression() ?: 0.0.asExpression()

        val curves = curvesJson.entrySet().mapNotNull { (name, curveJson) ->
            curveJson as JsonObject
            val variableName = name.substringAfter(".")
            when (curveJson.get("type").asString) {
                "catmull_rom" -> {
                    val nodes = curveJson.getAsJsonArray("nodes").map { it.asDouble }
                    CatmullRomMoLangCurve(
                        name = variableName,
                        input = curveJson.get("input").asString.asExpression(),
                        horizontalRange = curveJson.get("horizontal_range").asString.asExpression(),
                        nodes = nodes
                    )
                }
                "bezier" -> {
                    val nodes = curveJson.getAsJsonArray("nodes").map { it.asDouble }
                    BezierMoLangCurve(
                        name = variableName,
                        input = curveJson.get("input").asString.asExpression(),
                        horizontalRange = curveJson.get("horizontal_range").asString.asExpression(),
                        v0 = nodes[0],
                        v1 = nodes[1],
                        v2 = nodes[2],
                        v3 = nodes[3]
                    )
                }
                "bezier_chain" -> TODO("Bezier Chain curves are not implemented yet")
                "linear" -> {
                    val input = curveJson.get("input").asString.asExpression()
                    val horizontalRange = curveJson.get("horizontal_range").asString.asExpression()
                    val nodes = curveJson.get("nodes").asJsonArray.map { it.asDouble }
                    LinearMoLangCurve(variableName, input, horizontalRange, nodes)
                }
                else -> TODO("Unrecognized curve type was used")
            }
        }
        val emitterStartExpressions = (emitterInitializationJson["creation_expression"]?.asString ?: "").split(";").filter { it.isNotEmpty() }.map { it.asExpression() }
        val emitterUpdateExpressions = (emitterInitializationJson["per_update_expression"]?.asString ?: "").split(";").filter { it.isNotEmpty() }.map { it.asExpression() }
        val particleUpdateExpressions = (particleInitializationJson["per_update_expression"]?.asString ?: "").split(";").filter { it.isNotEmpty() }.map { it.asExpression() }
        val particleRenderExpressions = (particleInitializationJson["per_render_expressions"]?.asString ?: "").split(";").filter { it.isNotEmpty() }.map { it.asExpression() }
        var direction: ParticleMotionDirection? = null
        val speed = (componentsJson.get("minecraft:particle_initial_speed")?.asString ?: "0.0").asExpression()
        val rate = if (instantRateJson != null) {
            InstantParticleEmitterRate(amount = instantRateJson.get("num_particles").asInt)
        } else if (steadyRateJson != null) {
            SteadyParticleEmitterRate(
                rate = steadyRateJson.get("spawn_rate").asString.asExpression(),
                maximum = steadyRateJson.get("max_particles").asString.asExpression()
            )
        } else {
            throw IllegalStateException("Missing or unspecified emitter rate")
        }
        val lifetime = if (emitterLifetimeOnceJson != null) {
            OnceEmitterLifetime(activeTime = (emitterLifetimeOnceJson.get("active_time")?.asString ?: "").asExpression())
        } else if (emitterLifetimeLoopingJson != null) {
            LoopingEmitterLifetime(
                activeTime = (emitterLifetimeLoopingJson.get("active_time")?.asString ?: "").asExpression(),
                sleepTime = (emitterLifetimeLoopingJson.get("sleep_time")?.asString ?: "").asExpression()
            )
        } else if (emitterLifetimeExpressionJson != null) {
            ExpressionEmitterLifetime(
                activation = (emitterLifetimeExpressionJson.get("activation_expression")?.asString ?: "").asExpression(),
                expiration = (emitterLifetimeExpressionJson.get("expiration_expression")?.asString ?: "").asExpression()
            )
        } else {
            TODO("Missing or unspecified emitter lifetime")
        }

        fun resolveDirection(json: JsonObject) {
            val directionProperty = json.get("direction") ?: let {
                direction = CustomMotionDirection()
                return
            }
            direction = if (directionProperty.isJsonArray) {
                val arr = directionProperty.asJsonArray.map { it.asString.asExpression() }
                CustomMotionDirection(direction = Triple(arr[0], arr[1], arr[2]))
            } else {
                val name = directionProperty.asString
                if (name == "outwards") {
                    OutwardsMotionDirection()
                } else {
                    InwardsMotionDirection()
                }
            }
        }

        val shape = if (emitterShapePointJson != null) {
            val arr = emitterShapePointJson.get("offset")?.asJsonArray?.map { it.asString.asExpression() } ?: listOf(0.0.asExpression(), 0.0.asExpression(), 0.0.asExpression())
            resolveDirection(emitterShapePointJson)
            PointParticleEmitterShape(offset = Triple(arr[0], arr[1], arr[2]))
        } else if (emitterShapeSphereJson != null) {
            val arr = emitterShapeSphereJson.get("offset")?.asJsonArray?.map { it.asString.asExpression() } ?: listOf(0.0.asExpression(), 0.0.asExpression(), 0.0.asExpression())
            resolveDirection(emitterShapeSphereJson)
            SphereParticleEmitterShape(
                offset = Triple(arr[0], arr[1], arr[2]),
                radius = emitterShapeSphereJson.get("radius")?.asString?.asExpression() ?: 0.0.asExpression(),
                surfaceOnly = emitterShapeSphereJson.get("surface_only")?.asBoolean ?: false
            )
        } else if (emitterShapeDiscJson != null) {
            resolveDirection(emitterShapeDiscJson)
            val offsetExpressions = emitterShapeDiscJson.get("offset")?.asJsonArray?.map { it.asString.asExpression() } ?: listOf(0.0.asExpression(), 0.0.asExpression(), 0.0.asExpression())
            val normalJson = emitterShapeDiscJson.get("plane_normal") ?: JsonPrimitive("y")
            val normal: Triple<Expression, Expression, Expression> = if (normalJson.isJsonArray) {
                val normalArr = normalJson.asJsonArray.map { it.asString.asExpression() }
                Triple(normalArr[0], normalArr[1], normalArr[2])
            } else {
                when (normalJson.asString) {
                    "x" -> Triple(1.0.asExpression(), 0.0.asExpression(), 0.0.asExpression())
                    "y" -> Triple(0.0.asExpression(), 1.0.asExpression(), 0.0.asExpression())
                    else -> Triple(0.0.asExpression(), 0.0.asExpression(), 1.0.asExpression())
                }
            }

            resolveDirection(emitterShapeDiscJson)
            DiscParticleEmitterShape(
                offset = Triple(offsetExpressions[0], offsetExpressions[1], offsetExpressions[2]),
                radius = emitterShapeDiscJson.get("radius")?.asString?.asExpression() ?: 0.0.asExpression(),
                surfaceOnly = emitterShapeDiscJson.get("surface_only")?.asBoolean ?: false,
                normal = normal
            )
        } else if (emitterShapeBoxJson != null) {
            resolveDirection(emitterShapeBoxJson)
            val offsetExpressions = emitterShapeBoxJson.get("offset")?.asJsonArray?.map { it.asString.asExpression() }
                ?: listOf(0.0.asExpression(), 0.0.asExpression(), 0.0.asExpression())
            val boxSizeExpressions = emitterShapeBoxJson.get("half_dimensions")?.asJsonArray?.map { it.asString.asExpression() }
                ?: listOf(0.0.asExpression(), 0.0.asExpression(), 0.0.asExpression())
            BoxParticleEmitterShape(
                offset = Triple(offsetExpressions[0], offsetExpressions[1], offsetExpressions[2]),
                boxSize = Triple(boxSizeExpressions[0], boxSizeExpressions[1], boxSizeExpressions[2]),
                surfaceOnly = emitterShapeBoxJson.get("surface_only")?.asBoolean ?: false
            )
        } else if (emitterShapeEntityBoundingBoxJson != null) {
            resolveDirection(emitterShapeEntityBoundingBoxJson)
            EntityBoundingBoxParticleEmitterShape(surfaceOnly = emitterShapeEntityBoundingBoxJson.get("surface_only")?.asBoolean ?: false)
        } else {
            TODO("Missing or unimplemented emitter shape")
        }
        val motion = if (dynamicMotionJson != null) {
            val accelerationExpressions = dynamicMotionJson.get("linear_acceleration")?.asJsonArray?.map { it.asString.asExpression() }
                ?: listOf(0.0.asExpression(), 0.0.asExpression(), 0.0.asExpression())
            val drag= dynamicMotionJson.get("linear_drag_coefficient")?.asString?.asExpression() ?: 0.0.asExpression()
            DynamicParticleMotion(
                direction = direction!!,
                speed = speed,
                acceleration = Triple(accelerationExpressions[0], accelerationExpressions[1], accelerationExpressions[2]),
                drag = drag
            )
        } else {
            StaticParticleMotion()
        }
        val cameraMode = if (cameraModeJson.isJsonPrimitive && cameraModeJson.asString == "rotate_xyz") {
            RotateXYZCameraMode()
        } else if (cameraModeJson.isJsonPrimitive && cameraModeJson.asString == "rotate_y") {
            RotateYCameraMode()
        } else {
            TODO("Missing or unimplemented camera mode")
        }
        val uvMode = if (uvModeJson.has("flipbook")) {
            val flipbook = uvModeJson.get("flipbook").asJsonObject
            val baseUV = flipbook.get("base_UV").asJsonArray
            val sizeUV = flipbook.get("size_UV").asJsonArray
            val stepUV = flipbook.get("step_UV").asJsonArray
            AnimatedParticleUVMode(
                startU = baseUV[0].asString.asExpression(),
                startV = baseUV[1].asString.asExpression(),
                uSize = sizeUV[0].asString.asExpression(),
                vSize = sizeUV[1].asString.asExpression(),
                stepU = stepUV[0].asString.asExpression(),
                stepV = stepUV[1].asString.asExpression(),
                textureSizeX = uvModeJson.get("texture_width")?.asInt ?: 128,
                textureSizeY = uvModeJson.get("texture_height")?.asInt ?: 128,
                maxFrame = flipbook.get("max_frame")?.asString?.asExpression() ?: NumberExpression(0.0),
                loop = flipbook.get("loop")?.asBoolean ?: false,
                fps = flipbook.get("frames_per_second")?.asString?.asExpression() ?: NumberExpression(0.0),
                stretchToLifetime = flipbook.get("stretch_to_lifetime")?.asBoolean ?: false
            )
        } else {
            val uvJson = uvModeJson.get("uv").asJsonArray ?: JsonArray().also { it.add(JsonPrimitive("0")); it.add(JsonPrimitive("0")) }
            val uvSizeJson = uvModeJson.get("uv_size")?.asJsonArray ?: JsonArray().also { it.add(JsonPrimitive("128")); it.add(JsonPrimitive("128")) }
            StaticParticleUVMode(
                startU = uvJson[0].asString.asExpression(),
                startV = uvJson[1].asString.asExpression(),
                uSize = uvSizeJson[0].asString.asExpression(),
                vSize = uvSizeJson[1].asString.asExpression(),
                textureSizeX = uvModeJson.get("texture_width")?.asInt ?: 128,
                textureSizeY = uvModeJson.get("texture_height")?.asInt ?: 128
            )
        }
        val rotation = DynamicParticleRotation(
            startRotation = startRotation,
            speed = rotationSpeed,
            acceleration = dynamicMotionJson?.get("rotation_acceleration")?.asString?.asExpression() ?: 0.0.asExpression(),
            drag = dynamicMotionJson?.get("rotation_drag_coefficient")?.asString?.asExpression() ?: 0.0.asExpression()
        )
        val tinting = if (colourJson is JsonObject) {
            GradientParticleTinting(
                interpolant = colourJson.get("interpolant").asString.asExpression(),
                gradient = colourJson.get("gradient").asJsonObject.entrySet().map { (key, hex) ->
                    key.toDouble() to parseHex(hex.asString)
                }.toMap()
            )
        } else if (colourJson is JsonArray) {
            val arr = colourJson.map { it.asString.asExpression() }
            ExpressionParticleTinting(
                red = arr[0],
                green = arr[1],
                blue = arr[2],
                alpha = arr[3]
            )
        } else {
            ExpressionParticleTinting()
        }
        val environmentLighting = componentsJson.has("minecraft:particle_appearance_lighting")
        val collision = collisionJson?.let {
            var collides = true
            val radius = it.get("collision_radius")?.asString?.asExpression() ?: run {
                collides = false
                NumberExpression(0.1)
            }
            ParticleCollision(
                enabled = it.get("enabled")?.asString?.asExpression() ?: NumberExpression(if (collides) 1.0 else 0.0),
                radius = radius,
                friction = it.get("collision_drag")?.asString?.asExpression() ?: NumberExpression(10.0),
                bounciness = it.get("coefficient_of_restitution")?.asString?.asExpression() ?: NumberExpression(0.0),
                expiresOnContact = it.get("expire_on_contact")?.asBoolean ?: false
            )
        } ?: ParticleCollision()
        val space = spaceJson?.let {
            val spaceRotation = it.get("rotation")?.asBoolean ?: false
            ParticleSpace(
                localPosition = if (spaceRotation) true else it.get("position")?.asBoolean ?: false,
                localRotation = spaceRotation,
                localVelocity = it.get("velocity")?.asBoolean ?: false
            )
        } ?: ParticleSpace()

        return BedrockParticleEffect(
            id = id,
            emitter = BedrockParticleEmitter(
                startExpressions = emitterStartExpressions.toMutableList(),
                updateExpressions = emitterUpdateExpressions.toMutableList(),
                rate = rate,
                shape = shape,
                lifetime = lifetime
            ),
            curves = curves.toMutableList(),
            particle = BedrockParticle(
                texture = texture,
                material = material,
                motion = motion,
                rotation = rotation,
                uvMode = uvMode,
                sizeX = sizeX,
                sizeY = sizeY,
                maxAge = maxAge,
                killExpression = killExpression,
                updateExpressions = particleUpdateExpressions.toMutableList(),
                renderExpressions = particleRenderExpressions.toMutableList(),
                cameraMode = cameraMode,
                collision = collision,
                environmentLighting = environmentLighting,
                tinting = tinting
            ),
            space = space
        )
    }

    private fun parseHex(hex: String): Vector4f {
        val cleaned = hex.replace("#", "")
        val alphaHex = cleaned.substring(0, 2)
        val redHex = cleaned.substring(2, 4)
        val greenHex = cleaned.substring(4, 6)
        val blueHex = cleaned.substring(6, 8)
        return Vector4f(
            redHex.toInt(16) / 255F,
            greenHex.toInt(16) / 255F,
            blueHex.toInt(16) / 255F,
            alphaHex.toInt(16) / 255F
        )
    }
}