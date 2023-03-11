/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench

import com.bedrockk.molang.runtime.MoLangRuntime
import com.cobblemon.mod.common.client.render.MatrixWrapper
import com.cobblemon.mod.common.client.render.models.blockbench.additives.PosedAdditiveAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.animation.StatefulAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.bedrock.animation.BedrockParticleKeyframe
import com.cobblemon.mod.common.client.render.models.blockbench.bedrock.animation.BedrockStatelessAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.ModelFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.client.render.models.blockbench.quirk.ModelQuirk
import com.cobblemon.mod.common.client.render.models.blockbench.quirk.QuirkData
import java.util.concurrent.ConcurrentLinkedQueue
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity

/**
 * Represents the entity-specific state for a poseable model. The implementation is responsible for
 * handling all the state for an entity's model, and needs to be conscious of the fact that the
 * model may change without this state changing.
 *
 * @author Hiroku
 * @since December 5th, 2021
 */
abstract class PoseableEntityState<T : Entity> {
    var currentModel: PoseableEntityModel<T>? = null
    var currentPose: String? = null
    val statefulAnimations: MutableList<StatefulAnimation<T, *>> = mutableListOf()
    val quirks = mutableMapOf<ModelQuirk<T, *>, QuirkData<T>>()
    val additives: MutableList<PosedAdditiveAnimation<T>> = mutableListOf()
    var timeEnteredPose = 0F
    var previousAnimationSeconds = 0F
    var animationSeconds = 0F
    var deltaSeconds = 0F
    var timeLastRendered = System.currentTimeMillis()
    var wasPaused = false
    val poseParticles = mutableListOf<BedrockParticleKeyframe>()
    val runtime = MoLangRuntime().also {
        it.environment.structs["query"] = it.environment.structs["variable"]
    }

    val locatorStates = mutableMapOf<String, MatrixWrapper>()

    val renderQueue = ConcurrentLinkedQueue<() -> Unit>()

    fun isPosedIn(vararg poses: Pose<T, in ModelFrame>) = poses.any { it.poseName == currentPose }
    fun isNotPosedIn(vararg poses: Pose<T, in ModelFrame>) = poses.none { it.poseName == currentPose }

    fun preRender() {
        val now = System.currentTimeMillis()
        var deltaMillis = now - timeLastRendered

        if (wasPaused) {
            deltaMillis = 0L
        }

        if (MinecraftClient.getInstance().isPaused) {
            if (!wasPaused) {
                wasPaused = true
            }
        } else if (wasPaused) {
            wasPaused = false
        }

        timeLastRendered = now
        deltaSeconds = deltaMillis / 1000F
        previousAnimationSeconds = animationSeconds
        animationSeconds += deltaSeconds

        while (renderQueue.peek() != null) {
            val action = renderQueue.poll()
            action()
        }
    }

    fun doLater(action: () -> Unit) {
        renderQueue.offer(action)
    }

    fun getPose(): String? {
        return currentPose
    }

    fun setPose(pose: String) {
        currentPose = pose
        val model = currentModel
        if (model != null) {
            val poseImpl = model.getPose(pose) ?: return
            poseParticles.removeIf { it !in poseImpl.idleAnimations.filterIsInstance<BedrockStatelessAnimation<*>>().flatMap { it.particleKeyFrames } }
            poseImpl.onTransitionedInto(this)
            timeEnteredPose = animationSeconds
        }
    }

    fun applyAdditives(entity: T?, model: PoseableEntityModel<T>, state: PoseableEntityState<T>?) {
        additives.removeIf { !it.run(entity, model, state) }
    }

    fun setStatefulAnimations(vararg animations: StatefulAnimation<T, out ModelFrame>) {
        statefulAnimations.clear()
        statefulAnimations.addAll(animations)
    }
}