/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.EarJoint
import com.cobblemon.mod.common.client.render.models.blockbench.RangeOfMotion
import com.cobblemon.mod.common.client.render.models.blockbench.asTransformed
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BiWingedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.EaredFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.TransformedModelPart.Companion.X_AXIS
import com.cobblemon.mod.common.client.render.models.blockbench.pose.TransformedModelPart.Companion.Y_AXIS
import com.cobblemon.mod.common.client.render.models.blockbench.pose.TransformedModelPart.Companion.Z_AXIS
import com.cobblemon.mod.common.client.render.models.blockbench.wavefunction.sineFunction
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.ALL_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.SHOULDER_POSES
import com.cobblemon.mod.common.util.math.geometry.toRadians
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.MathConstants.PI
import net.minecraft.util.math.Vec3d

class ZubatModel(root: ModelPart) : PokemonPoseableModel(), BiWingedFrame, EaredFrame {
    override val rootPart = root.registerChildWithAllChildren("zubat")

    override val leftWing = getPart("leftwing")
    override val rightWing = getPart("rightwing")

    private val leftEar = getPart("leftear")
    private val rightEar = getPart("rightear")
    override val leftEarJoint = EarJoint(leftEar, Z_AXIS, RangeOfMotion(70F.toRadians(), 40F.toRadians()))
    override val rightEarJoint = EarJoint(rightEar, Z_AXIS, RangeOfMotion((-70F).toRadians(), (-40F).toRadians()))

    override val portraitScale = 2.3F
    override val portraitTranslation = Vec3d(0.0, -0.5, 0.0)
    override val profileScale = 1.0F
    override val profileTranslation = Vec3d(0.0, 0.36, 0.0)

    lateinit var sleep: PokemonPose

    override fun registerPoses() {
        sleep = registerPose(
                poseType = PoseType.SLEEP,
                idleAnimations = arrayOf(bedrock("zubat", "sleep"))
        )

        registerPose(
            poseName = "hover",
            poseTypes = ALL_POSES - SHOULDER_POSES - PoseType.FLY - PoseType.SLEEP,
            idleAnimations = arrayOf(
                bedrock("zubat", "ground_idle")
            )
        )

        registerPose(
            poseName = "fly",
            poseType = PoseType.FLY,
            idleAnimations = arrayOf(
                bedrock("zubat", "ground_walk")
            )
        )

        registerPose(
            poseType = PoseType.SHOULDER_LEFT,
            idleAnimations = arrayOf(
                leftWing.rotation(
                    function = sineFunction(
                        amplitude = PI / 3,
                        period = 1F
                    ),
                    axis = Z_AXIS,
                    timeVariable = { _, _, ageInTicks -> ageInTicks / 20 },
                )
            ),
            transformedParts = arrayOf(
                rootPart.asTransformed().addRotation(X_AXIS, PI / 9).addPosition(Y_AXIS, 4F).addPosition(Z_AXIS, 3F),
                leftWing.asTransformed().addRotation(X_AXIS, PI / 3),
                rightWing.asTransformed().addRotation(X_AXIS, PI / 3).addRotation(Z_AXIS, -PI / 2)
            )
        )
        registerPose(
            poseType = PoseType.SHOULDER_RIGHT,
            idleAnimations = arrayOf(
                rightWing.rotation(
                    function = sineFunction(
                        amplitude = PI / 3,
                        period = 1F
                    ),
                    axis = Z_AXIS,
                    timeVariable = { _, _, ageInTicks -> ageInTicks / 20 },
                )
            ),
            transformedParts = arrayOf(
                rootPart.asTransformed().addRotation(X_AXIS, PI / 9).addPosition(Y_AXIS, 4F).addPosition(Z_AXIS, 3F),
                leftWing.asTransformed().addRotation(X_AXIS, PI / 3).addRotation(Z_AXIS, PI / 2),
                rightWing.asTransformed().addRotation(X_AXIS, PI / 3)
            )
        )
    }
}