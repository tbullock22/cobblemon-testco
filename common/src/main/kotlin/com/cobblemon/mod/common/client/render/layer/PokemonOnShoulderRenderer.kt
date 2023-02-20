/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.layer

import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonFloatingState
import com.cobblemon.mod.common.client.render.models.blockbench.repository.PokemonModelRepository
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.DataKeys
import com.cobblemon.mod.common.util.isPokemonEntity
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.LivingEntityRenderer
import net.minecraft.client.render.entity.feature.FeatureRenderer
import net.minecraft.client.render.entity.feature.FeatureRendererContext
import net.minecraft.client.render.entity.model.PlayerEntityModel
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerEntity
class PokemonOnShoulderRenderer<T : PlayerEntity>(renderLayerParent: FeatureRendererContext<T, PlayerEntityModel<T>>) : FeatureRenderer<T, PlayerEntityModel<T>>(renderLayerParent) {
    override fun render(
        pMatrixStack: MatrixStack,
        pBuffer: VertexConsumerProvider,
        pPackedLight: Int,
        pLivingEntity: T,
        pLimbSwing: Float,
        pLimbSwingAmount: Float,
        pPartialTicks: Float,
        pAgeInTicks: Float,
        pNetHeadYaw: Float,
        pHeadPitch: Float
    ) {
        this.render(pMatrixStack, pBuffer, pPackedLight, pLivingEntity, pLimbSwing, pLimbSwingAmount, pNetHeadYaw, pHeadPitch, true)
        this.render(pMatrixStack, pBuffer, pPackedLight, pLivingEntity, pLimbSwing, pLimbSwingAmount, pNetHeadYaw, pHeadPitch, false)
    }

    private fun render(
        pMatrixStack: MatrixStack,
        pBuffer: VertexConsumerProvider,
        pPackedLight: Int,
        pLivingEntity: T,
        pLimbSwing: Float,
        pLimbSwingAmount: Float,
        pNetHeadYaw: Float,
        pHeadPitch: Float,
        pLeftShoulder: Boolean
    ) {
        val compoundTag = if (pLeftShoulder) pLivingEntity.shoulderEntityLeft else pLivingEntity.shoulderEntityRight
        if (compoundTag.isPokemonEntity()) {
            pMatrixStack.push()
            val pokemon = Pokemon().loadFromNBT(compoundTag.getCompound(DataKeys.POKEMON))
            val scale = pokemon.form.baseScale * pokemon.scaleModifier
            val width = pokemon.form.hitbox.width
            val offset = width / 2 - 0.7
            pMatrixStack.translate(
                if (pLeftShoulder) -offset else offset,
                (if (pLivingEntity.isSneaking) -1.3 else -1.5) * scale,
                0.0
            )
            pMatrixStack.scale(scale, scale, scale)
            val model = PokemonModelRepository.getPoser(pokemon.species, pokemon.aspects)
            val state = PokemonFloatingState()
            state.animationSeconds = pLivingEntity.age.toFloat() / 20F
            val vertexConsumer = pBuffer.getBuffer(model.getLayer(PokemonModelRepository.getTexture(pokemon.species, pokemon.aspects, state)))
            val i = LivingEntityRenderer.getOverlay(pLivingEntity, 0.0f)

            val pose = model.poses.values
                .firstOrNull { (if (pLeftShoulder) PoseType.SHOULDER_LEFT else PoseType.SHOULDER_RIGHT) in it.poseTypes  }
                ?: model.poses.values.first()
            state.setPose(pose.poseName)
            state.timeEnteredPose = 0F
            model.setupAnimStateful(
                entity = null,
                state = state,
                headYaw = pNetHeadYaw,
                headPitch = pHeadPitch,
                limbSwing = pLimbSwing,
                limbSwingAmount = pLimbSwingAmount,
                ageInTicks = pLivingEntity.age.toFloat()
            )
            model.render(pMatrixStack, vertexConsumer, pPackedLight, i, 1.0f, 1.0f, 1.0f, 1.0f)
            model.withLayerContext(pBuffer, state, PokemonModelRepository.getLayers(pokemon.species, pokemon.aspects)) {
                model.render(pMatrixStack, vertexConsumer, pPackedLight, OverlayTexture.DEFAULT_UV, 1F, 1F, 1F, 1F)
            }
            pMatrixStack.pop();
        }
    }
}