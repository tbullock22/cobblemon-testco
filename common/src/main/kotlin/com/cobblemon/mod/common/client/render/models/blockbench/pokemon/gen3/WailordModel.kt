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

class WailordModel(root: ModelPart) : PokemonPoseableModel() {
    override val rootPart = root.registerChildWithAllChildren("wailord")
    override val portraitScale = 0.45F
    override val portraitTranslation = Vec3d(-0.38, 0.8, 0.0)

    override val profileScale = 0.25F
    override val profileTranslation = Vec3d(0.0, 1.2, 0.0)

    lateinit var standing: PokemonPose
    override fun registerPoses() {
        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.ALL_POSES,
            idleAnimations = arrayOf(
//                bedrock("wailord", "ground_idle")
            )
        )
    }
}