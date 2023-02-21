/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pose

import com.cobblemon.mod.common.util.math.geometry.toRadians
import net.minecraft.client.model.ModelPart

/**
 * Represents a [ModelPart] with some changes to position and rotation. This is to take a snapshot
 * and store mutations for the purpose of poses.
 *
 * @author Hiroku
 * @since December 5th, 2021
 */
class TransformedModelPart(
    /** The [ModelPart] that is being transformed. */
    val modelPart: ModelPart
) {
    companion object {
        const val X_AXIS = 0
        const val Y_AXIS = 1
        const val Z_AXIS = 2
    }

    var changeFactor = 1F

    val initialPosition = floatArrayOf(modelPart.pivotX, modelPart.pivotY, modelPart.pivotZ)
    val initialRotation = floatArrayOf(modelPart.pitch, modelPart.yaw, modelPart.roll)

    var position = floatArrayOf(modelPart.pivotX, modelPart.pivotY, modelPart.pivotZ)
    var rotation = floatArrayOf(modelPart.pitch, modelPart.yaw, modelPart.roll)

    var originalVisibility = true
    var visibility = originalVisibility

    /** Applies the transformation to the model part. */
    fun apply() {
        modelPart.setPivot(position[0], position[1], position[2])
        modelPart.setAngles(rotation[0], rotation[1], rotation[2])
        modelPart.visible = visibility
    }

    /** Sets the part back to its original location, prior to this transformation. */
    fun applyDefaults() {
        modelPart.setPivot(initialPosition[0], initialPosition[1], initialPosition[2])
        modelPart.setAngles(initialRotation[0], initialRotation[1], initialRotation[2])
        visibility = originalVisibility
    }

    fun withVisibility(visibility: Boolean): TransformedModelPart {
        this.visibility = visibility
        return this
    }

    var xPos: Float
        get() = position[0]
        set(value) {
            position[0] = value
        }
    var yPos: Float
        get() = position[1]
        set(value) {
            position[1] = value
        }
    var zPos: Float
        get() = position[2]
        set(value) {
            position[2] = value
        }
    var pitch: Float
        get() = rotation[0]
        set(value) {
            rotation[0] = value
        }
    var yaw: Float
        get() = rotation[1]
        set(value) {
            rotation[1] = value
        }
    var roll: Float
        get() = rotation[2]
        set(value) {
            rotation[2] = value
        }

    fun withPosition(axis: Int, position: Number): TransformedModelPart {
        this.position[axis] = position.toFloat()
        return this
    }

    fun withPosition(xPos: Number, yPos: Number, zPos: Number): TransformedModelPart {
        return withPosition(X_AXIS, xPos).withPosition(Y_AXIS, yPos).withPosition(Z_AXIS, zPos)
    }

    fun withRotation(axis: Int, angleRadians: Number): TransformedModelPart {
        this.rotation[axis] = angleRadians.toFloat()
        return this
    }

    fun withRotation(pitch: Number, yaw: Number, roll: Number): TransformedModelPart {
        return withRotation(X_AXIS, pitch).withRotation(Y_AXIS, yaw).withRotation(Z_AXIS, roll)
    }

    fun addPosition(axis: Int, distance: Number): TransformedModelPart {
        return withPosition(axis, position[axis] + distance.toFloat() * changeFactor)
    }

    fun addPosition(xDist: Number, yDist: Number, zDist: Number): TransformedModelPart {
        return addPosition(X_AXIS, xDist).addPosition(Y_AXIS, yDist).addPosition(Z_AXIS, zDist)
    }

    fun addRotation(axis: Int, angleRadians: Number): TransformedModelPart {
        return withRotation(axis, rotation[axis] + angleRadians.toFloat() * changeFactor)
    }

    fun addRotation(pitchRadians: Number, yawRadians: Number, rollRadians: Number): TransformedModelPart {
        return addRotation(X_AXIS, pitchRadians).addRotation(Y_AXIS, yawRadians).addRotation(Z_AXIS, rollRadians)
    }

    fun addRotationDegrees(pitch: Number, yaw: Number, roll: Number): TransformedModelPart {
        return addRotation(X_AXIS, pitch.toFloat().toRadians()).addRotation(Y_AXIS, yaw.toFloat().toRadians()).addRotation(Z_AXIS, roll.toFloat().toRadians())
    }

    fun withRotationDegrees(pitch: Number, yaw: Number, roll: Number): TransformedModelPart {
        return withRotation(pitch.toFloat().toRadians(), yaw.toFloat().toRadians(), roll.toFloat().toRadians())
    }

    fun addRotationDegrees(axis: Int, angle: Number): TransformedModelPart {
        return addRotation(axis, rotation[axis] + angle.toFloat().toRadians() * changeFactor)
    }
}