/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench

import com.cobblemon.mod.common.client.render.models.blockbench.pose.TransformedModelPart
import com.cobblemon.mod.common.client.render.models.blockbench.pose.TransformedModelPart.Companion.X_AXIS
import com.cobblemon.mod.common.client.render.models.blockbench.pose.TransformedModelPart.Companion.Y_AXIS
import com.cobblemon.mod.common.util.math.geometry.toRadians
import net.minecraft.client.model.ModelPart


fun ModelPart.getPosition(axis: Int) = if (axis == X_AXIS) this.pivotX else if (axis == Y_AXIS) this.pivotY else this.pivotZ
fun ModelPart.getRotation(axis: Int) = if (axis == X_AXIS) this.pitch else if (axis == Y_AXIS) this.yaw else this.roll
fun ModelPart.setRotation(axis: Int, angleInRadians: Float): ModelPart {
    when (axis) {
        X_AXIS -> {
            pitch = angleInRadians
        }
        Y_AXIS -> {
            yaw = angleInRadians
        }
        else -> {
            roll = angleInRadians
        }
    }
    return this
}
fun ModelPart.setPosition(axis: Int, position: Float): ModelPart {
    when (axis) {
        X_AXIS -> {
            pivotX = position
        }
        Y_AXIS -> {
            pivotY = position
        }
        else -> {
            pivotZ = position
        }
    }
    return this
}
fun ModelPart.addRotation(axis: Int, differenceInRadians: Float) = setRotation(axis, getRotation(axis) + differenceInRadians)
fun ModelPart.addPosition(axis: Int, difference: Float) = setPosition(axis, getPosition(axis) + difference)
fun ModelPart.withPosition(axis: Int, position: Float) = TransformedModelPart(this).withPosition(axis, position)
fun ModelPart.withPosition(xPos: Number, yPos: Number, zPos: Number) = TransformedModelPart(this).withPosition(xPos, yPos, zPos)
fun ModelPart.withRotation(axis: Int, angleRadians: Float) = TransformedModelPart(this).withRotation(axis, angleRadians)
fun ModelPart.withRotation(xRot: Float, yRot: Float, zRot: Float) = TransformedModelPart(this).withRotation(xRot, yRot, zRot)
fun ModelPart.withRotationDegrees(xRot: Float, yRot: Float, zRot: Float) = TransformedModelPart(this).withRotation(xRot.toRadians(), yRot.toRadians(), zRot.toRadians())
fun ModelPart.asTransformed() = TransformedModelPart(this)
fun ModelPart.getChildOf(vararg path: String): ModelPart {
    var part = this
    for (piece in path) {
        part = part.getChild(piece)
    }
    return part
}
fun ModelPart.childNamed(vararg path: String): Pair<String, ModelPart> {
    var final = path.last()
    return final to getChildOf(*path)
}