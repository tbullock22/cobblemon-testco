/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.snowstorm

import com.cobblemon.mod.common.api.codec.CodecMapped
import com.cobblemon.mod.common.api.data.ArbitrarilyMappedSerializableCompanion
import com.cobblemon.mod.common.util.codec.VECTOR3F_CODEC
import com.cobblemon.mod.common.util.math.hamiltonProduct
import com.mojang.serialization.Codec
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.codecs.PrimitiveCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.RotationAxis
import net.minecraft.util.math.Vec3d
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.Optional
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

interface ParticleCameraMode : CodecMapped {
    companion object : ArbitrarilyMappedSerializableCompanion<ParticleCameraMode, ParticleCameraModeType>(
        keyFromString = ParticleCameraModeType::valueOf,
        stringFromKey = { it.name },
        keyFromValue = { it.type }
    ) {
        init {
            registerSubtype(ParticleCameraModeType.ROTATE_XYZ, RotateXYZCameraMode::class.java, RotateXYZCameraMode.CODEC)
            registerSubtype(ParticleCameraModeType.ROTATE_Y, RotateYCameraMode::class.java, RotateYCameraMode.CODEC)
            registerSubtype(ParticleCameraModeType.LOOK_AT_XYZ, LookAtXYZ::class.java, LookAtXYZ.CODEC)
            registerSubtype(ParticleCameraModeType.LOOK_AT_Y, LookAtY::class.java, LookAtY.CODEC)
            registerSubtype(ParticleCameraModeType.DIRECTION_X, DirectionX::class.java, DirectionX.CODEC)
            registerSubtype(ParticleCameraModeType.DIRECTION_Y, DirectionY::class.java, DirectionY.CODEC)
            registerSubtype(ParticleCameraModeType.DIRECTION_Z, DirectionZ::class.java, DirectionZ.CODEC)
            registerSubtype(ParticleCameraModeType.LOOK_AT_DIRECTION, LookAtDirection::class.java, LookAtDirection.CODEC)
            registerSubtype(ParticleCameraModeType.EMITTER_XZ_PLANE, EmitterXZPlane::class.java, EmitterXZPlane.CODEC)
            registerSubtype(ParticleCameraModeType.EMITTER_XY_PLANE, EmitterXYPlane::class.java, EmitterXYPlane.CODEC)
            registerSubtype(ParticleCameraModeType.EMITTER_YZ_PLANE, EmitterYZPlane::class.java, EmitterYZPlane.CODEC)
        }
    }

    val type: ParticleCameraModeType
    fun getRotation(
        prevAngle: Float,
        angle: Float,
        deltaTicks: Float,
        cameraAngle: Quaternionf,
        cameraYaw: Float,
        cameraPitch: Float,
        initialVelocity: Vec3d
    ): Quaternionf
}

class RotateXYZCameraMode : ParticleCameraMode {
    companion object {
        val CODEC: Codec<RotateXYZCameraMode> = RecordCodecBuilder.create { instance ->
            instance.group(
                PrimitiveCodec.STRING.fieldOf("type").forGetter { it.type.name }
            ).apply(instance) { RotateXYZCameraMode() }
        }
    }

    override val type = ParticleCameraModeType.ROTATE_XYZ

    override fun getRotation(
        prevAngle: Float,
        angle: Float,
        deltaTicks: Float,
        cameraAngle: Quaternionf,
        cameraYaw: Float,
        cameraPitch: Float,
        initialVelocity: Vec3d
    ): Quaternionf {
        val i = if (angle == 0.0f) 0F else MathHelper.lerp(deltaTicks, prevAngle, angle)
        val q = Quaternionf(cameraAngle)
        q.hamiltonProduct(RotationAxis.POSITIVE_Z.rotationDegrees(i))
        return q
    }

    override fun <T> encode(ops: DynamicOps<T>) = CODEC.encodeStart(ops, this)
    override fun readFromBuffer(buffer: PacketByteBuf) {}
    override fun writeToBuffer(buffer: PacketByteBuf) {}
}

class RotateYCameraMode : ParticleCameraMode {
    companion object {
        val CODEC: Codec<RotateYCameraMode> = RecordCodecBuilder.create { instance ->
            instance.group(
                PrimitiveCodec.STRING.fieldOf("type").forGetter { it.type.name }
            ).apply(instance) { RotateYCameraMode() }
        }
    }

    override val type = ParticleCameraModeType.ROTATE_Y

    override fun getRotation(
        prevAngle: Float,
        angle: Float,
        deltaTicks: Float,
        cameraAngle: Quaternionf,
        cameraYaw: Float,
        cameraPitch: Float,
        initialVelocity: Vec3d
    ): Quaternionf {
        val i = if (angle == 0F) 0F else MathHelper.lerp(deltaTicks, prevAngle, angle)

        val q2 = RotationAxis.POSITIVE_Y.rotationDegrees(/*180 - */cameraYaw)
        q2.hamiltonProduct(RotationAxis.POSITIVE_Z.rotationDegrees(i))

        return q2
//        val xyz = cameraAngle.toEulerXyz()
//        xyz.set(0f, xyz.y, 0f)
//        val q = Quaternion.fromEulerXyz(xyz)
//        q.hamiltonProduct(Vec3f.POSITIVE_Z.getDegreesQuaternion(i))
//        return q
    }

    override fun <T> encode(ops: DynamicOps<T>) = CODEC.encodeStart(ops, this)
    override fun readFromBuffer(buffer: PacketByteBuf) {}
    override fun writeToBuffer(buffer: PacketByteBuf) {}
}

class LookAtXYZ : ParticleCameraMode {
    companion object {
        val CODEC: Codec<LookAtXYZ> = RecordCodecBuilder.create { instance ->
            instance.group(
                PrimitiveCodec.STRING.fieldOf("type").forGetter { it.type.name }
            ).apply(instance) { LookAtXYZ() }
        }
    }

    override val type = ParticleCameraModeType.LOOK_AT_XYZ
    override fun <T> encode(ops: DynamicOps<T>) = CODEC.encodeStart(ops, this)
    override fun readFromBuffer(buffer: PacketByteBuf) {}
    override fun writeToBuffer(buffer: PacketByteBuf) {}

    override fun getRotation(
        prevAngle: Float,
        angle: Float,
        deltaTicks: Float,
        cameraAngle: Quaternionf,
        cameraYaw: Float,
        cameraPitch: Float,
        initialVelocity: Vec3d
    ): Quaternionf {
        val i = if (angle == 0F) 0F else MathHelper.lerp(deltaTicks, prevAngle, angle)
        val rotation = Quaternionf(0F, 0F, 0F, 1F)
        rotation.hamiltonProduct(RotationAxis.POSITIVE_Y.rotationDegrees(-cameraYaw))
        rotation.hamiltonProduct(RotationAxis.POSITIVE_X.rotationDegrees(cameraPitch))
        rotation.hamiltonProduct(RotationAxis.POSITIVE_Z.rotationDegrees(i))
        return rotation
    }
}

class LookAtY() : ParticleCameraMode {
    override val type: ParticleCameraModeType = ParticleCameraModeType.LOOK_AT_Y
    companion object {
        val CODEC: Codec<LookAtY> = RecordCodecBuilder.create { instance ->
            instance.group(
                PrimitiveCodec.STRING.fieldOf("type").forGetter { it.type.name }
            ).apply(instance) { LookAtY() }
        }
    }
    override fun getRotation(
        prevAngle: Float,
        angle: Float,
        deltaTicks: Float,
        cameraAngle: Quaternionf,
        cameraYaw: Float,
        cameraPitch: Float,
        initialVelocity: Vec3d
    ): Quaternionf {
        val i = if (angle == 0F) 0F else MathHelper.lerp(deltaTicks, prevAngle, angle)
        val rotation = Quaternionf(0F, 0F, 0F, 1F)
        rotation.hamiltonProduct(RotationAxis.POSITIVE_Y.rotationDegrees(-cameraYaw))
        return rotation
    }

    override fun <T> encode(ops: DynamicOps<T>) = CODEC.encodeStart(ops, this)


    override fun readFromBuffer(buffer: PacketByteBuf) {
    }

    override fun writeToBuffer(buffer: PacketByteBuf) {
    }
}
class DirectionZ(directionVector: Vector3f, minSpeed: Float = 0.01f): ParticleCameraMode {
    override val type: ParticleCameraModeType = ParticleCameraModeType.DIRECTION_Z
    companion object {
        val CODEC: Codec<DirectionZ> = RecordCodecBuilder.create { instance ->
            instance.group(
                PrimitiveCodec.STRING.fieldOf("type").forGetter { it.type.name },
                VECTOR3F_CODEC.fieldOf("direction_vector").forGetter { it.directionVector },
                PrimitiveCodec.FLOAT.fieldOf("min_speed").forGetter { it.minSpeed }
            ).apply(instance) { type, directionVector, minSpeed ->
                DirectionZ(directionVector, minSpeed)
            }
        }
    }
    val directionVector: Vector3f
    val minSpeed: Float

    init {
        this.directionVector = directionVector
        this.minSpeed = minSpeed
    }

    override fun getRotation(
        prevAngle: Float,
        angle: Float,
        deltaTicks: Float,
        cameraAngle: Quaternionf,
        cameraYaw: Float,
        cameraPitch: Float,
        initialVelocity: Vec3d
    ): Quaternionf {
        val i = if (angle == 0F) 0F else MathHelper.lerp(deltaTicks, prevAngle, angle)
        val rotation = Quaternionf(0F, 0F, 0F, 1F)
        var y = atan2(directionVector.x.toDouble(), directionVector.z.toDouble())
        var x = atan2(directionVector.y.toDouble(), sqrt(directionVector.x.toDouble().pow(2.0) + directionVector.z.toDouble().pow(2.0)))
        if (initialVelocity.length() > minSpeed) {
            y = atan2(initialVelocity.x, initialVelocity.z)
            x = atan2(initialVelocity.y, sqrt(initialVelocity.x.pow(2.0) + initialVelocity.z.pow(2.0)))
        }
        rotation.hamiltonProduct(RotationAxis.POSITIVE_X.rotationDegrees(-x.toFloat()))
        rotation.hamiltonProduct(RotationAxis.POSITIVE_Y.rotationDegrees(y.toFloat()))
        return rotation
    }

    override fun <T> encode(ops: DynamicOps<T>) = CODEC.encodeStart(ops, this)

    override fun readFromBuffer(buffer: PacketByteBuf) {
    }

    override fun writeToBuffer(buffer: PacketByteBuf) {
    }
}
class EmitterYZPlane(): ParticleCameraMode {
    override val type: ParticleCameraModeType = ParticleCameraModeType.EMITTER_YZ_PLANE
    companion object {
        val CODEC: Codec<EmitterYZPlane> = RecordCodecBuilder.create { instance ->
            instance.group(
                PrimitiveCodec.STRING.fieldOf("type").forGetter { it.type.name }
            ).apply(instance) { EmitterYZPlane() }
        }
    }

    override fun getRotation(
        prevAngle: Float,
        angle: Float,
        deltaTicks: Float,
        cameraAngle: Quaternionf,
        cameraYaw: Float,
        cameraPitch: Float,
        initialVelocity: Vec3d
    ): Quaternionf {
        val rotation = Quaternionf(0F, 0F, 0F, 1F)
        rotation.hamiltonProduct(RotationAxis.POSITIVE_Y.rotationDegrees((PI/2f).toFloat()))
        return rotation
    }

    override fun <T> encode(ops: DynamicOps<T>) = CODEC.encodeStart(ops, this)

    override fun readFromBuffer(buffer: PacketByteBuf) {
    }

    override fun writeToBuffer(buffer: PacketByteBuf) {
    }

}
class EmitterXZPlane(): ParticleCameraMode {
    override val type: ParticleCameraModeType = ParticleCameraModeType.EMITTER_XZ_PLANE
    companion object {
        val CODEC: Codec<EmitterXZPlane> = RecordCodecBuilder.create { instance ->
            instance.group(
                PrimitiveCodec.STRING.fieldOf("type").forGetter { it.type.name }
            ).apply(instance) { EmitterXZPlane() }
        }
    }

    override fun getRotation(
        prevAngle: Float,
        angle: Float,
        deltaTicks: Float,
        cameraAngle: Quaternionf,
        cameraYaw: Float,
        cameraPitch: Float,
        initialVelocity: Vec3d
    ): Quaternionf {
        val rotation = Quaternionf(0F, 0F, 0F, 1F)
        rotation.hamiltonProduct(RotationAxis.POSITIVE_X.rotationDegrees((-PI/2f).toFloat()))
        return rotation
    }

    override fun <T> encode(ops: DynamicOps<T>) = CODEC.encodeStart(ops, this)

    override fun readFromBuffer(buffer: PacketByteBuf) {
    }

    override fun writeToBuffer(buffer: PacketByteBuf) {
    }

}

class EmitterXYPlane(): ParticleCameraMode {
    override val type: ParticleCameraModeType = ParticleCameraModeType.EMITTER_XY_PLANE
    companion object {
        val CODEC: Codec<EmitterXYPlane> = RecordCodecBuilder.create { instance ->
            instance.group(
                PrimitiveCodec.STRING.fieldOf("type").forGetter { it.type.name }
            ).apply(instance) { EmitterXYPlane() }
        }
    }

    override fun getRotation(
        prevAngle: Float,
        angle: Float,
        deltaTicks: Float,
        cameraAngle: Quaternionf,
        cameraYaw: Float,
        cameraPitch: Float,
        initialVelocity: Vec3d
    ): Quaternionf {
        return Quaternionf(0F, 0F, 0F, 1F)
    }

    override fun <T> encode(ops: DynamicOps<T>) = CODEC.encodeStart(ops, this)

    override fun readFromBuffer(buffer: PacketByteBuf) {
    }

    override fun writeToBuffer(buffer: PacketByteBuf) {
    }

}
class DirectionY(directionVector: Vector3f, minSpeed: Float = 0.01f): ParticleCameraMode {
    override val type: ParticleCameraModeType = ParticleCameraModeType.DIRECTION_Y
    companion object {
        val CODEC: Codec<DirectionY> = RecordCodecBuilder.create { instance ->
            instance.group(
                PrimitiveCodec.STRING.fieldOf("type").forGetter { it.type.name },
                VECTOR3F_CODEC.fieldOf("direction_vector").forGetter { it.directionVector },
                PrimitiveCodec.FLOAT.fieldOf("min_speed").forGetter { it.minSpeed }
            ).apply(instance) { type, directionVector, minSpeed ->
                DirectionY(directionVector, minSpeed)
            }
        }
    }
    val directionVector: Vector3f
    val minSpeed: Float

    init {
        this.directionVector = directionVector
        this.minSpeed = minSpeed
    }

    override fun getRotation(
        prevAngle: Float,
        angle: Float,
        deltaTicks: Float,
        cameraAngle: Quaternionf,
        cameraYaw: Float,
        cameraPitch: Float,
        initialVelocity: Vec3d
    ): Quaternionf {
        val i = if (angle == 0F) 0F else MathHelper.lerp(deltaTicks, prevAngle, angle)
        val rotation = Quaternionf(0F, 0F, 0F, 1F)
        var y = atan2(directionVector.x.toDouble(), directionVector.z.toDouble())
        var x = atan2(directionVector.y.toDouble(), sqrt(directionVector.x.toDouble().pow(2.0) + directionVector.z.toDouble().pow(2.0)))
        if (initialVelocity.length() > minSpeed) {
            y = atan2(initialVelocity.x, initialVelocity.z)
            x = atan2(initialVelocity.y, sqrt(initialVelocity.x.pow(2.0) + initialVelocity.z.pow(2.0)))
        }
        rotation.hamiltonProduct(RotationAxis.POSITIVE_X.rotationDegrees(x.toFloat() - PI.toFloat()/2f))
        rotation.hamiltonProduct(RotationAxis.POSITIVE_Y.rotationDegrees(y.toFloat() - PI.toFloat()))
        return rotation
    }

    override fun <T> encode(ops: DynamicOps<T>) = CODEC.encodeStart(ops, this)

    override fun readFromBuffer(buffer: PacketByteBuf) {
    }

    override fun writeToBuffer(buffer: PacketByteBuf) {
    }
}

class DirectionX(directionVector: Vector3f, minSpeed: Float = 0.01f): ParticleCameraMode {
    override val type: ParticleCameraModeType = ParticleCameraModeType.DIRECTION_X
    companion object {
        val CODEC: Codec<DirectionX> = RecordCodecBuilder.create { instance ->
            instance.group(
                PrimitiveCodec.STRING.fieldOf("type").forGetter { it.type.name },
                VECTOR3F_CODEC.fieldOf("direction_vector").forGetter { it.directionVector },
                PrimitiveCodec.FLOAT.fieldOf("min_speed").forGetter { it.minSpeed }
            ).apply(instance) { type, directionVector, minSpeed ->
                DirectionX(directionVector, minSpeed)
            }
        }
    }
    val directionVector: Vector3f
    val minSpeed: Float
    init {
        this.directionVector = directionVector
        this.minSpeed = minSpeed
    }

    override fun getRotation(
        prevAngle: Float,
        angle: Float,
        deltaTicks: Float,
        cameraAngle: Quaternionf,
        cameraYaw: Float,
        cameraPitch: Float,
        initialVelocity: Vec3d
    ): Quaternionf {
        val i = if (angle == 0F) 0F else MathHelper.lerp(deltaTicks, prevAngle, angle)
        val rotation = Quaternionf(0F, 0F, 0F, 1F)
        var y = atan2(directionVector.x.toDouble(), directionVector.z.toDouble())
        var z = atan2(directionVector.y.toDouble(), sqrt(directionVector.x.toDouble().pow(2.0) + directionVector.z.toDouble().pow(2.0)))
        if (initialVelocity.length() > minSpeed) {
            y = atan2(initialVelocity.x, initialVelocity.z)
            z = atan2(initialVelocity.y, sqrt(initialVelocity.x.pow(2.0) + initialVelocity.z.pow(2.0)))
        }
        rotation.hamiltonProduct(RotationAxis.POSITIVE_Y.rotationDegrees(y.toFloat() - PI.toFloat()/2f))
        rotation.hamiltonProduct(RotationAxis.POSITIVE_Z.rotationDegrees(z.toFloat()))
        return rotation
    }

    override fun <T> encode(ops: DynamicOps<T>) = CODEC.encodeStart(ops, this)

    override fun readFromBuffer(buffer: PacketByteBuf) {
    }

    override fun writeToBuffer(buffer: PacketByteBuf) {
    }
}

class LookAtDirection(direction: Direction, directionVector: Optional<Vector3f>) : ParticleCameraMode {
    override val type: ParticleCameraModeType = ParticleCameraModeType.LOOK_AT_DIRECTION
    companion object {
        val CODEC: Codec<LookAtDirection> = RecordCodecBuilder.create { instance ->
            instance.group(
                PrimitiveCodec.STRING.fieldOf("type").forGetter { it.type.name },
                Codec.STRING.fieldOf("direction").xmap({s->Direction.valueOf(s)}, {s -> s.name}).forGetter { it.direction },
                VECTOR3F_CODEC.optionalFieldOf("direction_vector").forGetter { it.directionVector }
            ).apply(instance) { type, direction, directionVector ->
                LookAtDirection(direction, directionVector)
            }
        }
    }
    val direction: Direction
    val directionVector: Optional<Vector3f>

    init {
        this.direction = direction
        this.directionVector = directionVector
    }
    override fun getRotation(
        prevAngle: Float,
        angle: Float,
        deltaTicks: Float,
        cameraAngle: Quaternionf,
        cameraYaw: Float,
        cameraPitch: Float,
        initialVelocity: Vec3d
    ): Quaternionf {
        val i = if (angle == 0F) 0F else MathHelper.lerp(deltaTicks, prevAngle, angle)
        val rotation = Quaternionf(0F, 0F, 0F, 1F)
        if(directionVector.isPresent){
            val vec = directionVector.get()
            val normalizedVec: Vector3f = vec.normalize()
            rotation.hamiltonProduct(RotationAxis.POSITIVE_Y.rotationDegrees(normalizedVec.x * -cameraYaw))
            rotation.hamiltonProduct(RotationAxis.POSITIVE_X.rotationDegrees(normalizedVec.y * cameraPitch))
            rotation.hamiltonProduct(RotationAxis.POSITIVE_Z.rotationDegrees(normalizedVec.z * i))
        } else {
            val vec = Vector3f(initialVelocity.x.toFloat(), initialVelocity.y.toFloat(), initialVelocity.z.toFloat())
            vec.normalize()
            rotation.hamiltonProduct(RotationAxis.POSITIVE_Y.rotationDegrees(vec.x * -cameraYaw))
            rotation.hamiltonProduct(RotationAxis.POSITIVE_X.rotationDegrees(vec.y * cameraPitch))
            rotation.hamiltonProduct(RotationAxis.POSITIVE_Z.rotationDegrees(vec.z * i))
        }
        return rotation
    }

    override fun <T> encode(ops: DynamicOps<T>) = CODEC.encodeStart(ops, this)


    override fun readFromBuffer(buffer: PacketByteBuf) {
    }

    override fun writeToBuffer(buffer: PacketByteBuf) {
    }

    enum class Direction{
        FROM_MOTION,
        CUSTOM
    }
}



enum class ParticleCameraModeType {
    ROTATE_XYZ,
    ROTATE_Y,
    LOOK_AT_XYZ,
    LOOK_AT_Y,
    LOOK_AT_DIRECTION,
    DIRECTION_X,
    DIRECTION_Y,
    DIRECTION_Z,
    EMITTER_XY_PLANE,
    EMITTER_XZ_PLANE,
    EMITTER_YZ_PLANE
}