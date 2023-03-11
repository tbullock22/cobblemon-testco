/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render

import net.minecraft.util.math.Matrix4f

/**
 * Holds onto a space matrix for quick access, exposes the matrix to mutation.
 *
 * @author Hiroku
 * @since February 10th, 2023
 */
class MatrixWrapper {
    var matrix: Matrix4f = Matrix4f()

    fun update(rotationMatrix: Matrix4f): MatrixWrapper {
        this.matrix = rotationMatrix.copy()
        return this
    }
}