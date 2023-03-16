/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.gui

import com.cobblemon.mod.common.api.text.font
import com.cobblemon.mod.common.client.gui.battle.BattleOverlay.Companion.PORTRAIT_DIAMETER
import com.cobblemon.mod.common.client.render.models.blockbench.PoseableEntityState
import com.cobblemon.mod.common.client.render.models.blockbench.repository.PokemonModelRepository
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Species
import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.BufferRenderer
import net.minecraft.client.render.DiffuseLighting
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.LightmapTextureManager
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.MutableText
import net.minecraft.text.OrderedText
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.Matrix4f
import net.minecraft.util.math.Vec3f

fun blitk(
    matrixStack: MatrixStack,
    texture: Identifier? = null,
    x: Number,
    y: Number,
    height: Number = 0,
    width: Number = 0,
    uOffset: Number = 0,
    vOffset: Number = 0,
    textureWidth: Number = width,
    textureHeight: Number = height,
    blitOffset: Number = 0,
    red: Number = 1,
    green: Number = 1,
    blue: Number = 1,
    alpha: Number = 1F,
    blend: Boolean = true,
    scale: Float = 1F
) {
    RenderSystem.setShader { GameRenderer.getPositionTexShader() }
    texture?.run { RenderSystem.setShaderTexture(0, this) }
    if (blend) {
        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA)
    }
    RenderSystem.setShaderColor(red.toFloat(), green.toFloat(), blue.toFloat(), alpha.toFloat())
    matrixStack.push()
    matrixStack.scale(scale, scale, 1F)
    drawRectangle(
        matrixStack.peek().positionMatrix,
        x.toFloat(), y.toFloat(), x.toFloat() + width.toFloat(), y.toFloat() + height.toFloat(),
        blitOffset.toFloat(),
        uOffset.toFloat() / textureWidth.toFloat(), (uOffset.toFloat() + width.toFloat()) / textureWidth.toFloat(),
        vOffset.toFloat() / textureHeight.toFloat(), (vOffset.toFloat() + height.toFloat()) / textureHeight.toFloat()
    )
    matrixStack.pop()
}

fun drawRectangle(
    matrix: Matrix4f,
    x: Float,
    y: Float,
    endX: Float,
    endY: Float,
    blitOffset: Float,
    minU: Float,
    maxU: Float,
    minV: Float,
    maxV: Float
) {
    val bufferbuilder = Tessellator.getInstance().buffer
    bufferbuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE)
    bufferbuilder.vertex(matrix, x, endY, blitOffset).texture(minU, maxV).next()
    bufferbuilder.vertex(matrix, endX, endY, blitOffset).texture(maxU, maxV).next()
    bufferbuilder.vertex(matrix, endX, y, blitOffset).texture(maxU, minV).next()
    bufferbuilder.vertex(matrix, x, y, blitOffset).texture(minU, minV).next()
    // TODO: Figure out if this is correct replacement.
    // OLD: BufferRenderer.draw(bufferbuilder)
    BufferRenderer.drawWithShader(bufferbuilder.end())
}

fun drawCenteredText(
    poseStack: MatrixStack,
    font: Identifier? = null,
    text: Text,
    x: Number,
    y: Number,
    colour: Int,
    shadow: Boolean = true
) {
    val comp = (text as MutableText).let { if (font != null) it.font(font) else it }
    val mcFont = MinecraftClient.getInstance().textRenderer
    if (shadow)
        mcFont.drawWithShadow(poseStack, comp, x.toFloat() - mcFont.getWidth(comp) / 2, y.toFloat(), colour)
    else
        mcFont.draw(poseStack, comp, x.toFloat() - mcFont.getWidth(comp) / 2, y.toFloat(), colour)
}

fun drawText(
    poseStack: MatrixStack,
    font: Identifier? = null,
    text: MutableText,
    x: Number,
    y: Number,
    centered: Boolean = false,
    colour: Int,
    shadow: Boolean = true
) {
    val comp = if (font == null) text else text.setStyle(text.style.withFont(font))
    val mcFont = MinecraftClient.getInstance().textRenderer
    var x = x
    if (centered) {
        val width = mcFont.getWidth(comp)
        x = x.toDouble() - width / 2
    }

    if (shadow) {
        mcFont.drawWithShadow(poseStack, comp, x.toFloat(), y.toFloat(), colour)
    } else {
        mcFont.draw(poseStack, comp, x.toFloat(), y.toFloat(), colour)
    }
}

fun drawText(
    poseStack: MatrixStack,
    text: OrderedText,
    x: Number,
    y: Number,
    centered: Boolean = false,
    colour: Int,
    shadow: Boolean = true
) {
    val mcFont = MinecraftClient.getInstance().textRenderer
    var x = x
    if (centered) {
        val width = mcFont.getWidth(text)
        x = x.toDouble() - width / 2
    }

    if (shadow)
        mcFont.drawWithShadow(poseStack, text, x.toFloat(), y.toFloat(), colour)
    else
        mcFont.draw(poseStack, text, x.toFloat(), y.toFloat(), colour)
}

fun drawString(
    poseStack: MatrixStack,
    text: String,
    x: Number,
    y: Number,
    colour: Int,
    shadow: Boolean = true,
    font: Identifier? = null
) {
    val comp = Text.literal(text).also {
        font?.run {
            it.getWithStyle(it.style.withFont(this))
        }
    }
    val mcFont = MinecraftClient.getInstance().textRenderer
    if (shadow)
        mcFont.drawWithShadow(poseStack, comp, x.toFloat(), y.toFloat(), colour)
    else
        mcFont.draw(poseStack, comp, x.toFloat(), y.toFloat(), colour)
}

fun drawPortraitPokemon(
    species: Species,
    aspects: Set<String>,
    matrixStack: MatrixStack,
    scale: Float = 13F,
    reversed: Boolean = false,
    state: PoseableEntityState<PokemonEntity>? = null
) {
    val model = PokemonModelRepository.getPoser(species.resourceIdentifier, aspects)
    val texture = PokemonModelRepository.getTexture(species.resourceIdentifier, aspects, state)

    val renderType = model.getLayer(texture)

    RenderSystem.applyModelViewMatrix()
    val quaternion1 = Vec3f.POSITIVE_Y.getDegreesQuaternion(-32F * if (reversed) -1F else 1F)
    val quaternion2 = Vec3f.POSITIVE_X.getDegreesQuaternion(5F)

    if (state == null) {
        model.setupAnimStateless(setOf(PoseType.PORTRAIT, PoseType.PROFILE))
    } else {
        model.getPose(PoseType.PORTRAIT)?.let { state.setPose(it.poseName) }
        state.timeEnteredPose = 0F
        model.setupAnimStateful(null, state, 0F, 0F, 0F, 0F, 0F)
    }

    matrixStack.push()
    matrixStack.translate(0.0, PORTRAIT_DIAMETER.toDouble() + 2.0, 0.0)
    matrixStack.scale(scale, scale, -scale)
    matrixStack.translate(0.0, -PORTRAIT_DIAMETER / 18.0, 0.0)
    matrixStack.translate(model.portraitTranslation.x * if (reversed) -1F else 1F, model.portraitTranslation.y, model.portraitTranslation.z - 4)
    matrixStack.scale(model.portraitScale, model.portraitScale, 0.1F)
    matrixStack.multiply(quaternion1)
    matrixStack.multiply(quaternion2)

    val light1 = Vec3f(0.2F, 1.0F, -1.0F)
    val light2 = Vec3f(0.1F, 0.0F, 8.0F)
    RenderSystem.setShaderLights(light1, light2)
    quaternion1.conjugate()

    val immediate = MinecraftClient.getInstance().bufferBuilders.entityVertexConsumers
    val buffer = immediate.getBuffer(renderType)
    val packedLight = LightmapTextureManager.pack(11, 7)

    model.withLayerContext(immediate, state, PokemonModelRepository.getLayers(species.resourceIdentifier, aspects)) {
        model.render(matrixStack, buffer, packedLight, OverlayTexture.DEFAULT_UV, 1F, 1F, 1F, 1F)
        immediate.draw()
    }

    matrixStack.pop()

    DiffuseLighting.enableGuiDepthLighting()
}