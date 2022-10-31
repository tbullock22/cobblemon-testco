/*
 * Copyright (C) 2022 Pokemod Cobbled Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cablemc.pokemod.common.client.render.models.blockbench.pokemon.gen4

import com.cablemc.pokemod.common.client.render.models.blockbench.animation.QuadrupedWalkAnimation
import com.cablemc.pokemod.common.client.render.models.blockbench.frame.QuadrupedFrame
import com.cablemc.pokemod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cablemc.pokemod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cablemc.pokemod.common.entity.PoseType.Companion.MOVING_POSES
import com.cablemc.pokemod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cablemc.pokemod.common.entity.PoseType.Companion.UI_POSES
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class MamoswineModel(root: ModelPart) : PokemonPoseableModel(), QuadrupedFrame {
    override val rootPart = root.registerChildWithAllChildren("mamoswine")

    override val hindLeftLeg = getPart("leftbackleg")
    override val hindRightLeg = getPart("rightbackleg")
    override val foreLeftLeg = getPart("leftfrontleg")
    override val foreRightLeg = getPart("rightfrontleg")

    override val portraitScale = 1.0F
    override val portraitTranslation = Vec3d(0.0, 0.0, 0.0)

    override val profileScale = 1.0F
    override val profileTranslation = Vec3d(0.0, 0.0, 0.0)

    lateinit var standing: PokemonPose
    lateinit var walk: PokemonPose

    override fun registerPoses() {
        standing = registerPose(
            poseName = "standing",
            poseTypes = STATIONARY_POSES + UI_POSES,
            idleAnimations = arrayOf(
                //bedrock("0473_mamoswine/mamoswine", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = MOVING_POSES,
            idleAnimations = arrayOf(
                QuadrupedWalkAnimation(this, periodMultiplier = 0.6F, amplitudeMultiplier = 0.9F)
                //bedrock("0473_mamoswine/mamoswine", "ground_walk")
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PoseableEntityState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("0473_mamoswine/mamoswine", "faint") else null
}