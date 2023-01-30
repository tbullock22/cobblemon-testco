/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.world.feature

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonBlocks
import com.cobblemon.mod.common.api.tags.CobblemonBiomeTags
import com.cobblemon.mod.common.util.randomNoCopy
import com.cobblemon.mod.common.block.ApricornBlock
import com.google.common.collect.Lists
import com.mojang.serialization.Codec
import kotlin.random.Random.Default.nextInt
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.HorizontalFacingBlock
import net.minecraft.block.LeavesBlock
import net.minecraft.tag.BlockTags
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Direction.*
import net.minecraft.util.math.random.Random
import net.minecraft.world.StructureWorldAccess
import net.minecraft.world.TestableWorld
import net.minecraft.world.chunk.ChunkStatus
import net.minecraft.world.gen.feature.Feature
import net.minecraft.world.gen.feature.SingleStateFeatureConfig
import net.minecraft.world.gen.feature.TreeFeature
import net.minecraft.world.gen.feature.util.FeatureContext

class ApricornTreeFeature(
    codec: Codec<SingleStateFeatureConfig>
) : Feature<SingleStateFeatureConfig>(codec) {

    override fun generate(context: FeatureContext<SingleStateFeatureConfig>) : Boolean {
        val worldGenLevel: StructureWorldAccess = context.world
        val random = context.random
        val origin = context.origin

        val isGenerating = worldGenLevel.getChunk(origin).status != ChunkStatus.FULL

        if (isGenerating) {
            val biome = worldGenLevel.getBiome(origin)
            val multiplier = if (biome.isIn(CobblemonBiomeTags.HAS_APRICORNS_DENSE)) {
                10F
            } else if (biome.isIn(CobblemonBiomeTags.HAS_APRICORNS_NORMAL)) {
                1.0F
            } else if (biome.isIn(CobblemonBiomeTags.HAS_APRICORNS_SPARSE)) {
                0.1F
            } else {
                return false
            }

            if (random.nextFloat() > multiplier * Cobblemon.config.baseApricornTreeGenerationChance) {
                return false
            }
        }

        if (!worldGenLevel.getBlockState(origin.down()).isIn(BlockTags.DIRT)) {
            return false
        }

        // Create trunk
        val logState = CobblemonBlocks.APRICORN_LOG.get().defaultState
        for (y in 0..4) {
            try {
                val logPos = origin.offset(UP, y)
                worldGenLevel.setBlockState(logPos, logState, 2)
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }

        // Decorate with leaves
        val allApricornSpots: MutableList<List<Pair<Direction, BlockPos>>> = mutableListOf()
        val leafBlock = CobblemonBlocks.APRICORN_LEAVES.get().defaultState

        val layerOnePos = origin.offset(UP)
        for (direction in listOf(NORTH, EAST, SOUTH, WEST)) {
            var leafPos = layerOnePos.offset(direction)
            setBlockIfClear(worldGenLevel, leafPos, LeavesBlock.updateDistanceFromLogs(leafBlock, worldGenLevel, leafPos))
            for (offset in 1..3) {
                leafPos = leafPos.up()
                setBlockIfClear(worldGenLevel, leafPos, LeavesBlock.updateDistanceFromLogs(leafBlock, worldGenLevel, leafPos))
            }
        }

        val layerOneExtenders = getLayerOneVariation(layerOnePos, random)
        setBlockIfClear(worldGenLevel, layerOneExtenders.first, LeavesBlock.updateDistanceFromLogs(leafBlock, worldGenLevel, layerOneExtenders.first))
        setBlockIfClear(worldGenLevel, layerOneExtenders.second, LeavesBlock.updateDistanceFromLogs(leafBlock, worldGenLevel, layerOneExtenders.second))

        for (coords in listOf(Pair(1, 1), Pair(-1, -1), Pair(1, -1), Pair(-1, 1))) {
            var leafPos = layerOnePos.add(coords.first, 0, coords.second)
            setBlockIfClear(worldGenLevel, leafPos, LeavesBlock.updateDistanceFromLogs(leafBlock, worldGenLevel, leafPos))
            for (offset in 1..3) {
                leafPos = leafPos.up()
                setBlockIfClear(worldGenLevel, leafPos, LeavesBlock.updateDistanceFromLogs(leafBlock, worldGenLevel, leafPos))
            }
        }

        val layerTwoPos = origin.add(0, 2, 0)
        for (direction in Lists.newArrayList(NORTH, EAST, SOUTH, WEST)) {
            val apricornSpots = mutableListOf<Pair<Direction, BlockPos>>()
            var leafPos = layerTwoPos.add(direction.offsetX * 2, direction.offsetY * 2, direction.offsetZ * 2)

            setBlockIfClear(worldGenLevel, leafPos, LeavesBlock.updateDistanceFromLogs(leafBlock, worldGenLevel, leafPos))
            apricornSpots.add(direction.opposite to leafPos.offset(direction))

            leafPos = leafPos.up()
            setBlockIfClear(worldGenLevel, leafPos, LeavesBlock.updateDistanceFromLogs(leafBlock, worldGenLevel, leafPos))
            apricornSpots.add(direction.opposite to leafPos.offset(direction))

            allApricornSpots.add(apricornSpots)
        }

        for (coords in Lists.newArrayList(Pair(1, 2), Pair(-1, 2), Pair(1, -2), Pair(-2, 1), Pair(2, 1), Pair(-2, -1), Pair(-1, -2), Pair(2, -1))) {
            val apricornSpots = mutableListOf<Pair<Direction, BlockPos>>()
            var leafPos = layerTwoPos.add(coords.first, 0, coords.second)
            setBlockIfClear(worldGenLevel, leafPos, LeavesBlock.updateDistanceFromLogs(leafBlock, worldGenLevel, leafPos))

            for (direction in listOf(NORTH, EAST, SOUTH, WEST)) {
                val apricornPos = leafPos.offset(direction)
                if (isAir(worldGenLevel, apricornPos)) {
                    apricornSpots.add(direction.opposite to apricornPos)
                }
            }

            leafPos = leafPos.up()
            setBlockIfClear(worldGenLevel, leafPos, LeavesBlock.updateDistanceFromLogs(leafBlock, worldGenLevel, leafPos))

            for (direction in listOf(NORTH, EAST, SOUTH, WEST)) {
                val apricornPos = leafPos.offset(direction)
                if (isAir(worldGenLevel, apricornPos)) {
                    apricornSpots.add(direction.opposite to apricornPos)
                }
            }

            allApricornSpots.add(apricornSpots)
        }

        // Topper
        val topperPos = origin.add(0, 5, 0)
        setBlockIfClear(worldGenLevel, topperPos, LeavesBlock.updateDistanceFromLogs(leafBlock, worldGenLevel, topperPos))

        for (direction in Lists.newArrayList(NORTH, EAST, SOUTH, WEST)) {
            val leafPos = topperPos.offset(direction)
            setBlockIfClear(worldGenLevel, leafPos, LeavesBlock.updateDistanceFromLogs(leafBlock, worldGenLevel, leafPos))
        }

        for (blocks in getLayerFourVariation(origin.offset(UP, 4), random)) {
            for (block in blocks) {
                setBlockIfClear(worldGenLevel, block, LeavesBlock.updateDistanceFromLogs(leafBlock, worldGenLevel, block))
            }
        }

        if (allApricornSpots.isNotEmpty()) {
            allApricornSpots.filter(List<*>::isNotEmpty)
                .randomNoCopy(allApricornSpots.size.coerceAtMost(8))
                .map { it.random() }
                .forEach {
                    if(worldGenLevel.getBlockState(it.second.offset(it.first)).block.equals(leafBlock.block)) {
                        setBlockIfClear(
                            worldGenLevel,
                            it.second,
                            context.config.state
                                .with(HorizontalFacingBlock.FACING, it.first)
                                .with(
                                    ApricornBlock.AGE,
                                    if (isGenerating) random.nextInt(ApricornBlock.MAX_AGE + 1) else 0
                                )
                        )
                    }
                }
        }
        return true
    }

    private fun setBlockIfClear(worldGenLevel: StructureWorldAccess, blockPos: BlockPos, blockState: BlockState) {
        if (!TreeFeature.isAirOrLeaves(worldGenLevel, blockPos)) {
            return
        }
        worldGenLevel.setBlockState(blockPos, blockState, 3)
    }

    private fun getLayerOneVariation(origin: BlockPos, random: Random): Pair<BlockPos, BlockPos> {
        var direction = NORTH
        when (random.nextInt(4)) {
            1 -> direction = EAST
            2 -> direction = SOUTH
            3 -> direction = WEST
        }
        val posOne = origin.add(direction.offsetX * 2, direction.offsetY * 2, direction.offsetZ * 2)
        val offset = if (random.nextBoolean()) -1 else 1
        val posTwo = if (direction.offsetX == 0) posOne.add(offset, 0, 0) else posOne.add(0, 0, offset)
        return posOne to posTwo
    }

    private fun getLayerFourVariation(origin: BlockPos, random: Random): List<List<BlockPos>> {
        val variationList = mutableListOf<List<BlockPos>>()
        val usedDirections = mutableListOf<Direction>()

        for (i in 1..nextInt(2,4)) {
            var direction: Direction? = null

            while (direction == null || usedDirections.contains(direction)) {
                when (random.nextInt(4)) {
                    0 -> direction = NORTH
                    1 -> direction = EAST
                    2 -> direction = SOUTH
                    3 -> direction = WEST
                }
            }

            val posOne = origin.add(direction.offsetX * 2, direction.offsetY * 2, direction.offsetZ * 2)
            val offset = if (random.nextBoolean()) -1 else 1
            val posTwo = if (direction.offsetX == 0) posOne.add(offset, 0, 0) else posOne.add(0, 0, offset)
            if (random.nextInt(3) == 0) {
                variationList.add(listOf(posOne, posTwo))
            } else {
                variationList.add(listOf(if (random.nextBoolean()) posOne else posTwo))
            }
        }
        return variationList
    }

    private fun isAir(testableWorld: TestableWorld, blockPos: BlockPos?): Boolean {
        return testableWorld.testBlockState(
            blockPos
        ) { blockState: BlockState ->
            blockState.isOf(
                Blocks.AIR
            )
        }
    }

}