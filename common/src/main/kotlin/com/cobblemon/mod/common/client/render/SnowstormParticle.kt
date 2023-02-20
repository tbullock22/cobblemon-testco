/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render

import com.bedrockk.molang.runtime.struct.VariableStruct
import com.bedrockk.molang.runtime.value.DoubleValue
import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.ModAPI
import com.cobblemon.mod.common.api.snowstorm.UVDetails
import com.cobblemon.mod.common.client.particle.ParticleStorm
import com.cobblemon.mod.common.util.resolveBoolean
import com.cobblemon.mod.common.util.resolveDouble
import kotlin.math.abs
import net.minecraft.client.MinecraftClient
import net.minecraft.client.particle.Particle
import net.minecraft.client.particle.ParticleTextureSheet
import net.minecraft.client.particle.ParticleTextureSheet.NO_RENDER
import net.minecraft.client.particle.ParticleTextureSheet.PARTICLE_SHEET_LIT
import net.minecraft.client.render.BufferBuilder
import net.minecraft.client.render.Camera
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.texture.Sprite
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3f
import net.minecraft.util.shape.VoxelShapes

class SnowstormParticle(
    val storm: ParticleStorm,
    world: ClientWorld,
    x: Double,
    y: Double,
    z: Double,
    initialVelocity: Vec3d,
    var invisible: Boolean = false
) : Particle(world, x, y, z) {
    companion object {
        const val MAXIMUM_DISTANCE_CHANGE_PER_TICK_FOR_FRICTION = 0.005
    }

    val sprite = getSpriteFromAtlas()

    val particleTextureSheet: ParticleTextureSheet
    var angularVelocity = 0.0
    var colliding = false

    var texture = storm.effect.particle.texture

    var variableStruct = (storm.runtime.environment.structs["variable"] as VariableStruct);

    val random1 = variableStruct.map["particle_random_1"]
    val random2 = variableStruct.map["particle_random_2"]
    val random3 = variableStruct.map["particle_random_3"]
    val random4 = variableStruct.map["particle_random_4"]

    val uvDetails = UVDetails()

    fun getSpriteFromAtlas(): Sprite {
        val atlas = MinecraftClient.getInstance().particleManager.particleAtlasTexture

//        val field = atlas::class.java.getDeclaredField("sprites")
//        field.isAccessible = true
//        val map = field.get(atlas) as Map<Identifier, Sprite>
//        println(map.keys.joinToString { it.toString() })
        val sprite = atlas.getSprite(storm.effect.particle.texture)
//        println(sprite.id)
//        println(storm.effect.particle.texture)
        return sprite
    }

    private fun applyRandoms() {
        variableStruct.setDirectly("particle_random_1", random1)
        variableStruct.setDirectly("particle_random_2", random2)
        variableStruct.setDirectly("particle_random_3", random3)
        variableStruct.setDirectly("particle_random_4", random4)
    }

    init {
        setVelocity(initialVelocity.x, initialVelocity.y, initialVelocity.z)
        angle = storm.effect.particle.rotation.getInitialRotation(storm.runtime).toFloat()
        prevAngle = angle
        angularVelocity = storm.effect.particle.rotation.getInitialAngularVelocity(storm.runtime)
        velocityMultiplier = 1F
        maxAge = (storm.runtime.resolveDouble(storm.effect.particle.maxAge) * 20).toInt()
        storm.particles.add(this)
        gravityStrength = 0F
        particleTextureSheet = if (invisible) {
            NO_RENDER
        } else {
            PARTICLE_SHEET_LIT
        }
    }

    override fun buildGeometry(vertexConsumer: VertexConsumer, camera: Camera, tickDelta: Float) {
        if (Cobblemon.implementation.modAPI != ModAPI.FORGE) {
           if (!MinecraftClient.getInstance().worldRenderer.frustum.isVisible(boundingBox)) {
               return
           }
        }

        applyRandoms()
        setParticleAgeInRuntime()
        storm.effect.curves.forEach { it.apply(storm.runtime) }
        storm.runtime.execute(storm.effect.particle.renderExpressions)
//        // TODO need to implement the other materials but not sure exactly what they are GL wise
//        when (storm.effect.particle.material) {
//            ParticleMaterial.ALPHA -> RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA)
//            ParticleMaterial.OPAQUE -> RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_COLOR, GlStateManager.DstFactor.ZERO)
//            ParticleMaterial.BLEND -> RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA)
//        }

        vertexConsumer as BufferBuilder

        val vec3d = camera.pos
        val f = (MathHelper.lerp(tickDelta.toDouble(), prevPosX, x) - vec3d.getX()).toFloat()
        val g = (MathHelper.lerp(tickDelta.toDouble(), prevPosY, y) - vec3d.getY()).toFloat()
        val h = (MathHelper.lerp(tickDelta.toDouble(), prevPosZ, z) - vec3d.getZ()).toFloat()
        val quaternion = storm.effect.particle.cameraMode.getRotation(prevAngle, angle, tickDelta, camera.rotation, camera.yaw, camera.pitch)

        val xSize = storm.runtime.resolveDouble(storm.effect.particle.sizeX).toFloat() / 2
        val ySize = storm.runtime.resolveDouble(storm.effect.particle.sizeY).toFloat() / 2

        val particleVertices = arrayOf(
            Vec3f(-xSize, -ySize, 0.0f),
            Vec3f(-xSize, ySize, 0.0f),
            Vec3f(xSize, ySize, 0.0f),
            Vec3f(xSize, -ySize, 0.0f)
        )


        for (k in 0..3) {
            val vertex = particleVertices[k]
            vertex.rotate(quaternion)
            vertex.add(f, g, h)
        }

        val uvs = storm.effect.particle.uvMode.get(storm.runtime, age / 20.0, maxAge / 20.0, uvDetails)
        val colour = storm.effect.particle.tinting.getTint(storm.runtime)

        val spriteURange = sprite.maxU - sprite.minU
        val spriteVRange = sprite.maxV - sprite.minV

        val minU = uvs.startU * spriteURange + sprite.minU
        val maxU = uvs.endU * spriteURange + sprite.minU
        val minV = uvs.startV * spriteVRange + sprite.minV
        val maxV = uvs.endV * spriteVRange + sprite.minV

        val p = if (storm.effect.particle.environmentLighting) getBrightness(tickDelta) else (15 shl 20 or (15 shl 4))
        vertexConsumer
            .vertex(particleVertices[0].x.toDouble(), particleVertices[0].y.toDouble(), particleVertices[0].z.toDouble())
            .texture(maxU, maxV)
            .color(colour.x, colour.y, colour.z, colour.w)
            .light(p)
            .next()
        vertexConsumer
            .vertex(particleVertices[1].x.toDouble(), particleVertices[1].y.toDouble(), particleVertices[1].z.toDouble())
            .texture(maxU, minV)
            .color(colour.x, colour.y, colour.z, colour.w)
            .light(p)
            .next()
        vertexConsumer
            .vertex(particleVertices[2].x.toDouble(), particleVertices[2].y.toDouble(), particleVertices[2].z.toDouble())
            .texture(minU, minV)
            .color(colour.x, colour.y, colour.z, colour.w)
            .light(p)
            .next()
        vertexConsumer
            .vertex(particleVertices[3].x.toDouble(), particleVertices[3].y.toDouble(), particleVertices[3].z.toDouble())
            .texture(minU, maxV)
            .color(colour.x, colour.y, colour.z, colour.w)
            .light(p)
            .next()
//        tessellator.draw()
    }

    override fun tick() {
        applyRandoms()

        setParticleAgeInRuntime()
        storm.runtime.execute(storm.effect.particle.updateExpressions)
        angularVelocity += storm.effect.particle.rotation.getAngularAcceleration(storm.runtime, angularVelocity) / 20

        if (age > maxAge || storm.runtime.resolveBoolean(storm.effect.particle.killExpression)) {
            markDead()
            return
        } else {
            val acceleration = storm.effect.particle.motion.getAcceleration(
                storm.runtime,
                Vec3d(velocityX, velocityY, velocityZ).multiply(20.0) // Uses blocks per second, not blocks per tick
            ).multiply(1 / 20.0).multiply(1 / 20.0)

            velocityX += acceleration.x
            velocityY += acceleration.y
            velocityZ += acceleration.z

            prevAngle = angle
            angle = prevAngle + angularVelocity.toFloat()
        }

        prevPosX = x
        prevPosY = y
        prevPosZ = z

        age++

        if (storm.effect.space.localPosition) {
            this.move(velocityX + (storm.getX() - storm.getPrevX()), velocityY + (storm.getY() - storm.getPrevY()), velocityZ + (storm.getZ() - storm.getPrevZ()))
        } else {
            this.move(velocityX, velocityY, velocityZ)
        }
    }

    override fun move(dx: Double, dy: Double, dz: Double) {
        val collision = storm.effect.particle.collision
        val radius = storm.runtime.resolveDouble(collision.radius)
        boundingBox = Box.of(Vec3d(x, y, z), radius, radius, radius)
        if (dx == 0.0 && dy == 0.0 && dz == 0.0) {
            return
        }

        var dx = dx
        var dy = dy
        var dz = dz

        // field_21507 stoppedByCollision

        if (storm.runtime.resolveBoolean(collision.enabled) && radius > 0.0) {
            collidesWithWorld = true

            val newMovement = checkCollision(Vec3d(dx, dy, dz))

            if (dead) {
                return
            }

            dx = newMovement.x
            dy = newMovement.y
            dz = newMovement.z

//            if (collidesWithWorld && (dx != 0.0 || dy != 0.0 || dz != 0.0) && dx * dx + dy * dy + dz * dz < 10000) {
//                val vec3d = Entity.adjustMovementForCollisions(
//                    null,
//                    Vec3d(dx, dy, dz),
//                    boundingBox,
//                    world,
//                    listOf()
//                )
//
//            }

            if (dx != 0.0 || dy != 0.0 || dz != 0.0) {
                boundingBox = boundingBox.offset(dx, dy, dz)
                x += dx
                y += dy
                z += dz
            }

//            if (abs(dy) >= 9.999999747378752E-6 && abs(dy) < 9.999999747378752E-6) {
//                field_21507 = true
//            }
//            onGround = dy != dy && e < 0.0
//            if (d != dx) {
//                velocityX = 0.0
//            }
//            if (dz != dz) {
//                velocityZ = 0.0
//            }
        } else {
            collidesWithWorld =  false
            if (dx != 0.0 || dy != 0.0 || dz != 0.0) {
                x += dx
                y += dy
                z += dz
            }
        }
    }

    private fun checkCollision(movement: Vec3d): Vec3d {
        val collision = storm.effect.particle.collision
        var box = boundingBox
        val bounciness = storm.runtime.resolveDouble(collision.bounciness)
        val friction = storm.runtime.resolveDouble(collision.friction)
        val expiresOnContact = collision.expiresOnContact

        val collisions = world.getBlockCollisions(null, box.stretch(movement))
        if (collisions.none()) {
            colliding = false
//            println("No collisions")
            return movement
        } else if (expiresOnContact) {
            markDead()
            return movement
        }

//        println("Collisions with Y values: ${collisionProvider.map { it.boundingBox.center.y }.distinct().joinToString() }")

        var xMovement = movement.x
        var yMovement = movement.y
        var zMovement = movement.z

        var bouncing = false
        var sliding = false

        if (yMovement != 0.0) {
//            // If it would have avoided collisions if not for the Y movement, then it's bouncing off a vertical-normal surface
//            val originalCollisionYs = collisionProvider.map { it.boundingBox.center.y }.distinct()
//            val yCollisions = world.getBlockCollisions(null, box.stretch(movement.multiply(1.0, 0.0, 1.0))).toList()
////            println("Compared to new Y values: ${yCollisions.map { it.boundingBox.center.y }.distinct().joinToString()}")
//            if (yCollisions.none { it.boundingBox.center.y in originalCollisionYs }) {
//                yMovement = 0.0
//                if (bounciness > 0.0 && abs(movement.y) > MAXIMUM_DISTANCE_CHANGE_PER_TICK_FOR_FRICTION) {
//                    velocityY *= -1 * bounciness
//                    yMovement = -1 * bounciness * movement.y
//                    bouncing = true
//                } else if (friction > 0.0) {
//                    sliding = true
//                    velocityY = 0.0
//                } else {
//                    velocityY = 0.0
//                }
//            } else {
//            }


            yMovement = VoxelShapes.calculateMaxOffset(Direction.Axis.Y, box, collisions, yMovement)
            if (yMovement != 0.0) {
                box = box.offset(0.0, 0.0, zMovement)
            } else {
                if (bounciness > 0.0 && abs(movement.y) > MAXIMUM_DISTANCE_CHANGE_PER_TICK_FOR_FRICTION) {
                    velocityY *= -1 * bounciness
                    yMovement = -1 * bounciness * movement.y
                    bouncing = true
                } else if (friction > 0.0) {
                    sliding = true
                    velocityY = 0.0
                } else {
                    velocityY = 0.0
                }
            }
        }

        val mostlyIsZMovement = abs(xMovement) < abs(zMovement)
        if (mostlyIsZMovement && zMovement != 0.0) {
            zMovement = VoxelShapes.calculateMaxOffset(Direction.Axis.Z, box, collisions, zMovement)
            if (zMovement != 0.0) {
                box = box.offset(0.0, 0.0, zMovement)
            } else {
                if (bounciness > 0.0 && abs(movement.z) > MAXIMUM_DISTANCE_CHANGE_PER_TICK_FOR_FRICTION) {
                    velocityZ *= -1 * bounciness
                    zMovement = -1 * bounciness * movement.z
                    bouncing = true
                } else if (friction > 0.0) {
                    sliding = true
                    velocityZ = 0.0
                } else {
                    velocityZ = 0.0
                }
            }
        }

        if (xMovement != 0.0) {
            xMovement = VoxelShapes.calculateMaxOffset(Direction.Axis.X, box, collisions, xMovement)
            if (!mostlyIsZMovement && xMovement != 0.0) {
                box = box.offset(xMovement, 0.0, 0.0)
            } else {
                if (bounciness > 0.0 && abs(movement.x) > MAXIMUM_DISTANCE_CHANGE_PER_TICK_FOR_FRICTION) {
                    velocityX *= -1 * bounciness
                    xMovement = -1 * bounciness * movement.x
                    bouncing = true
                } else if (friction > 0.0) {
                    sliding = true
                    velocityZ = 0.0
                } else {
                    velocityZ = 0.0
                }
            }
        }

        if (!mostlyIsZMovement && zMovement != 0.0) {
            zMovement = VoxelShapes.calculateMaxOffset(Direction.Axis.Z, box, collisions, zMovement)
            if (zMovement != 0.0) {
            } else {
                if (bounciness > 0.0 && abs(movement.z) > MAXIMUM_DISTANCE_CHANGE_PER_TICK_FOR_FRICTION) {
                    velocityZ *= -1 * bounciness
                    zMovement = -1 * bounciness * movement.z
                    bouncing = true
                } else if (friction > 0.0) {
                    sliding = true
                    velocityZ = 0.0
                } else {
                    velocityZ = 0.0
                }
            }
        }



        var newMovement = Vec3d(xMovement, yMovement, zMovement)

        if (sliding && !bouncing) {
            // If it's moving slower than the friction per second, time to stop
            newMovement = if (newMovement.length() * 20 < friction) {
                Vec3d.ZERO
            } else {
                newMovement.subtract(newMovement.normalize().multiply(friction / 20))
            }

            var velocity = Vec3d(velocityX, velocityY, velocityZ)
            if (velocity.length() * 20 < friction) {
                setVelocity(0.0, 0.0, 0.0)
            } else {
                velocity = velocity.subtract(velocity.normalize().multiply(friction / 20))
                setVelocity(velocity.x, velocity.y, velocity.z)
            }
        }

        return newMovement
    }


    private fun setParticleAgeInRuntime() {
        variableStruct.setDirectly("particle_age", DoubleValue(age / 20.0))
        variableStruct.setDirectly("particle_lifetime", DoubleValue(maxAge / 20.0))
    }

    override fun getType() = particleTextureSheet

    override fun markDead() {
        super.markDead()
        storm.particles.remove(this)
    }
}