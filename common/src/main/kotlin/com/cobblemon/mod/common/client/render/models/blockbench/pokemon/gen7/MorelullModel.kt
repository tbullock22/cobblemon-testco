/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7

import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class MorelullModel(root: ModelPart) : PokemonPoseableModel() {
    override val rootPart = root.registerChildWithAllChildren("morelull")

    override val portraitScale = 5.0F
    override val portraitTranslation = Vec3d(0.0, -5.0, 0.0)

    override val profileScale = 1.0F
    override val profileTranslation = Vec3d(0.0, 0.25, 0.0)

    lateinit var standing: PokemonPose

    override fun registerPoses() {
        val blink = quirk("blink") { bedrockStateful("morelull", "blink").setPreventsIdle(false) }
        standing = registerPose(
                poseName = "standing",
                poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
                quirks = arrayOf(blink),
                transformTicks = 10,
                idleAnimations = arrayOf(
                        bedrock("morelull", "ground_idle")
                )
        )
    }
}