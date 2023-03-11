/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.animation

import com.cobblemon.mod.common.util.math.geometry.toDegrees
import com.cobblemon.mod.common.util.toVec3d
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.cos
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.MathConstants.PI
import net.minecraft.util.math.Vec3f
import org.junit.jupiter.api.Test

internal class RootPokeBallLookAnimationTest {
    @Test
    fun `attempts at getting rotation logic working, i am not done trying angryface`() {
        val xRot = 0F
        val yRot = PI / 3
        val zRot = 0F

        val desiredZ = PI / 3F //PI / 3

        val poseStack = MatrixStack()
        poseStack.multiply(Vec3f.POSITIVE_Z.getRadialQuaternion(zRot))
        val yAxisCop = Vec3f.POSITIVE_Y.copy()
        yAxisCop.transform(poseStack.peek().normalMatrix)
        poseStack.multiply(yAxisCop.getRadialQuaternion(yRot))
        val xAxisCop = Vec3f.POSITIVE_X.copy()
        xAxisCop.transform(poseStack.peek().normalMatrix)
        poseStack.multiply(xAxisCop.getRadialQuaternion(xRot))
        val zAxis = Vec3f.POSITIVE_Z.copy()
        zAxis.transform(poseStack.peek().normalMatrix)

        println(zAxis.toVec3d())

        poseStack.multiply(zAxis.getRadialQuaternion(desiredZ))
        val y = Vec3f.POSITIVE_Y.copy()
        y.transform(poseStack.peek().normalMatrix)
        val py1 = y.x

        val z = Vec3f.POSITIVE_Z.copy()
        z.transform(poseStack.peek().normalMatrix)
        val pz1 = z.x
        val pz3 = z.z

        val beta = asin(pz1)
        val alpha = acos(pz3 / cos(beta))
        val gamma = asin(-py1 / cos(beta))

        val poseStack2 = MatrixStack()
        poseStack2.multiply(Vec3f.POSITIVE_X.getRadialQuaternion(alpha))
        poseStack2.multiply(Vec3f.POSITIVE_Y.getRadialQuaternion(beta))
        poseStack2.multiply(Vec3f.POSITIVE_Z.getRadialQuaternion(gamma))

        val alphaD = alpha.toDegrees()
        val betaD = beta.toDegrees()
        val gammaD = gamma.toDegrees()

        println("xRot: $alphaD, yRot: $betaD, zRot: $gammaD")
    }
}