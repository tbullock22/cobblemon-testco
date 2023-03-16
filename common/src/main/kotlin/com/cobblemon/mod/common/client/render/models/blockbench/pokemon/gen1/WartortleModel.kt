/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.EarJoint
import com.cobblemon.mod.common.client.render.models.blockbench.PoseableEntityState
import com.cobblemon.mod.common.client.render.models.blockbench.RangeOfMotion
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BimanualFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BipedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.EaredFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.TransformedModelPart
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.math.geometry.toRadians
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d
class WartortleModel(root: ModelPart) : PokemonPoseableModel(), HeadedFrame, BipedFrame, BimanualFrame, EaredFrame {
    override val rootPart = root.registerChildWithAllChildren("wartortle")
    override val head = getPart("head_ai")
    override val rightArm = getPart("arm_right")
    override val leftArm = getPart("arm_left")
    override val rightLeg = getPart("leg_right")
    override val leftLeg = getPart("leg_left")
    private val rightEar = getPart("ear_right")
    private val leftEar = getPart("ear_left")
    override val leftEarJoint = EarJoint(leftEar, TransformedModelPart.Z_AXIS, RangeOfMotion(50F.toRadians(), 0F))
    override val rightEarJoint = EarJoint(rightEar, TransformedModelPart.Z_AXIS, RangeOfMotion((-50F).toRadians(), 0F))

    override val portraitScale = 2.2F
    override val portraitTranslation = Vec3d(-0.3, 0.44, 0.0)

    override val profileScale = 0.7F
    override val profileTranslation = Vec3d(-0.06, 0.7, 0.0)

    lateinit var sleep: PokemonPose
    lateinit var standing: PokemonPose
    lateinit var walk: PokemonPose
    lateinit var swimIdle: PokemonPose
    lateinit var swim: PokemonPose

    override fun registerPoses() {
        val blink = quirk("blink") { bedrockStateful("wartortle", "blink").setPreventsIdle(false)}
        sleep = registerPose(
            poseType = PoseType.SLEEP,
            transformTicks = 10,
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(bedrock("wartortle", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = UI_POSES + PoseType.STAND,
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("wartortle", "ground_idle")
            )
        )

        swimIdle = registerPose(
            poseName = "swim_idle",
            poseTypes = setOf(PoseType.FLOAT),
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("wartortle", "water_idle")
            )
        )

        swim = registerPose(
            poseName = "swim",
            poseTypes = setOf(PoseType.SWIM),
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("wartortle", "water_swim")
            )
        )

        walk = registerPose(
            poseType = PoseType.WALK,
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("wartortle", "ground_walk")
            )
        )
    }

    override fun getFaintAnimation(
        pokemonEntity: PokemonEntity,
        state: PoseableEntityState<PokemonEntity>
    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("wartortle", "faint") else null
}