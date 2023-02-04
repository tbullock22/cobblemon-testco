/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.STANDING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class PoliwagModel(root: ModelPart) : PokemonPoseableModel() {
    override val rootPart = root.registerChildWithAllChildren("poliwag")

    override val portraitScale = 1.8F
    override val portraitTranslation = Vec3d(0.0, -0.7, 0.0)

    override val profileScale = 0.9F
    override val profileTranslation = Vec3d(0.0, 0.38, 0.0)

    lateinit var standing: PokemonPose
    lateinit var float: PokemonPose
    lateinit var swim: PokemonPose

    override fun registerPoses() {
        standing = registerPose(
            poseName = "standing",
            poseTypes = STANDING_POSES + UI_POSES,
            idleAnimations = arrayOf(
                bedrock("poliwag", "ground_idle")
            )
        )

        float = registerPose(
            poseName = "float",
            poseType = PoseType.FLOAT,
            idleAnimations = arrayOf(
                bedrock("poliwag", "water_idle")
            )
        )

        swim = registerPose(
            poseName = "swim",
            poseType = PoseType.SWIM,
            idleAnimations = arrayOf(
                bedrock("poliwag", "water_swim")
            )
        )
    }

//    override fun getFaintAnimation(
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("poliwag", "faint") else null
//        pokemonEntity: PokemonEntity,
//        state: PoseableEntityState<PokemonEntity>
}