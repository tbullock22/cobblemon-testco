/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.frame.BiWingedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BipedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.TransformedModelPart
import com.cobblemon.mod.common.client.render.models.blockbench.wavefunction.parabolaFunction
import com.cobblemon.mod.common.client.render.models.blockbench.wavefunction.sineFunction
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.SHOULDER_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import com.cobblemon.mod.common.util.math.geometry.toRadians
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class PidgeyModel(root: ModelPart) : PokemonPoseableModel(), HeadedFrame, BipedFrame, BiWingedFrame{
    override val rootPart = root.registerChildWithAllChildren("pidgey")
    override val leftWing = getPart("wing_left")
    override val rightWing = getPart("wing_right")
    override val leftLeg = getPart("leg_left")
    override val rightLeg = getPart("leg_right")
    override val head = getPart("head")
    private val tail = getPart("tail")

    override val portraitScale = 1.95F
    override val portraitTranslation = Vec3d(-0.05, -0.7, 0.0)

    override val profileScale = 1.0F
    override val profileTranslation = Vec3d(0.0, 0.0, 0.0)

    lateinit var stand: PokemonPose
    lateinit var walk: PokemonPose
    lateinit var hover: PokemonPose
    lateinit var fly: PokemonPose

    override fun registerPoses() {
        stand = registerPose(
            poseName = "standing",
            poseTypes = SHOULDER_POSES + UI_POSES + PoseType.STAND,
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("0016_pidgey/pidgey", "ground_idle")
            )
        )

        hover = registerPose(
            poseName = "hover",
            poseType = PoseType.HOVER,
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("0016_pidgey/pidgey", "air_idle")
            )
        )

        fly = registerPose(
            poseName = "fly",
            poseType = PoseType.FLY,
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("0016_pidgey/pidgey", "air_fly")
            )
        )

        walk = registerPose(
            poseName = "walking",
            poseType = PoseType.WALK,
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("0016_pidgey/pidgey", "ground_idle"),
                rootPart.translation(
                    function = parabolaFunction(
                        peak = -4F,
                        period = 0.4F
                    ),
                    timeVariable = { state, _, _ -> state?.animationSeconds },
                    axis = TransformedModelPart.Y_AXIS
                ),
                head.translation(
                    function = sineFunction(
                        amplitude = (-20F).toRadians(),
                        period = 1F,
                        verticalShift = (-10F).toRadians()
                    ),
                    axis = TransformedModelPart.X_AXIS,
                    timeVariable = { state, _, _ -> state?.animationSeconds }
                ),
                leftLeg.rotation(
                    function = parabolaFunction(
                        tightness = -20F,
                        phaseShift = 0F,
                        verticalShift = (30F).toRadians()
                    ),
                    axis = TransformedModelPart.X_AXIS,
                    timeVariable = { _, _, ageInTicks -> ageInTicks / 20 },
                ),
                rightLeg.rotation(
                    function = parabolaFunction(
                        tightness = -20F,
                        phaseShift = 0F,
                        verticalShift = (30F).toRadians()
                    ),
                    axis = TransformedModelPart.X_AXIS,
                    timeVariable = { _, _, ageInTicks -> ageInTicks / 20 },
                ),
                tail.rotation(
                    function = sineFunction(
                        amplitude = (-5F).toRadians(),
                        period = 1F
                    ),
                    axis = TransformedModelPart.X_AXIS,
                    timeVariable = { _, _, ageInTicks -> ageInTicks / 20 },
                ),
                wingFlap(
                    flapFunction = sineFunction(
                        amplitude = (-5F).toRadians(),
                        period = 0.4F,
                        phaseShift = 0.00F,
                        verticalShift = (-20F).toRadians()
                    ),
                    timeVariable = { state, _, _ -> state?.animationSeconds },
                    axis = TransformedModelPart.Z_AXIS
                ),
                rightWing.translation(
                    function = parabolaFunction(
                        tightness = -10F,
                        phaseShift = 30F,
                        verticalShift = (25F).toRadians()
                    ),
                    axis = TransformedModelPart.Y_AXIS,
                    timeVariable = { _, _, ageInTicks -> ageInTicks / 20 },
                ),
                leftWing.translation(
                    function = parabolaFunction(
                        tightness = -10F,
                        phaseShift = 30F,
                        verticalShift = (25F).toRadians()
                    ),
                    axis = TransformedModelPart.Y_AXIS,
                    timeVariable = { _, _, ageInTicks -> ageInTicks / 20 },
                ),
            )
        )
    }
}