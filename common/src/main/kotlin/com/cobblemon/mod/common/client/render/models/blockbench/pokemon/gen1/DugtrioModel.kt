/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.PoseableEntityState
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.entity.PoseType.Companion.MOVING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d
class DugtrioModel(root: ModelPart) : PokemonPoseableModel() {
    override val rootPart: ModelPart = root.registerChildWithAllChildren("dugtrio")

    override val portraitScale = 1.3F
    override val portraitTranslation = Vec3d(0.0, -0.4, 0.0)

    override val profileScale = 0.9F
    override val profileTranslation = Vec3d(0.0, 0.15, 0.0)

    override fun registerPoses() {
        registerPose(
            poseName = "stand",
            poseTypes = STATIONARY_POSES + UI_POSES,
            idleAnimations = arrayOf(bedrock("dugtrio", "ground_idle"))
        )

        registerPose(
            poseName = "walk",
            poseTypes = MOVING_POSES,
            idleAnimations = arrayOf(bedrock("dugtrio", "ground_walk"))
        )
    }

    override fun getFaintAnimation(
        pokemonEntity: PokemonEntity,
        state: PoseableEntityState<PokemonEntity>
    ) = bedrockStateful("dugtrio", "faint")
}