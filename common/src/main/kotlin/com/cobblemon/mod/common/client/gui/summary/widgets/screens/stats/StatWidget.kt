/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.summary.widgets.screens.stats

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.pokemon.stats.Stat
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.gui.summary.widgets.SoundlessWidget
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.lang
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.render.BufferRenderer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.math.MathHelper.ceil
import net.minecraft.util.math.MathHelper.floor
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3f

class StatWidget(
    pX: Int, pY: Int,
    val pokemon: Pokemon,
    val tabIndex: Int = STATS
): SoundlessWidget(pX, pY, WIDTH, HEIGHT, Text.literal("StatWidget")) {

    companion object {
        // Stat Index
        private const val STATS = 0
        private const val IV = 1
        private const val EV = 2
        private const val BASE = 3
        private const val OTHER = 4

        private const val WIDTH = 134
        private const val HEIGHT = 148
        private const val SCALE = 0.5F

        private const val WHITE = 0x00FFFFFF
        private const val GREY = 0x00AAAAAA
        private const val BLUE = 0x00548BFB
        private const val RED = 0x00FB5454

        private val statsBaseResource = cobblemonResource("ui/summary/summary_stats_chart_base.png")
        private val statsChartResource = cobblemonResource("ui/summary/summary_stats_chart.png")
        private val statsOtherBaseResource = cobblemonResource("ui/summary/summary_stats_other_base.png")
        private val friendshipOverlayResource = cobblemonResource("ui/summary/summary_stats_friendship_overlay.png")
        private val tabMarkerResource = cobblemonResource("ui/summary/summary_stats_tab_marker.png")
        private val statIncreaseResource = cobblemonResource("ui/summary/summary_stats_icon_increase.png")
        private val statDecreaseResource = cobblemonResource("ui/summary/summary_stats_icon_decrease.png")

        private val statsLabel = lang("ui.stats")
        private val baseLabel = lang("ui.stats.base")
        private val ivLabel = lang("ui.stats.ivs")
        private val evLabel = lang("ui.stats.evs")
        private val otherLabel = lang("ui.stats.other")

        private val hpLabel = lang("ui.stats.hp")
        private val spAtkLabel = lang("ui.stats.sp_atk")
        private val atkLabel = lang("ui.stats.atk")
        private val spDefLabel = lang("ui.stats.sp_def")
        private val defLabel = lang("ui.stats.def")
        private val speedLabel = lang("ui.stats.speed")
    }

    var statTabIndex = tabIndex

    private fun drawTriangle(
        colour: Vec3f,
        v1: Vec2f,
        v2: Vec2f,
        v3: Vec2f
    ) {
        CobblemonResources.WHITE.let { RenderSystem.setShaderTexture(0, it) }
        RenderSystem.setShaderColor(colour.x, colour.y, colour.z, 0.6F)
        val bufferBuilder = Tessellator.getInstance().buffer
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION)
        bufferBuilder.vertex(v1.x.toDouble(), v1.y.toDouble(), 10.0).next()
        bufferBuilder.vertex(v2.x.toDouble(), v2.y.toDouble(), 10.0).next()
        bufferBuilder.vertex(v3.x.toDouble(), v3.y.toDouble(), 10.0).next()
        BufferRenderer.drawWithShader(bufferBuilder.end())
    }

    private fun drawStatHexagon(stats: Map<Stat, Int>, colour: Vec3f, maximum: Int) {
        val hexLeftX = x + 25.5
        val hexTopY = y + 22
        val hexAttackY = hexTopY + 24.5
        val hexDefenceY = hexAttackY + 47.0
        val hexBottomY = hexDefenceY + 24.5
        val hexRightX = x + 108.5
        val hexCenterX = (hexLeftX + hexRightX) / 2
        val hexCenterY = (hexTopY + hexBottomY) / 2

        val triangleWidth = (hexCenterX - hexLeftX).toFloat()
        val triangleHeight = ((hexBottomY - hexTopY) / 2).toFloat()

        val hpRatio = (stats.getOrDefault(Stats.HP, 0).toFloat() / maximum).coerceIn(0.075F, 1F)
        val atkRatio = (stats.getOrDefault(Stats.ATTACK, 0).toFloat() / maximum).coerceIn(0.075F, 1F)
        val defRatio = (stats.getOrDefault(Stats.DEFENCE, 0).toFloat() / maximum).coerceIn(0.075F, 1F)
        val spAtkRatio = (stats.getOrDefault(Stats.SPECIAL_ATTACK, 0).toFloat() / maximum).coerceIn(0.075F, 1F)
        val spDefRatio = (stats.getOrDefault(Stats.SPECIAL_DEFENCE, 0).toFloat() / maximum).coerceIn(0.075F, 1F)
        val spdRatio = (stats.getOrDefault(Stats.SPEED, 0).toFloat() / maximum).coerceIn(0.075F, 1F)

        val hpPoint = Vec2f(
            hexCenterX.toFloat(),
            hexCenterY.toFloat() - hpRatio * triangleHeight
        )

        val attackPoint = Vec2f(
            hexCenterX.toFloat() + atkRatio * triangleWidth,
            hexCenterY.toFloat() - atkRatio * triangleHeight / 2
        )

        val defencePoint = Vec2f(
            hexCenterX.toFloat() + defRatio * triangleWidth,
            hexCenterY.toFloat() + defRatio * triangleHeight / 2
        )

        val specialAttackPoint = Vec2f(
            hexCenterX.toFloat() - spAtkRatio * triangleWidth,
            hexCenterY.toFloat() - spAtkRatio * triangleHeight / 2
        )

        val specialDefencePoint = Vec2f(
            hexCenterX.toFloat() - spDefRatio * triangleWidth,
            hexCenterY.toFloat() + spDefRatio * triangleHeight / 2
        )

        val speedPoint = Vec2f(
            hexCenterX.toFloat(),
            hexCenterY.toFloat() + spdRatio * triangleHeight
        )

        val centerPoint = Vec2f(
            hexCenterX.toFloat(),
            hexCenterY.toFloat()
        )

        // 1-o'clock
        drawTriangle(colour, hpPoint, centerPoint, attackPoint)
        // 3-o'clock
        drawTriangle(colour, attackPoint, centerPoint, defencePoint)
        // 5-o'clock
        drawTriangle(colour, defencePoint, centerPoint, speedPoint)
        // 7-o'clock
        drawTriangle(colour, speedPoint, centerPoint, specialDefencePoint)
        // 9-o'clock
        drawTriangle(colour, specialDefencePoint, centerPoint, specialAttackPoint)
        // 11-o'clock
        drawTriangle(colour, hpPoint, specialAttackPoint, centerPoint)


//        drawTriangle(colour, specialAttackPoint, centerPoint, hpPoint)
    }


    override fun render(pMatrixStack: MatrixStack, pMouseX: Int, pMouseY: Int, pPartialTicks: Float) {
        val renderChart = statTabIndex != OTHER

        // Background
        blitk(
            matrixStack = pMatrixStack,
            texture = if (renderChart) statsBaseResource else statsOtherBaseResource,
            x= x,
            y = y,
            width = width,
            height = height
        )

        // Chart
        if (renderChart) {
            blitk(
                matrixStack = pMatrixStack,
                texture = statsChartResource,
                x= (x + 25.5) / SCALE,
                y = (y + 22) / SCALE,
                width = 166,
                height = 192,
                scale = SCALE
            )
        }

        when (statTabIndex) {
            STATS -> drawStatHexagon(
                mapOf(
                    Stats.HP to pokemon.hp,
                    Stats.ATTACK to pokemon.attack,
                    Stats.DEFENCE to pokemon.defence,
                    Stats.SPECIAL_ATTACK to pokemon.specialAttack,
                    Stats.SPECIAL_DEFENCE to pokemon.specialDefence,
                    Stats.SPEED to pokemon.speed
                ),
                colour = Vec3f(50F/255, 215F/255F, 1F),
                maximum = 400
            )
            BASE -> drawStatHexagon(
                pokemon.form.baseStats,
                colour = Vec3f(1F, 107F/255, 50F/255),
                maximum = 200
            )
            IV -> drawStatHexagon(
                pokemon.ivs.associate { it.key to it.value },
                colour = Vec3f(216F/255, 100F/255, 1F),
                maximum = 31
            )
            EV -> drawStatHexagon(
                pokemon.evs.associate { it.key to it.value },
                colour = Vec3f(1F, 1F, 100F/255),
                maximum = 252
            )
        }

        drawScaledText(
            matrixStack = pMatrixStack,
            text = statsLabel.bold(),
            x = x + 29,
            y = y + 143,
            scale = SCALE,
            colour = if (statTabIndex == STATS) WHITE else GREY,
            centered = true
        )

        drawScaledText(
            matrixStack = pMatrixStack,
            text = ivLabel.bold(),
            x = x + 48,
            y = y + 143,
            scale = SCALE,
            colour = if (statTabIndex == IV) WHITE else GREY,
            centered = true
        )

        drawScaledText(
            matrixStack = pMatrixStack,
            text = evLabel.bold(),
            x = x + 67,
            y = y + 143,
            scale = SCALE,
            colour = if (statTabIndex == EV) WHITE else GREY,
            centered = true
        )

        drawScaledText(
            matrixStack = pMatrixStack,
            text = baseLabel.bold(),
            x = x + 86,
            y = y + 143,
            scale = SCALE,
            colour = if (statTabIndex == BASE) WHITE else GREY,
            centered = true
        )

        drawScaledText(
            matrixStack = pMatrixStack,
            text = otherLabel.bold(),
            x = x + 105,
            y = y + 143,
            scale = SCALE,
            colour = if (statTabIndex == OTHER) WHITE else GREY,
            centered = true
        )

        blitk(
            matrixStack = pMatrixStack,
            texture = tabMarkerResource,
            x= (x + 27 + (statTabIndex * 19)) / SCALE,
            y = (y + 140) / SCALE,
            width = 8,
            height = 4,
            scale = SCALE,
        )

        if (renderChart) {
            // Stat Labels
            renderTextAtVertices(
                pMatrixStack = pMatrixStack,
                hp = hpLabel.bold(),
                spAtk = spAtkLabel.bold(),
                atk = atkLabel.bold(),
                spDef = spDefLabel.bold(),
                def = defLabel.bold(),
                speed = speedLabel.bold()
            )

            // Stat Values
            renderTextAtVertices(
                pMatrixStack = pMatrixStack,
                offsetY = 5.5,
                enableColour = false,
                hp = getStatValueAsText(Stats.HP),
                spAtk = getStatValueAsText(Stats.SPECIAL_ATTACK),
                atk = getStatValueAsText(Stats.ATTACK),
                spDef = getStatValueAsText(Stats.SPECIAL_DEFENCE),
                def = getStatValueAsText(Stats.DEFENCE),
                speed = getStatValueAsText(Stats.SPEED)
            )

            // Nature-modified Stat Icons
            if (statTabIndex == STATS) {
                renderModifiedStatIcon(pMatrixStack, pokemon.nature.increasedStat, true)
                renderModifiedStatIcon(pMatrixStack, pokemon.nature.decreasedStat, false)
            }
        } else {
            // Friendship
            val friendshipBarWidthMax = 108
            val friendshipRatio = pokemon.friendship / 255F
            val friendshipBarWidth = ceil(friendshipRatio * friendshipBarWidthMax)
            blitk(
                matrixStack = pMatrixStack,
                texture = CobblemonResources.WHITE,
                x = x + 13,
                y = y + 27,
                height = 8,
                width = friendshipBarWidth,
                red = 0.92,
                green = if (pokemon.friendship >= 160) 0.28 else 0.7,
                blue = if (pokemon.friendship >= 160) 0.4 else 0.28
            )

            blitk(
                matrixStack = pMatrixStack,
                texture = friendshipOverlayResource,
                x = (x + 5) / SCALE,
                y = (y + 26) / SCALE,
                height = 20,
                width = 248,
                scale = SCALE
            )

            // Label
            drawScaledText(
                matrixStack = pMatrixStack,
                font = CobblemonResources.DEFAULT_LARGE,
                text = lang("ui.stats.friendship").bold(),
                x = x + 67,
                y = y + 12.5,
                centered = true,
                shadow = true
            )

            drawScaledText(
                matrixStack = pMatrixStack,
                text = pokemon.friendship.toString().text(),
                x = x + 16,
                y = y + 16,
                scale = SCALE,
                centered = true
            )

            drawScaledText(
                matrixStack = pMatrixStack,
                text = "${floor(friendshipRatio * 100)}%".text(),
                x = x + 118,
                y = y + 16,
                scale = SCALE,
                centered = true
            )
        }
    }

    override fun mouseClicked(pMouseX: Double, pMouseY: Double, pButton: Int): Boolean {
        val index = getTabIndexFromPos(pMouseX, pMouseY)
        if (index in 0..4) statTabIndex = index
        return super.mouseClicked(pMouseX, pMouseY, pButton)
    }

    private fun getStatValueAsText(stat: Stat): MutableText {
        val value = when(statTabIndex) {
            STATS -> if (stat == Stats.HP) "${pokemon.currentHealth} / ${pokemon.hp}" else pokemon.getStat(stat).toString()
            BASE -> pokemon.form.baseStats[stat].toString()
            IV -> pokemon.ivs.getOrDefault(stat).toString()
            EV -> pokemon.evs.getOrDefault(stat).toString()
            else -> "0"
        }
        return value.text()
    }

    private fun renderModifiedStatIcon(pMatrixStack: MatrixStack, stat: Stat?, increasedStat: Boolean) {
        if (stat != null) {
            var posX = x.toDouble()
            var posY = y.toDouble()

            when(stat) {
                Stats.HP -> { posX += 65; posY += 6 }
                Stats.SPECIAL_ATTACK -> { posX += 10; posY += 38 }
                Stats.ATTACK -> { posX += 120; posY += 38 }
                Stats.SPECIAL_DEFENCE -> { posX += 10; posY += 89 }
                Stats.DEFENCE -> { posX += 120; posY += 89 }
                Stats.SPEED -> { posX += 65; posY += 120 }
            }

            blitk(
                matrixStack = pMatrixStack,
                texture = if (increasedStat) statIncreaseResource else statDecreaseResource,
                x= posX / SCALE,
                y = posY / SCALE,
                width = 8,
                height = 6,
                scale = SCALE,
            )
        }
    }

    private fun getModifiedStatColour(stat: Stat, enableColour: Boolean): Int {
        if (statTabIndex == STATS && enableColour) {
            if (pokemon.nature.increasedStat == stat) return RED
            if (pokemon.nature.decreasedStat == stat) return BLUE
        }
        return WHITE
    }

    private fun renderTextAtVertices(
        pMatrixStack: MatrixStack,
        offsetY: Double = 0.0,
        enableColour: Boolean = true,
        hp: MutableText,
        spAtk: MutableText,
        atk: MutableText,
        spDef: MutableText,
        def: MutableText,
        speed: MutableText
    ) {
        drawScaledText(
            matrixStack = pMatrixStack,
            text = hp,
            x = x + 67,
            y = y + 10.5 + offsetY,
            scale = SCALE,
            colour = getModifiedStatColour(Stats.HP, enableColour),
            centered = true
        )

        drawScaledText(
            matrixStack = pMatrixStack,
            text = spAtk,
            x = x + 12,
            y = y + 42.5 + offsetY,
            scale = SCALE,
            colour = getModifiedStatColour(Stats.SPECIAL_ATTACK, enableColour),
            centered = true
        )

        drawScaledText(
            matrixStack = pMatrixStack,
            text = atk,
            x = x + 122,
            y = y + 42.5 + offsetY,
            scale = SCALE,
            colour = getModifiedStatColour(Stats.ATTACK,enableColour),
            centered = true
        )

        drawScaledText(
            matrixStack = pMatrixStack,
            text = spDef,
            x = x + 12,
            y = y + 93.5 + offsetY,
            scale = SCALE,
            colour = getModifiedStatColour(Stats.SPECIAL_DEFENCE, enableColour),
            centered = true
        )

        drawScaledText(
            matrixStack = pMatrixStack,
            text = def,
            x = x + 122,
            y = y + 93.5 + offsetY,
            scale = SCALE,
            colour = getModifiedStatColour(Stats.DEFENCE, enableColour),
            centered = true
        )

        drawScaledText(
            matrixStack = pMatrixStack,
            text = speed,
            x = x + 67,
            y = y + 124.5 + offsetY,
            scale = SCALE,
            colour = getModifiedStatColour(Stats.SPEED, enableColour),
            centered = true
        )
    }

    private fun getTabIndexFromPos(mouseX: Double, mouseY: Double): Int {
        val left = x + 19.5
        val top = y + 140.0
        if (mouseX in left..(left + 95.0) && mouseY in top..(top + 9.0)) {
            var startX = left
            var endX = left + 19
            for (index in 0..4) {
                if (mouseX in startX..endX) return index
                startX += 19
                endX += 19
            }
        }
        return -1
    }
}