/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.item

import com.cobblemon.mod.common.client.render.models.blockbench.repository.PokemonModelRepository
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.item.PokemonItem
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.LightmapTextureManager
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemStack
import net.minecraft.util.math.Quaternion
import net.minecraft.util.math.Vec3f

class PokemonItemRenderer : CobblemonBuiltinItemRenderer {
    override fun render(stack: ItemStack, mode: ModelTransformation.Mode, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int, overlay: Int) {
        val pokemonItem = stack.item as? PokemonItem ?: return
        val (species, aspects) = pokemonItem.getSpeciesAndAspects(stack) ?: return

        matrices.push()
        val model = PokemonModelRepository.getPoser(species.resourceIdentifier, aspects)
        val renderLayer = model.getLayer(PokemonModelRepository.getTexture(species.resourceIdentifier, aspects, null))

        val transformations = positions[mode]!!

        RenderSystem.applyModelViewMatrix()
        matrices.scale(transformations.scale.x, transformations.scale.y, transformations.scale.z)
        matrices.translate(transformations.translation.x, transformations.translation.y, transformations.translation.z)
        model.setupAnimStateless(PoseType.PROFILE)
        matrices.translate(model.profileTranslation.x, model.profileTranslation.y,  model.profileTranslation.z - 4.0)
        matrices.scale(model.profileScale, model.profileScale, 0.1F)

        val rotation = Quaternion.fromEulerXyzDegrees(Vec3f(transformations.rotation.x, transformations.rotation.y, transformations.rotation.z))
        matrices.multiply(rotation)
        rotation.conjugate()
        MinecraftClient.getInstance().entityRenderDispatcher.rotation = rotation

        val light1 = Vec3f(-1F, 1F, 1.0F)
        val light2 = Vec3f(1.3F, -1F, 1.0F)
        RenderSystem.setShaderLights(light1, light2)
//        val packedLight = LightmapTextureManager.pack(12, 12)
        val vertexConsumer: VertexConsumer = vertexConsumers.getBuffer(renderLayer)
        matrices.push()

        val packedLight = LightmapTextureManager.pack(11, 7)
        model.withLayerContext(vertexConsumers, null, PokemonModelRepository.getLayers(species.resourceIdentifier, aspects)) {
            model.render(matrices, vertexConsumer, packedLight, OverlayTexture.DEFAULT_UV, 1F, 1F, 1F, 1F)
        }

        matrices.pop()
        matrices.pop()

        MinecraftClient.getInstance().bufferBuilders.entityVertexConsumers.draw()
    }

    companion object {
        val positions: MutableMap<ModelTransformation.Mode, Transformations> = mutableMapOf()

        init {
            positions[ModelTransformation.Mode.GUI] = PokemonItemRenderer().Transformations(
                PokemonItemRenderer().Transformation(1.0, -1.9, -0.5),
                PokemonItemRenderer().Transformation(0.5F, -0.5F, -0.5F),
                PokemonItemRenderer().Transformation(0F, 35F, 0F)
            )
            positions[ModelTransformation.Mode.FIXED] = PokemonItemRenderer().Transformations(
                PokemonItemRenderer().Transformation(1.0, -2.0, 3.0),
                PokemonItemRenderer().Transformation(0.5F, -0.5F, -0.5F),
                PokemonItemRenderer().Transformation(0F, 35F - 180F, 0F)
            )
            positions[ModelTransformation.Mode.FIRST_PERSON_RIGHT_HAND] = PokemonItemRenderer().Transformations(
                PokemonItemRenderer().Transformation(2.75, -1.2, 5.0),
                PokemonItemRenderer().Transformation(0.5F, -0.5F, -0.5F),
                PokemonItemRenderer().Transformation(0F, 35F, 0F)
            )
            positions[ModelTransformation.Mode.FIRST_PERSON_LEFT_HAND] = PokemonItemRenderer().Transformations(
                PokemonItemRenderer().Transformation(-0.75, -1.2, 5.0),
                PokemonItemRenderer().Transformation(0.5F, -0.5F, -0.5F),
                PokemonItemRenderer().Transformation(0F, -35F, 0F)
            )
            positions[ModelTransformation.Mode.THIRD_PERSON_RIGHT_HAND] = PokemonItemRenderer().Transformations(
                PokemonItemRenderer().Transformation(1.0, -2.6, 2.75),
                PokemonItemRenderer().Transformation(0.5F, -0.5F, -0.5F),
                PokemonItemRenderer().Transformation(0F, 35F, 0F)
            )
            positions[ModelTransformation.Mode.THIRD_PERSON_LEFT_HAND] = PokemonItemRenderer().Transformations(
                PokemonItemRenderer().Transformation(1.0, -2.6, 2.75),
                PokemonItemRenderer().Transformation(0.5F, -0.5F, -0.5F),
                PokemonItemRenderer().Transformation(0F, -35F, 0F)
            )
            positions[ModelTransformation.Mode.GROUND] = PokemonItemRenderer().Transformations(
                PokemonItemRenderer().Transformation(1.0, -2.6, 3.0),
                PokemonItemRenderer().Transformation(0.5F, -0.5F, -0.5F),
                PokemonItemRenderer().Transformation(0F, 35F, 0F)
            )
            positions[ModelTransformation.Mode.HEAD] = PokemonItemRenderer().Transformations(
                PokemonItemRenderer().Transformation(1.0, -3.5, 3.0),
                PokemonItemRenderer().Transformation(0.5F, -0.5F, -0.5F),
                PokemonItemRenderer().Transformation(0F, 215F, 0F)
            )
            positions[ModelTransformation.Mode.NONE] = PokemonItemRenderer().Transformations(
                PokemonItemRenderer().Transformation(0.0, 0.0, 0.0),
                PokemonItemRenderer().Transformation(0.5F, -0.5F, -0.5F),
                PokemonItemRenderer().Transformation(0F, 0F, 0F)
            )
        }
    }

    inner class Transformations(val translation: Transformation<Double>, val scale: Transformation<Float>, val rotation: Transformation<Float>)
    inner class Transformation<T>(val x: T, val y: T, val z: T)
}