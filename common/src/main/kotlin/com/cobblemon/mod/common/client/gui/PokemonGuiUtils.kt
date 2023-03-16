/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui

import com.cobblemon.mod.common.client.render.models.blockbench.PoseableEntityState
import com.cobblemon.mod.common.client.render.models.blockbench.repository.PokemonModelRepository
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.RenderablePokemon
import com.cobblemon.mod.common.pokemon.Species
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.DiffuseLighting
import net.minecraft.client.render.LightmapTextureManager
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Quaternion
import net.minecraft.util.math.Vec3f

fun drawProfilePokemon(
    renderablePokemon: RenderablePokemon,
    matrixStack: MatrixStack,
    rotation: Quaternion,
    state: PoseableEntityState<PokemonEntity>?,
    scale: Float = 20F
) = drawProfilePokemon(
    renderablePokemon.species,
    renderablePokemon.aspects,
    matrixStack,
    rotation,
    state,
    scale
)

fun drawProfilePokemon(
    species: Species,
    aspects: Set<String>,
    matrixStack: MatrixStack,
    rotation: Quaternion,
    state: PoseableEntityState<PokemonEntity>?,
    scale: Float = 20F
) {
    val model = PokemonModelRepository.getPoser(species.resourceIdentifier, aspects)
    val texture = PokemonModelRepository.getTexture(species.resourceIdentifier, aspects, state)

    val renderType = model.getLayer(texture)

    RenderSystem.applyModelViewMatrix()
    matrixStack.scale(scale, scale, -scale)

    if (state != null) {
        model.getPose(PoseType.PROFILE)?.let { state.setPose(it.poseName) }
        state.timeEnteredPose = 0F
        model.setupAnimStateful(null, state, 0F, 0F, 0F, 0F, 0F)
    } else {
        model.setupAnimStateless(PoseType.PROFILE)
    }
    matrixStack.translate(model.profileTranslation.x, model.profileTranslation.y,  model.profileTranslation.z - 4.0)
    matrixStack.scale(model.profileScale, model.profileScale, 0.1F)

    matrixStack.multiply(rotation)
    DiffuseLighting.method_34742()
    val entityRenderDispatcher = MinecraftClient.getInstance().entityRenderDispatcher
    rotation.conjugate()
    entityRenderDispatcher.rotation = rotation
    entityRenderDispatcher.setRenderShadows(true)

    val bufferSource = MinecraftClient.getInstance().bufferBuilders.entityVertexConsumers
    val buffer = bufferSource.getBuffer(renderType)
    val light1 = Vec3f(-1F, 1F, 1.0F)
    val light2 = Vec3f(1.3F, -1F, 1.0F)
    RenderSystem.setShaderLights(light1, light2)
    val packedLight = LightmapTextureManager.pack(11, 7)

    model.withLayerContext(bufferSource, state, PokemonModelRepository.getLayers(species.resourceIdentifier, aspects)) {
        model.render(matrixStack, buffer, packedLight, OverlayTexture.DEFAULT_UV, 1F, 1F, 1F, 1F)
        bufferSource.draw()
    }

    entityRenderDispatcher.setRenderShadows(true)
    DiffuseLighting.enableGuiDepthLighting()
}

