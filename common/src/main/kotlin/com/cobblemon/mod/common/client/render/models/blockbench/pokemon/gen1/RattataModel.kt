/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.EarJoint
import com.cobblemon.mod.common.client.render.models.blockbench.PoseableEntityState
import com.cobblemon.mod.common.client.render.models.blockbench.RangeOfMotion
import com.cobblemon.mod.common.client.render.models.blockbench.frame.EaredFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.QuadrupedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.TransformedModelPart.Companion.Z_AXIS
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.math.geometry.toRadians
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d
class RattataModel(root: ModelPart) : PokemonPoseableModel(), HeadedFrame, QuadrupedFrame, EaredFrame {
    override val rootPart = root.registerChildWithAllChildren("rattata")
    override val foreLeftLeg = getPart("leg_front_left")
    override val foreRightLeg = getPart("leg_front_right")
    override val hindLeftLeg = getPart("leg_back_left")
    override val hindRightLeg = getPart("leg_back_right")
    override val head = getPart("head")
    override val leftEarJoint: EarJoint = EarJoint(getPart("ear_left"), Z_AXIS, RangeOfMotion(0F.toRadians(), -20F.toRadians()))
    override val rightEarJoint: EarJoint = EarJoint(getPart("ear_right"), Z_AXIS, RangeOfMotion(0F.toRadians(), 20F.toRadians()))

    override val portraitScale = 2.5F
    override val portraitTranslation = Vec3d(-0.25, -2.03, 0.0)

    override val profileScale = 1.0F
    override val profileTranslation = Vec3d(0.0, 0.0, 0.0)

    lateinit var standing: PokemonPose
    lateinit var walk: PokemonPose
    lateinit var sleep: PokemonPose

    override fun registerPoses() {
        sleep = registerPose(
            poseType = PoseType.SLEEP,
            transformTicks = 10,
            idleAnimations = arrayOf(bedrock("rattata", "sleep"))
        )
        standing = registerPose(
            poseName = "standing",
            poseTypes = setOf(PoseType.NONE, PoseType.PROFILE, PoseType.STAND, PoseType.FLOAT, PoseType.PORTRAIT, PoseType.SHOULDER_LEFT, PoseType.SHOULDER_RIGHT),
            transformTicks = 10,
            condition = { !it.isMoving.get() },
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("rattata", "ground_idle")
            )
        )

        walk = registerPose(
            poseType = PoseType.WALK,
            transformTicks = 10,
            condition = { it.isMoving.get() },
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("rattata", "ground_walk")
            )
        )
    }

    override fun getFaintAnimation(
        pokemonEntity: PokemonEntity,
        state: PoseableEntityState<PokemonEntity>
    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("rattata", "faint") else null
}
