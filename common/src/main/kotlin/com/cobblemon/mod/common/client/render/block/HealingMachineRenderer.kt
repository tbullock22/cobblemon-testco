/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.block

import com.cobblemon.mod.common.CobblemonBlocks
import com.cobblemon.mod.common.block.entity.HealingMachineBlockEntity
import net.minecraft.block.BlockState
import net.minecraft.block.HorizontalFacingBlock
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.render.model.json.ModelTransformationMode
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Direction
import net.minecraft.util.math.RotationAxis

class HealingMachineRenderer<T: BlockEntity>(ctx: BlockEntityRendererFactory.Context): BlockEntityRenderer<T> {
    companion object {
        private val offsets = listOf(
            0.2 to 0.385,
            -0.2 to 0.385,
            0.2 to 0.0,
            -0.2 to 0.0,
            0.2 to -0.385,
            -0.2 to -0.385
        )
    }

    override fun render(blockEntity: T, tickDelta: Float, poseStack: MatrixStack, multiBufferSource: VertexConsumerProvider, light: Int, overlay: Int) {
        if (blockEntity !is HealingMachineBlockEntity) return

        poseStack.push()

        val blockState = if (blockEntity.world != null) blockEntity.cachedState
            else (CobblemonBlocks.HEALING_MACHINE.defaultState.with(HorizontalFacingBlock.FACING, Direction.SOUTH) as BlockState)
        val yRot = blockState.get(HorizontalFacingBlock.FACING).asRotation()

        // Position Poké Balls
        poseStack.translate(0.5, 0.5, 0.5)

        poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-yRot))
        poseStack.scale(0.65F, 0.65F, 0.65F)

        for ((index, pokeBall) in blockEntity.pokeBalls.withIndex()) {
            poseStack.push()
            val offset = offsets[index]
            poseStack.translate(offset.first, 0.4, offset.second)
            MinecraftClient.getInstance().itemRenderer.renderItem(pokeBall.stack(), ModelTransformationMode.GROUND, light, overlay, poseStack, multiBufferSource, blockEntity.world, 0)
            poseStack.pop()
        }
        poseStack.pop()
    }
}