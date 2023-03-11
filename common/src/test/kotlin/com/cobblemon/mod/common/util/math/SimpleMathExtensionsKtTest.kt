/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.math

import net.minecraft.util.math.Vec3d
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SimpleMathExtensionsKtTest {
    @Test
    fun `should get reliable rotation matrix for vectors`() {
        val vec1 = Vec3d(0.0, 1.0, 0.0)
        val vec2 = Vec3d(1.0, 0.0, 0.0)
        val rotation = getRotationMatrix(vec1, vec2)
        val vec3 = Vec3d(-2.0, 0.0, 0.0)
        val result = rotation * vec3
        assertEquals(0.0, result.x)
        assertEquals(2.0, result.y)
        assertEquals(0.0, result.z)
    }
}