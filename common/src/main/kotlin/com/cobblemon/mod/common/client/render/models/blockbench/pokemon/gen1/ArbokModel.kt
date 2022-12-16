/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.PoseableEntityState
import com.cobblemon.mod.common.client.render.models.blockbench.animation.WaveAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.animation.WaveSegment
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.TransformedModelPart
import com.cobblemon.mod.common.client.render.models.blockbench.wavefunction.sineFunction
import com.cobblemon.mod.common.entity.PoseType.Companion.MOVING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class ArbokModel(root: ModelPart) : PokemonPoseableModel(), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("arbok")
    override val head = getPart("head_ai")

    override val portraitScale = 1.3F
    override val portraitTranslation = Vec3d(-0.3, 0.95, 0.0)

    override val profileScale = 0.75F
    override val profileTranslation = Vec3d(-0.1, 0.6, 0.0)

    lateinit var standing: PokemonPose
    lateinit var walk: PokemonPose
    lateinit var summary: PokemonPose

    val tail = getPart("tail")
    val tail2 = getPart("tail2")
    val tail3 = getPart("tail3")
    val tail4 = getPart("tail4")
    val tail5 = getPart("tail5")
    val tailWaveSegment = WaveSegment(modelPart = tail, length = 11F)
    val tail2WaveSegment = WaveSegment(modelPart = tail2, length = 11F)
    val tail3WaveSegment = WaveSegment(modelPart = tail3, length = 11F)
    val tail4WaveSegment = WaveSegment(modelPart = tail4, length = 11F)
    val tail5WaveSegment = WaveSegment(modelPart = tail5, length = 11F)

    override fun registerPoses() {
        // TODO tongue_flick

        val wave = WaveAnimation<PokemonEntity>(
            frame = this,
            waveFunction = sineFunction(
                period = 10F,
                amplitude = 0.5F
            ),
            basedOnLimbSwing = true,
            oscillationsScalar = 8F,
            head = tail,
            rotationAxis = TransformedModelPart.Y_AXIS,
            motionAxis = TransformedModelPart.X_AXIS,
            headLength = 0.1F,
            segments = arrayOf(
                tailWaveSegment,
                tail2WaveSegment,
                tail3WaveSegment,
                tail4WaveSegment,
                tail5WaveSegment
            )
        )


        standing = registerPose(
            poseName = "standing",
            poseTypes = STATIONARY_POSES,
            transformTicks = 10,
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("arbok", "ground_idle"),
                wave
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = MOVING_POSES,
            transformTicks = 10,
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("arbok", "ground_walk"),
                wave
            )
        )

        summary = registerPose(
            poseName = "summary",
            poseTypes = UI_POSES,
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("arbok", "summary_idle")
            )
        )
    }

    override fun getFaintAnimation(
        pokemonEntity: PokemonEntity,
        state: PoseableEntityState<PokemonEntity>
    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("arbok", "faint") else null
}