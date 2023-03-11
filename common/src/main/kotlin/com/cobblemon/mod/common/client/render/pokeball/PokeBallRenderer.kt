/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.pokeball

import com.cobblemon.mod.common.client.entity.EmptyPokeBallClientDelegate
import com.cobblemon.mod.common.client.render.models.blockbench.repository.PokeBallModelRepository
import com.cobblemon.mod.common.client.render.models.blockbench.repository.PokemonModelRepository
import com.cobblemon.mod.common.entity.pokeball.EmptyPokeBallEntity
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRenderer
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.item.ItemRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3f.POSITIVE_Y

class PokeBallRenderer(context: EntityRendererFactory.Context) : EntityRenderer<EmptyPokeBallEntity>(context) {

    override fun getTexture(pEntity: EmptyPokeBallEntity): Identifier {
        return PokeBallModelRepository.getTexture(pEntity.pokeBall.name, pEntity.aspects.get(), pEntity.delegate as EmptyPokeBallClientDelegate)
    }

    override fun render(entity: EmptyPokeBallEntity, yaw: Float, partialTicks: Float, poseStack: MatrixStack, buffer: VertexConsumerProvider, packedLight: Int) {
        val model = PokeBallModelRepository.getPoser(entity.pokeBall.name, entity.aspects.get())
        poseStack.push()
        poseStack.translate(0.0, 1.5 * 0.7F, 0.0)
        poseStack.multiply(POSITIVE_Y.getDegreesQuaternion(yaw))
        poseStack.scale(0.7F, -0.7F, -0.7F)
        val vertexConsumer = ItemRenderer.getDirectItemGlintConsumer(buffer, model.getLayer(getTexture(entity)), false, false)

        val state = entity.delegate as EmptyPokeBallClientDelegate
        model.setLayerContext(buffer, state, PokemonModelRepository.getLayers(entity.pokeBall.name, entity.aspects.get()))
        model.setAngles(entity, 0f, 0f, entity.age + partialTicks, 0F, 0F)
        model.render(poseStack, vertexConsumer, packedLight, OverlayTexture.DEFAULT_UV, 1.0f, 1.0f, 1.0f, 1.0f)

        model.green = 1F
        model.blue = 1F
        model.red = 1F

        model.resetLayerContext()

        poseStack.pop()
        super.render(entity, yaw, partialTicks, poseStack, buffer, packedLight)
    }
}