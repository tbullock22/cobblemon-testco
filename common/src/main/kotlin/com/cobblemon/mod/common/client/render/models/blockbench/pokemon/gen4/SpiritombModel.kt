/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4

import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class SpiritombModel(root: ModelPart) : PokemonPoseableModel() {
    override val rootPart = root.registerChildWithAllChildren("spiritomb")

    override val portraitScale = 1.8F
    override val portraitTranslation = Vec3d(-0.23, -0.5, 0.0)

    override val profileScale = 1F
    override val profileTranslation = Vec3d(-0.1, 0.2, 0.0)

    lateinit var standing: PokemonPose
    lateinit var walk: PokemonPose

    override fun registerPoses() {

        standing = registerPose(
                poseName = "standing",
                poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
                transformTicks = 10,
                idleAnimations = arrayOf(
                        bedrock("spiritomb", "ground_idle")
                )
        )

        walk = registerPose(
                poseName = "walk",
                poseTypes = PoseType.MOVING_POSES,
                transformTicks = 10,
                idleAnimations = arrayOf(
                        bedrock("spiritomb", "ground_idle"),
                        bedrock("spiritomb", "ground_walk")
                )
        )
    }
}