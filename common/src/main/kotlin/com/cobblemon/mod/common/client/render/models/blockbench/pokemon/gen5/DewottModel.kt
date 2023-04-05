/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5

import com.cobblemon.mod.common.client.render.models.blockbench.asTransformed
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BimanualFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BipedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class DewottModel (root: ModelPart) : PokemonPoseableModel(), HeadedFrame, BipedFrame, BimanualFrame {
    override val rootPart = root.registerChildWithAllChildren("dewott")
    override val head = getPart("head")
    override val rightArm = getPart("arm_right")
    override val leftArm = getPart("arm_left")
    override val rightLeg = getPart("leg_right")
    override val leftLeg = getPart("leg_left")
    val scalchop_body_right = getPart("scalchop_skirt_right")
    val scalchop_body_left = getPart("scalchop_skirt_left")
    val scalchop_right = getPart("scalchop_hand_right")
    val scalchop_left = getPart("scalchop_hand_left")

    override val portraitScale = 2.0F
    override val portraitTranslation = Vec3d(-0.15, 0.8, 0.0)
    override val profileScale = 0.7F
    override val profileTranslation = Vec3d(0.0, 0.69, 0.0)

    lateinit var battleidle: PokemonPose
    lateinit var sleep: PokemonPose
    lateinit var standing: PokemonPose
    lateinit var walk: PokemonPose

    override fun registerPoses() {
        val blink = quirk("blink") { bedrockStateful("dewott", "blink").setPreventsIdle(false) }
        sleep = registerPose(
            poseType = PoseType.SLEEP,
            transformTicks = 10,
            condition = { !it.isBattling },
            transformedParts = arrayOf(
                scalchop_right.asTransformed().withVisibility(visibility = false),
                scalchop_left.asTransformed().withVisibility(visibility = false),
                scalchop_body_right.asTransformed().withVisibility(visibility = true),
                scalchop_body_left.asTransformed().withVisibility(visibility = true)

            ),
            idleAnimations = arrayOf(bedrock("dewott", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = setOf(PoseType.NONE, PoseType.STAND, PoseType.PORTRAIT, PoseType.PROFILE),
            transformTicks = 10,
            quirks = arrayOf(blink),
            condition = { !it.isBattling },
            transformedParts = arrayOf(
                scalchop_right.asTransformed().withVisibility(visibility = false),
                scalchop_left.asTransformed().withVisibility(visibility = false),
                scalchop_body_right.asTransformed().withVisibility(visibility = true),
                scalchop_body_left.asTransformed().withVisibility(visibility = true)
            ),
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("dewott", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walking",
            poseTypes = setOf(PoseType.SWIM, PoseType.WALK),
            transformTicks = 10,
            quirks = arrayOf(blink),
            condition = { !it.isBattling },
            transformedParts = arrayOf(
                scalchop_right.asTransformed().withVisibility(visibility = false),
                scalchop_left.asTransformed().withVisibility(visibility = false),
                scalchop_body_right.asTransformed().withVisibility(visibility = true),
                scalchop_body_left.asTransformed().withVisibility(visibility = true)
            ),
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("dewott", "ground_walk")
            )
        )

        battleidle = registerPose(
            poseName = "battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            condition = { it.isBattling },
            transformedParts = arrayOf(
                scalchop_right.asTransformed().withVisibility(visibility = true),
                scalchop_left.asTransformed().withVisibility(visibility = true),
                scalchop_body_right.asTransformed().withVisibility(visibility = false),
                scalchop_body_left.asTransformed().withVisibility(visibility = false)
            ),
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("dewott", "ground_idle")
            )

        )
    }
}