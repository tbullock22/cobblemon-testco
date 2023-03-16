/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.math.geometry

import net.minecraft.util.math.Matrix4f
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vector4f

fun Matrix4f.getOrigin(): Vec3d {
    return transformPosition(Vec3d.ZERO)
}

fun Matrix4f.transformPosition(pos: Vec3d): Vec3d {
    val vector = Vector4f(pos.x.toFloat(), pos.y.toFloat(), pos.z.toFloat(), 1F)
    vector.transform(this)
    vector.normalizeProjectiveCoordinates()
    return Vec3d(vector.x.toDouble(), vector.y.toDouble(), vector.z.toDouble())
}

fun Matrix4f.transformDirection(direction: Vec3d): Vec3d {
    val origin = Vector4f(0F, 0F, 0F, 1F)
    origin.transform(this)
    origin.normalizeProjectiveCoordinates()
    val originVec = Vec3d(origin.x.toDouble(), origin.y.toDouble(), origin.z.toDouble())
    val magnitude = direction.length()
    val vector = Vector4f(direction.x.toFloat(), direction.y.toFloat(), direction.z.toFloat(), 1F)
    vector.transform(this)
    vector.normalizeProjectiveCoordinates()
    val newVector = Vec3d(vector.x.toDouble(), vector.y.toDouble(), vector.z.toDouble())
        .subtract(originVec)
        .normalize()
        .multiply(magnitude)
    return newVector
}