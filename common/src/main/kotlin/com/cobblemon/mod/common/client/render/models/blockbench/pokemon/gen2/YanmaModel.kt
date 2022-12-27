/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2

import com.cobblemon.mod.common.client.render.models.blockbench.asTransformed
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BiWingedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.TransformedModelPart.Companion.Y_AXIS
import com.cobblemon.mod.common.client.render.models.blockbench.pose.TransformedModelPart.Companion.Z_AXIS
import com.cobblemon.mod.common.client.render.models.blockbench.wavefunction.triangleFunction
import com.cobblemon.mod.common.entity.PoseType.Companion.MOVING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class YanmaModel(root: ModelPart) : PokemonPoseableModel(), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("yanma")
    override val head = getPart("head")

    override val portraitScale = 1.6F
    override val portraitTranslation = Vec3d(-.35, 0.015, 0.0)

    override val profileScale = 1.0F
    override val profileTranslation = Vec3d(0.0, 0.0, 0.0)

    lateinit var standing: PokemonPose
    lateinit var walk: PokemonPose

    override fun registerPoses() {
        val wingFrame1 = object : BiWingedFrame {
            override val rootPart = this@YanmaModel.rootPart
            override val leftWing = getPart("wing_left1")
            override val rightWing = getPart("wing_right1")
        }

        val wingFrame2 = object : BiWingedFrame {
            override val rootPart = this@YanmaModel.rootPart
            override val leftWing = getPart("wing_left2")
            override val rightWing = getPart("wing_right2")
        }

        standing = registerPose(
            poseName = "standing",
            poseTypes = STATIONARY_POSES + UI_POSES,
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("yanma", "ground_idle"),
                wingFrame1.wingFlap(
                    flapFunction = triangleFunction(period = 0.2F, amplitude = 0.4F),
                    timeVariable = { state, _, ageInTicks -> state?.animationSeconds ?: ageInTicks },
                    axis = Z_AXIS
                ),
                wingFrame2.wingFlap(
                    flapFunction = triangleFunction(period = 0.2F, amplitude = 0.4F),
                    timeVariable = { state, _, ageInTicks -> 0.01F + (state?.animationSeconds ?: (ageInTicks / 20)) },
                    axis = Z_AXIS
                )
            ),
            transformedParts = arrayOf(
                rootPart.asTransformed().addPosition(Y_AXIS, -4)
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = MOVING_POSES,
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("yanma", "ground_idle"),
                wingFrame1.wingFlap(
                    flapFunction = triangleFunction(period = 0.2F, amplitude = 0.4F),
                    timeVariable = { state, _, ageInTicks -> state?.animationSeconds ?: ageInTicks },
                    axis = Z_AXIS
                ),
                wingFrame2.wingFlap(
                    flapFunction = triangleFunction(period = 0.2F, amplitude = 0.4F),
                    timeVariable = { state, _, ageInTicks -> 0.01F + (state?.animationSeconds ?: (ageInTicks / 20)) },
                    axis = Z_AXIS
                )
                //bedrock("yanma", "ground_walk")
            ),
            transformedParts = arrayOf(
                rootPart.asTransformed().addPosition(Y_AXIS, -4)
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PoseableEntityState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("yanma", "faint") else null
}