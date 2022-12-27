/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2

import com.cobblemon.mod.common.client.render.models.blockbench.animation.BipedWalkAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BipedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class MurkrowModel(root: ModelPart) : PokemonPoseableModel(), HeadedFrame, BipedFrame {
    override val rootPart = root.registerChildWithAllChildren("murkrow")
    override val head = getPart("head")
    override val leftLeg = getPart("leg_left")
    override val rightLeg = getPart("leg_right")

    override val portraitScale = 1.6F
    override val portraitTranslation = Vec3d(0.0, -0.15, 0.0)

    override val profileScale = 1.0F
    override val profileTranslation = Vec3d(0.0, 0.17, 0.0)

    lateinit var standing: PokemonPose
    lateinit var walk: PokemonPose
    lateinit var sleep: PokemonPose
    override fun registerPoses() {
        val blink = quirk("blink") { bedrockStateful("murkrow", "blink").setPreventsIdle(false) }
        sleep = registerPose(
            poseType = PoseType.SLEEP,
            idleAnimations = arrayOf(
                bedrock("murkrow", "sleep")
            )
        )
        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            quirks = arrayOf(blink), idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("murkrow", "ground_idle")
            )
        )
        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES,
            quirks = arrayOf(blink), idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("murkrow", "ground_idle"),
                BipedWalkAnimation(this, periodMultiplier = 0.8F, amplitudeMultiplier = 0.8F)
//                bedrock("murkrow", "ground_walk")
            )
        )
    }
}