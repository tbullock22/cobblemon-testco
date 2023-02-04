/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.spawner

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.spawning.SpawnCause
import com.cobblemon.mod.common.api.spawning.SpawnerManager
import com.cobblemon.mod.common.api.spawning.context.AreaContextResolver
import com.cobblemon.mod.common.api.spawning.context.SpawningContext
import com.cobblemon.mod.common.api.spawning.context.calculators.AreaSpawningContextCalculator
import com.cobblemon.mod.common.api.spawning.context.calculators.SpawningContextCalculator.Companion.prioritizedAreaCalculators
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail
import com.cobblemon.mod.common.api.spawning.detail.SpawnPool
import com.cobblemon.mod.common.api.spawning.mixins.CachedOnlyChunkAccessor
import com.cobblemon.mod.common.api.spawning.prospecting.SpawningProspector
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.squeezeWithinBounds
import com.cobblemon.mod.common.util.toVec3f
import net.minecraft.entity.ai.pathing.NavigationType
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.ChunkSectionPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk
import net.minecraft.world.chunk.ChunkStatus

/**
 * A type of [TickingSpawner] that operates within some area. When this spawner type
 * is told to do a spawning action, the subclass can provide a [SpawningArea] to use.
 * If a non-null value is returned, the [prospector] and [resolver] will be used to
 * select a spawn and action it.
 *
 * Subclasses must implement the function retrieving what area to do the spawning in,
 * but otherwise this class is feature-complete.
 *
 * @author Hiroku
 * @since February 5th, 2022
 */
abstract class AreaSpawner(
    name: String,
    spawns: SpawnPool,
    manager: SpawnerManager
) : TickingSpawner(name, spawns, manager) {
    abstract fun getArea(cause: SpawnCause): SpawningArea?

    companion object {
        const val CHUNK_REACH = 3
    }

    var prospector: SpawningProspector = Cobblemon.prospector
    var resolver: AreaContextResolver = Cobblemon.areaContextResolver
    var contextCalculators: List<AreaSpawningContextCalculator<*>> = prioritizedAreaCalculators

    override fun run(cause: SpawnCause): Pair<SpawningContext, SpawnDetail>? {
        val area = getArea(cause)
        val constrainedArea = if (area != null) constrainArea(area) else null
        if (constrainedArea != null) {
            val numberNearby = constrainedArea.world.getEntitiesByClass(
                PokemonEntity::class.java,
                Box.of(Vec3d(constrainedArea.getCenter().toVec3f()), CHUNK_REACH * 16.0 * 2, 1000.0, CHUNK_REACH * 16.0 * 2)
            ) { true }.size

            val chunksCovered = CHUNK_REACH * CHUNK_REACH
            if (numberNearby.toFloat() / chunksCovered >= Cobblemon.config.pokemonPerChunk) {
                return null
            }

            //val prospectStart = System.currentTimeMillis()
            val slice = prospector.prospect(this, constrainedArea)
            //val prospectEnd = System.currentTimeMillis()
            val contexts = resolver.resolve(this, contextCalculators, slice)
            //val resolveEnd = System.currentTimeMillis()
            //val prospectDuration = prospectEnd - prospectStart
            //val resolveDuration = resolveEnd - prospectEnd
            //println("Prospecting took: $prospectDuration ms. Resolution took: $resolveDuration ms")
            // Takes about 3ms on my laptop to prospect, similar to context resolve - not very good, needs some thought
            return getSpawningSelector().select(this, contexts)
        }

        return null
    }

    fun isValidStartPoint(world: World, chunk: Chunk, startPos: BlockPos.Mutable): Boolean {
        val y = startPos.y
        if (!world.canSetBlock(startPos) || !world.canSetBlock(startPos.setY(y + 1))) {
            return false
        }

        val mid = chunk.getBlockState(startPos.setY(y))
        val above = chunk.getBlockState(startPos.setY(y + 1))

        // Above must be non-solid
         if (!above.canPathfindThrough(world, startPos, NavigationType.AIR)) {
             return false
         }
        // Position must be non-air
        if (mid.isAir) {
            return false
        }

        return true
    }

    private fun constrainArea(area: SpawningArea): SpawningArea? {
        val basePos = BlockPos.Mutable(area.baseX, area.baseY, area.baseZ)
        val originalY = area.baseY

        val (chunkX, chunkZ) = Pair(ChunkSectionPos.getSectionCoord(area.baseX), ChunkSectionPos.getSectionCoord(area.baseZ))
        val chunk = (area.world.chunkManager as CachedOnlyChunkAccessor).`cobblemon$request`(chunkX, chunkZ, ChunkStatus.FULL) ?: return null

        var valid = isValidStartPoint(area.world, chunk, basePos)

        if (!valid) {
            var offset = 1
            do {
                if (isValidStartPoint(area.world, chunk, basePos.setY(originalY + offset))) {
                    valid = true
                    basePos.y = originalY + offset
                    break
                } else if (isValidStartPoint(area.world, chunk, basePos.setY(originalY - offset))) {
                    valid = true
                    basePos.y = originalY + offset
                    break
                }
                offset++
            } while (offset <= Cobblemon.config.maxVerticalCorrectionBlocks)
        }

        if (valid) {
            val min = area.world.squeezeWithinBounds(basePos)
            val max = area.world.squeezeWithinBounds(basePos.add(area.length, area.height, area.width))
            if (area.world.canSetBlock(min) && area.world.canSetBlock(max) && min.y != max.y) {
                return SpawningArea(
                    cause = area.cause,
                    world = area.world,
                    baseX = min.x,
                    baseY = min.y,
                    baseZ = min.z,
                    length = area.length,
                    height = max.y - min.y,
                    width = area.width
                )
            }
        }

        return null
    }
}
