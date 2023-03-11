/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.bedrock.animation

import com.cobblemon.mod.common.client.render.models.blockbench.PoseableEntityModel
import com.cobblemon.mod.common.client.render.models.blockbench.PoseableEntityState
import com.cobblemon.mod.common.client.render.models.blockbench.animation.StatefulAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.animation.StatelessAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.ModelFrame
import net.minecraft.entity.Entity

/**
 * A stateful animation that runs a [BedrockAnimation]. It is completed when the underlying
 * bedrock animation is complete.
 *
 * @author Hiroku
 * @since July 16th, 2022
 */
open class BedrockStatefulAnimation<T : Entity>(
    val animation: BedrockAnimation,
    var preventsIdleCheck: (T?, PoseableEntityState<T>, StatelessAnimation<T, *>) -> Boolean
) : StatefulAnimation<T, ModelFrame> {
    fun setPreventsIdle(preventsIdle: Boolean): BedrockStatefulAnimation<T> {
        this.preventsIdleCheck = { _, _, _ -> preventsIdle }
        return this
    }

    var secondsPassed = 0F
    var isTransformAnimation = false
    private var afterAction: (T, PoseableEntityState<T>) -> Unit = { _, _ -> }

    override val isTransform: Boolean
        get() = isTransformAnimation

    fun isTransformAnimation(value: Boolean) = this.also {
        it.isTransformAnimation = value
    }

    fun andThen(action: (entity: T, PoseableEntityState<T>) -> Unit) = this.also {
        it.afterAction = action
    }

    override fun preventsIdle(entity: T?, state: PoseableEntityState<T>, idleAnimation: StatelessAnimation<T, *>) = preventsIdleCheck(entity, state, idleAnimation)
    override fun run(
        entity: T?,
        model: PoseableEntityModel<T>,
        state: PoseableEntityState<T>,
        limbSwing: Float,
        limbSwingAmount: Float,
        ageInTicks: Float,
        headYaw: Float,
        headPitch: Float
    ): Boolean {
        val previousSeconds = secondsPassed
        secondsPassed += state.deltaSeconds
        return animation.run(model, entity, state, previousSeconds.toDouble(), secondsPassed.toDouble()).also {
            if (!it && entity != null) {
                afterAction(entity, state)
            }
        }
    }
}