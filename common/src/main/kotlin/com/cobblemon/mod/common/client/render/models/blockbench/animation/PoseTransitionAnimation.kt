/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.animation

import com.cobblemon.mod.common.client.render.models.blockbench.PoseableEntityModel
import com.cobblemon.mod.common.client.render.models.blockbench.PoseableEntityState
import com.cobblemon.mod.common.client.render.models.blockbench.frame.ModelFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.client.render.models.blockbench.withPosition
import java.lang.Float.min
import net.minecraft.client.model.ModelPart
import net.minecraft.entity.Entity

/**
 * An animation that gradually moves any [ModelFrame] from one pose to another.
 *
 * @author Hiroku
 * @since December 5th, 2021
 */
class PoseTransitionAnimation<T : Entity>(
    val beforePose: Pose<T, *>,
    val afterPose: Pose<T, *>,
    durationTicks: Int = 20
) : StatefulAnimation<T, ModelFrame> {
    override val isTransform = true

    var changedPose = false
    val transforms = mutableListOf<GradualTransform>()
    val startTime = System.currentTimeMillis()
    val endTime = startTime + durationTicks * 50L

    inner class GradualTransform(
        val modelPart: ModelPart,
        val initialPosition: FloatArray,
        val initialRotation: FloatArray,
        val destinationPosition: FloatArray,
        val destinationRotation: FloatArray
    ) {
        fun apply(ratio: Float) {
            modelPart.setPivot(
                ratio * (destinationPosition[0] - initialPosition[0]) + initialPosition[0],
                ratio * (destinationPosition[1] - initialPosition[1]) + initialPosition[1],
                ratio * (destinationPosition[2] - initialPosition[2]) + initialPosition[2]
            )
            modelPart.setAngles(
                ratio * (destinationRotation[0] - initialRotation[0]) + initialRotation[0],
                ratio * (destinationRotation[1] - initialRotation[1]) + initialRotation[1],
                ratio * (destinationRotation[2] - initialRotation[2]) + initialRotation[2]
            )
        }
    }

    init {
        val beforeTransforms = beforePose.transformedParts
        val afterTransforms = afterPose.transformedParts

        val checkedParts = mutableListOf<ModelPart>()

        beforeTransforms.forEach { before ->
            val destination = afterTransforms.find { it.modelPart == before.modelPart }
                ?: before.modelPart
                    .withPosition(before.initialPosition[0], before.initialPosition[1], before.initialPosition[2])
                    .withRotation(before.initialRotation[0], before.initialRotation[1], before.initialRotation[2])

            transforms.add(
                GradualTransform(
                    modelPart = before.modelPart,
                    initialPosition = before.position,
                    initialRotation = before.rotation,
                    destinationPosition = destination.position,
                    destinationRotation = destination.rotation
                )
            )

            checkedParts.add(before.modelPart)
        }

        afterTransforms.filter { it.modelPart !in checkedParts }.forEach { after ->
            transforms.add(
                GradualTransform(
                    modelPart = after.modelPart,
                    initialPosition = after.initialPosition,
                    initialRotation = after.initialRotation,
                    destinationPosition = after.position,
                    destinationRotation = after.rotation
                )
            )
        }
    }

    override fun preventsIdle(entity: T?, state: PoseableEntityState<T>, idleAnimation: StatelessAnimation<T, *>) = false
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
        val now = System.currentTimeMillis()
        val durationMillis = (endTime - startTime).toFloat()
        val passedMillis = (now - startTime).toFloat()
        val ratio = min(passedMillis / durationMillis, 1F)

        transforms.forEach { it.apply(min(ratio * 2F, 1F)) }

        val amountOfBefore = 1 - ratio
        val amountOfAfter = ratio

        if (ratio < 1F) {
            model.relevantParts.forEach { it.changeFactor = amountOfAfter }
            afterPose.idleAnimations.forEach {
                it.apply(
                    entity,
                    model,
                    state,
                    limbSwing,
                    limbSwingAmount,
                    ageInTicks,
                    headYaw,
                    headPitch
                )
            }
        }

        // Stateless animations happen next
        model.relevantParts.forEach { it.changeFactor = if (ratio < 1F) amountOfBefore else 1F }

        if (ratio >= 1F) {
            state.setPose(afterPose.poseName)
            model.applyPose(afterPose.poseName)
        }

        return ratio < 1F
    }
}