/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2

import com.cobblemon.mod.common.client.render.models.blockbench.asTransformed
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.TransformedModelPart.Companion.Y_AXIS
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class KingdraModel(root: ModelPart) : PokemonPoseableModel(), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("kingdra")
    override val head = getPart("head")

    override val portraitScale = 1.8F
    override val portraitTranslation = Vec3d(-0.4, 1.8, 0.0)

    override val profileScale = 0.63F
    override val profileTranslation = Vec3d(0.0, 1.0, 0.0)

    lateinit var standing: PokemonPose
    lateinit var walk: PokemonPose
    lateinit var float: PokemonPose
    lateinit var swim: PokemonPose

    override fun registerPoses() {
        val blink = quirk("blink") { bedrockStateful("kingdra", "blink").setPreventsIdle(false) }
        standing = registerPose(
            poseName = "standing",
            poseType = PoseType.STAND,
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("kingdra", "ground_idle")
            ),
            transformedParts = arrayOf(
                rootPart.asTransformed().addPosition(Y_AXIS, -4.0)
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseType = PoseType.WALK,
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("kingdra", "ground_walk")
            ),
            transformedParts = arrayOf(
                rootPart.asTransformed().addPosition(Y_AXIS, -4.0)
            )
        )

        float = registerPose(
            poseName = "float",
            poseTypes = UI_POSES + PoseType.FLOAT,
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("kingdra", "water_idle")
            ),
            transformedParts = arrayOf(
                rootPart.asTransformed().addPosition(Y_AXIS, -4.0)
            )
        )

        swim = registerPose(
            poseName = "swim",
            poseType = PoseType.SWIM,
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("kingdra", "water_swim")
            ),
            transformedParts = arrayOf(
                rootPart.asTransformed().addPosition(Y_AXIS, -4.0)
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PoseableEntityState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("kingdra", "faint") else null
}