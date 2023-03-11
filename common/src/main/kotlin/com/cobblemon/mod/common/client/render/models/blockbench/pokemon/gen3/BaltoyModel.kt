/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3

import com.cobblemon.mod.common.client.render.models.blockbench.frame.BimanualFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class BaltoyModel(root: ModelPart) : PokemonPoseableModel(), BimanualFrame {
    override val rootPart = root.registerChildWithAllChildren("baltoy")
    override val rightArm = getPart("arm_right")
    override val leftArm = getPart("arm_left")

    override val portraitScale = 3.1F
    override val portraitTranslation = Vec3d(0.0, -1.6, 0.0)

    override val profileScale = 1.0F
    override val profileTranslation = Vec3d(0.0, 0.3, 0.0)

    lateinit var walk: PokemonPose
    lateinit var standing: PokemonPose

    override fun registerPoses() {
        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            transformTicks = 10,
            idleAnimations = arrayOf(
                bedrock("baltoy", "ground_idle")
            )
        )
        walk = registerPose(
            poseName = "walking",
            poseTypes = PoseType.MOVING_POSES,
            transformTicks = 10,
            idleAnimations = arrayOf(
                bedrock("baltoy", "ground_walk")
            )
        )
    }
}