/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.animation.StatelessAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.asTransformed
import com.cobblemon.mod.common.client.render.models.blockbench.frame.ModelFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.TransformedModelPart.Companion.Y_AXIS
import com.cobblemon.mod.common.client.render.models.blockbench.withRotation
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.FLYING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.STANDING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.SWIMMING_POSES
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.math.geometry.toRadians
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d
class MagikarpModel(root: ModelPart) : PokemonPoseableModel() {
    override val rootPart = root.registerChildWithAllChildren("magikarp")
    val leftMustache = getPart("whisker_left")
    val rightMustache = getPart("whisker_right")

    override val portraitScale = 2.0F
    override val portraitTranslation = Vec3d(-0.1, -0.75, 0.0)
    override val profileScale = 0.95F
    override val profileTranslation = Vec3d(0.0, 0.40, 0.0)

    override fun registerPoses() {
        val blink = quirk("blink") { bedrockStateful("magikarp", "blink").setPreventsIdle(false)}
        registerPose(
            poseName = "land",
            poseTypes = STANDING_POSES + PoseType.PROFILE,
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(bedrock("magikarp","flop")),
            transformedParts = arrayOf(
                rootPart.asTransformed().addPosition(Y_AXIS, 6),
                leftMustache.withRotation(Y_AXIS, (-75F).toRadians()),
                rightMustache.withRotation(Y_AXIS, 75F.toRadians())
            )
        )

        registerPose(
            poseName = "swimming",
            poseTypes = SWIMMING_POSES,
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                bedrock("magikarp", "water_swim")
            )
        )

        registerPose(
            poseName = "fly",
            poseTypes = FLYING_POSES,
            quirks = arrayOf(blink),
            idleAnimations = arrayOf(
                bedrock("magikarp", "air_idle")
            )
        )

        registerPose(
            poseName = "portrait",
            poseTypes = setOf(PoseType.PORTRAIT),
            quirks = arrayOf(blink),
            idleAnimations = emptyArray<StatelessAnimation<PokemonEntity, out ModelFrame>>(),
            transformedParts = arrayOf(
                leftMustache.withRotation(Y_AXIS, (-75F).toRadians()),
                rightMustache.withRotation(Y_AXIS, 75F.toRadians())
            )
        )
    }
}