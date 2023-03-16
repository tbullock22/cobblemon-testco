/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4

import com.cobblemon.mod.common.client.render.models.blockbench.frame.BimanualFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BipedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class GibleModel(root: ModelPart) : PokemonPoseableModel(), BipedFrame, BimanualFrame {
    override val rootPart = root.registerChildWithAllChildren("gible")

    override val rightArm = getPart("arm_right")
    override val leftArm = getPart("arm_left")
    override val rightLeg = getPart("leg_right")
    override val leftLeg = getPart("leg_left")

    override val portraitScale = 1.65F
    override val portraitTranslation = Vec3d(0.1, -0.5, 0.0)

    override val profileScale = 0.71F
    override val profileTranslation = Vec3d(0.0, 0.72, 0.0)

    lateinit var standing: PokemonPose
    lateinit var walk: PokemonPose

    override fun registerPoses() {
        val blink = quirk("blink") { bedrockStateful("gible", "blink").setPreventsIdle(false) }
        standing = registerPose(
                poseName = "standing",
                poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
                transformTicks = 10,
                quirks = arrayOf(blink),
                idleAnimations = arrayOf(
                        bedrock("gible", "ground_idle")
                )
        )
        walk = registerPose(
                poseName = "walk",
                poseTypes = PoseType.MOVING_POSES,
                transformTicks = 5,
                quirks = arrayOf(blink),
                idleAnimations = arrayOf(
                        bedrock("gible", "ground_idle"),
                        bedrock("gible", "ground_walk")
                )
        )
    }
}