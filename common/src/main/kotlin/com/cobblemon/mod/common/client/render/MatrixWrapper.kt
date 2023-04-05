/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render

import com.cobblemon.mod.common.util.math.geometry.getOrigin
import com.cobblemon.mod.common.util.math.geometry.transformPosition
import net.minecraft.util.math.Matrix4f
import net.minecraft.util.math.Vec3d

/**
 * Holds onto a space matrix for quick access, exposes the matrix to mutation.
 *
 * @author Hiroku
 * @since February 10th, 2023
 */
class MatrixWrapper {
    var position: Vec3d = Vec3d.ZERO
    var matrix: Matrix4f = Matrix4f()

    fun updateMatrix(rotationMatrix: Matrix4f) = apply {
        this.matrix = rotationMatrix.copy()
    }

    fun updatePosition(position: Vec3d) = apply {
        this.position = position
    }

    fun getOrigin() = position.add(matrix.getOrigin())
    fun transformPosition(position: Vec3d) = this.position.add(matrix.transformPosition(position))
}