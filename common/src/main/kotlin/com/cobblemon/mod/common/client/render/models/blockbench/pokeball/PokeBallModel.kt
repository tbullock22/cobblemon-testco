/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokeball

import com.cobblemon.mod.common.client.entity.EmptyPokeBallClientDelegate
import com.cobblemon.mod.common.client.render.models.blockbench.PoseableEntityModel
import com.cobblemon.mod.common.client.render.models.blockbench.frame.ModelFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.PokeBallFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokeball.EmptyPokeBallEntity
import net.minecraft.client.model.ModelPart

class PokeBallModel(root: ModelPart) : PoseableEntityModel<EmptyPokeBallEntity>(), PokeBallFrame {
    override val rootPart = root.registerChildWithAllChildren("poke_ball")
    override val base = getPart("base")
    override val lid = getPart("lid")


    lateinit var shut: PokeBallPose
    lateinit var open: PokeBallPose
    lateinit var midair: PokeBallPose

    override fun getState(entity: EmptyPokeBallEntity) = entity.delegate as EmptyPokeBallClientDelegate

    override fun registerPoses() {
        midair = registerPose(
            poseName = "flying",
            poseTypes = setOf(PoseType.NONE),
            condition = { it.captureState.get() == EmptyPokeBallEntity.CaptureState.NOT.ordinal.toByte() },
            transformTicks = 0,
            idleAnimations = arrayOf(bedrock("poke_ball", "throw"))
        )

        shut = registerPose(
            poseName = "shut",
            poseTypes = setOf(PoseType.NONE),
            idleAnimations = arrayOf(
                bedrock("poke_ball", "shut_idle")
            ),
            transformTicks = 0
        )

        open = registerPose(
            poseName = "open",
            poseTypes = setOf(PoseType.NONE),
            idleAnimations = arrayOf(
                bedrock("poke_ball", "open_idle")
            ),
            transformTicks = 10
        )

        shut.transitions[open] = { _, _ -> bedrockStateful("poke_ball", "open").andThen { entity, state -> state.setPose(open.poseName) } }
        open.transitions[shut] = { _, _ -> bedrockStateful("poke_ball", "shut").andThen { entity, state -> state.setPose(shut.poseName) } }
        midair.transitions[open] = shut.transitions[open]!!
    }
}

typealias PokeBallPose = Pose<EmptyPokeBallEntity, ModelFrame>