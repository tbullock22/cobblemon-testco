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
import com.mojang.serialization.Codec
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.codecs.PrimitiveCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Quaternion
import net.minecraft.util.math.Vec3f

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
        }
    }

    val type: ParticleCameraModeType
    fun getRotation(prevAngle: Float, angle: Float, deltaTicks: Float, cameraAngle: Quaternion, cameraYaw: Float, cameraPitch: Float): Quaternion
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

    override fun getRotation(prevAngle: Float, angle: Float, deltaTicks: Float, cameraAngle: Quaternion, cameraYaw: Float, cameraPitch: Float): Quaternion {
        val i = if (angle == 0.0f) 0F else MathHelper.lerp(deltaTicks, prevAngle, angle)
        val q = Quaternion(cameraAngle)
        q.hamiltonProduct(Vec3f.POSITIVE_Z.getDegreesQuaternion(i))
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

    override fun getRotation(prevAngle: Float, angle: Float, deltaTicks: Float, cameraAngle: Quaternion, cameraYaw: Float, cameraPitch: Float): Quaternion {
        val i = if (angle == 0F) 0F else MathHelper.lerp(deltaTicks, prevAngle, angle)
        val xyz = cameraAngle.toEulerXyz()
        xyz.set(0f, xyz.y, 0f)
        val q = Quaternion.fromEulerXyz(xyz)
        q.hamiltonProduct(Vec3f.POSITIVE_Z.getDegreesQuaternion(i))
        return q
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

    override fun getRotation(prevAngle: Float, angle: Float, deltaTicks: Float, cameraAngle: Quaternion, cameraYaw: Float, cameraPitch: Float): Quaternion {
        val i = if (angle == 0F) 0F else MathHelper.lerp(deltaTicks, prevAngle, angle)
        val rotation = Quaternion(0F, 0F, 0F, 1F)
//        rotation.hamiltonProduct(Vec3f.POSITIVE_Y.getDegreesQuaternion(-cameraYaw))
//        rotation.hamiltonProduct(Vec3f.POSITIVE_X.getDegreesQuaternion(cameraPitch))
//        rotation.hamiltonProduct(Vec3f.POSITIVE_Z.getDegreesQuaternion(i))
        return rotation
    }
}

//class LookAtYCameraMode() : ParticleCameraMode {
//    override val type: ParticleCameraModeType = ParticleCameraModeType.LOOK_AT_Y
//}



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