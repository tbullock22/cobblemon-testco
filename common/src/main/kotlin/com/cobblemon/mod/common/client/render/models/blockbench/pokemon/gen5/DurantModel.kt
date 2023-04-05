/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5

import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class DurantModel (root: ModelPart) : PokemonPoseableModel(), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("durant")
    override val head = getPart("head")

    override val portraitScale = 2.8F
    override val portraitTranslation = Vec3d(-0.4, -1.6, 0.0)

    override val profileScale = 1.0F
    override val profileTranslation = Vec3d(0.0, 0.2, 0.0)

    lateinit var standing: PokemonPose
    lateinit var walk: PokemonPose

    override fun registerPoses() {
        val blink = quirk("blink") { bedrockStateful("durant", "blink").setPreventsIdle(false) }
        standing = registerPose(
            poseName = "standing",
            quirks = arrayOf(blink),
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            idleAnimations = arrayOf(
                singleBoneLook()
                //bedrock("durant", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES,
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("durant", "ground_walk")
            )
        )
    }
}