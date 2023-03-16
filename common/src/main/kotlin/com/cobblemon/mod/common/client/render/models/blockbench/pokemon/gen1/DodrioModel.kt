/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.animation.BipedWalkAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BipedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.entity.PoseType.Companion.MOVING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d
class DodrioModel(root: ModelPart) : PokemonPoseableModel(), BipedFrame {
    override val rootPart = root.registerChildWithAllChildren("dodrio")

    override val leftLeg = getPart("leg_left")
    override val rightLeg = getPart("leg_right")

    override val portraitScale = 1.5F
    override val portraitTranslation = Vec3d(-0.15, 0.9, 0.0)

    override val profileScale = 0.8F
    override val profileTranslation = Vec3d(0.0, 0.6, 0.0)

    lateinit var standing: PokemonPose
    lateinit var walk: PokemonPose

    override fun registerPoses() {
        val blink1 = quirk("blink1") { bedrockStateful("dodrio", "blink1").setPreventsIdle(false)}
        val blink2 = quirk("blink2") { bedrockStateful("dodrio", "blink2").setPreventsIdle(false)}
        val blink3 = quirk("blink3") { bedrockStateful("dodrio", "blink3").setPreventsIdle(false)}
        standing = registerPose(
            poseName = "standing",
            poseTypes = STATIONARY_POSES + UI_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink1, blink2, blink3),
            idleAnimations = arrayOf(
//                singleBoneLook()
                bedrock("dodrio", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = MOVING_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink1, blink2, blink3),
            idleAnimations = arrayOf(
                BipedWalkAnimation(this, periodMultiplier = 0.6F, amplitudeMultiplier = 0.9F),
                bedrock("dodrio", "ground_idle")
//                singleBoneLook()
                //bedrock("dodrio", "ground_walk")
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PoseableEntityState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("dodrio", "faint") else null
}