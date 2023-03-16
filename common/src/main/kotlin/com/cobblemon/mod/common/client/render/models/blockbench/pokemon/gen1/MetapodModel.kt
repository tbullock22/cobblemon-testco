/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.PoseableEntityState
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.ALL_POSES
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d
class MetapodModel(root: ModelPart) : PokemonPoseableModel() {
    override val rootPart = root.registerChildWithAllChildren("metapod")

    override val portraitScale = 2.2F
    override val portraitTranslation = Vec3d(-0.15, -1.35, 0.0)
    override val profileScale = 1.1F
    override val profileTranslation = Vec3d(0.0, 0.1, 0.0)

    lateinit var sleep: PokemonPose
    lateinit var standing: PokemonPose

    override fun registerPoses() {
        val blink = quirk("blink") { bedrockStateful("metapod", "blink").setPreventsIdle(false)}
        sleep = registerPose(
            poseType = PoseType.SLEEP,
            idleAnimations = arrayOf(bedrock("metapod", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            quirks = arrayOf(blink),
            poseTypes = ALL_POSES - PoseType.SLEEP,
            idleAnimations = arrayOf(bedrock("metapod", "ground_idle"))
        )
    }

    override fun getFaintAnimation(
        pokemonEntity: PokemonEntity,
        state: PoseableEntityState<PokemonEntity>
    ) = if (state.isPosedIn(standing)) bedrockStateful("metapod", "faint") else null
}