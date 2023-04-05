/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4

import com.cobblemon.mod.common.client.render.models.blockbench.PoseableEntityState
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.MOVING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d
class PorygonzModel(root: ModelPart) : PokemonPoseableModel(), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("porygon_z")
    override val head = getPart("head")

    override val portraitScale = 2.4F
    override val portraitTranslation = Vec3d(-0.1, 1.1, 0.0)

    override val profileScale = 0.85F
    override val profileTranslation = Vec3d(0.0, 0.6, 0.0)

    lateinit var sleep: PokemonPose
    lateinit var standing: PokemonPose
    lateinit var walk: PokemonPose

    override fun registerPoses() {
        val blink = quirk("blink") { bedrockStateful("porygonz", "blink").setPreventsIdle(false) }
        val twitch1 = quirk("twitch1") { bedrockStateful("porygonz", "twitch1").setPreventsIdle(false) }
        val twitch2 = quirk("twitch2") { bedrockStateful("porygonz", "twitch2").setPreventsIdle(false) }
        val twitch3 = quirk("twitch3") { bedrockStateful("porygonz", "twitch3").setPreventsIdle(false) }
        val twitch4 = quirk("twitch4") { bedrockStateful("porygonz", "twitch4").setPreventsIdle(false) }
        val twitch5 = quirk("twitch5") { bedrockStateful("porygonz", "twitch5").setPreventsIdle(false) }
        val twitch6 = quirk("twitch6") { bedrockStateful("porygonz", "twitch6").setPreventsIdle(false) }
        sleep = registerPose(
            poseType = PoseType.SLEEP,
            quirks = arrayOf(twitch1, twitch2, twitch3, twitch4, twitch5, twitch6),
            idleAnimations = arrayOf(bedrock("porygonz", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = STATIONARY_POSES + UI_POSES,
            quirks = arrayOf(blink, twitch1, twitch2, twitch3, twitch4, twitch5, twitch6),
            transformTicks = 10,
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("porygonz", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = MOVING_POSES,
            quirks = arrayOf(blink, twitch1, twitch2, twitch3, twitch4, twitch5, twitch6),
            transformTicks = 10,
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("porygonz", "ground_walk")
            )
        )
    }

    override fun getFaintAnimation(
        pokemonEntity: PokemonEntity,
        state: PoseableEntityState<PokemonEntity>
    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("porygonz", "faint") else null
}