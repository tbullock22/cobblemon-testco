/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3

import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class ClaydolModel(root: ModelPart) : PokemonPoseableModel() {
    override val rootPart = root.registerChildWithAllChildren("claydol")

    override val portraitScale = 1.6F
    override val portraitTranslation = Vec3d(-0.7, 1.0, 0.0)

    override val profileScale = 0.65F
    override val profileTranslation = Vec3d(0.0, 0.8, 0.0)

    lateinit var sleep: PokemonPose
    lateinit var standing: PokemonPose
    lateinit var walk: PokemonPose

    override fun registerPoses() {
        sleep = registerPose(
            poseType = PoseType.SLEEP,
            idleAnimations = arrayOf(bedrock("claydol", "sleep"))
        )

        val blink = quirk("blink") { bedrockStateful("claydol", "blink").setPreventsIdle(false) }
        standing = registerPose(
            poseName = "hover",
            poseTypes = PoseType.STATIONARY_POSES - PoseType.HOVER + PoseType.UI_POSES,
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                bedrock("claydol", "air_idle")
            )
        )

        walk = registerPose(
            poseName = "fly",
            poseTypes = PoseType.MOVING_POSES + PoseType.FLY,
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                bedrock("claydol", "air_fly")
            )
        )
    }
}