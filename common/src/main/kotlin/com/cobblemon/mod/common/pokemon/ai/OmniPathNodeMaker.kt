/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.ai

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.canFit
import com.google.common.collect.Maps
import it.unimi.dsi.fastutil.longs.Long2ObjectFunction
import it.unimi.dsi.fastutil.longs.Long2ObjectMap
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import java.util.EnumSet
import net.minecraft.block.AbstractRailBlock
import net.minecraft.entity.ai.pathing.NavigationType
import net.minecraft.entity.ai.pathing.PathNode
import net.minecraft.entity.ai.pathing.PathNodeMaker
import net.minecraft.entity.ai.pathing.PathNodeType
import net.minecraft.entity.ai.pathing.TargetPathNode
import net.minecraft.entity.mob.MobEntity
import net.minecraft.fluid.FluidState
import net.minecraft.tag.FluidTags
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.world.BlockView
import net.minecraft.world.chunk.ChunkCache

/**
 * A path node maker that constructs paths knowing that the entity might be capable of
 * traveling across land, water, and air. This most closely resembles the aquatic
 * node maker.
 *
 * @author Hiroku
 * @since September 10th, 2022
 */
class OmniPathNodeMaker : PathNodeMaker() {
    private val nodePosToType: Long2ObjectMap<PathNodeType> = Long2ObjectOpenHashMap()

    override fun init(cachedWorld: ChunkCache, entity: MobEntity) {
        super.init(cachedWorld, entity)
        nodePosToType.clear()
    }

    override fun clear() {
        super.clear()
        nodePosToType.clear()
    }

    override fun getStart(): PathNode? {
        return super.getNode(
            MathHelper.floor(entity.boundingBox.minX),
            MathHelper.floor(entity.boundingBox.minY + 0.5),
            MathHelper.floor(entity.boundingBox.minZ)
        )
    }

    override fun getNode(x: Double, y: Double, z: Double): TargetPathNode? {
        return asTargetPathNode(super.getNode(MathHelper.floor(x), MathHelper.floor(y + 0.5), MathHelper.floor(z)))
    }

    override fun getSuccessors(successors: Array<PathNode>, node: PathNode): Int {
        var i = 0
        val map = Maps.newEnumMap<Direction, PathNode?>(Direction::class.java)
        val upperMap = Maps.newEnumMap<Direction, PathNode?>(Direction::class.java)
        val lowerMap = Maps.newEnumMap<Direction, PathNode?>(Direction::class.java)

        val upIsOpen = entity.canFit(node.blockPos.up())

        // Non-diagonal surroundings in 3d space
        for (direction in Direction.values()) {
            val pathNode = this.getNode(node.x + direction.offsetX, node.y + direction.offsetY, node.z + direction.offsetZ) ?: continue
            map[direction] = pathNode
            if (!hasNotVisited(pathNode)) {
                continue
            }
            successors[i++] = pathNode
        }

        // Diagonals
        for (direction in Direction.Type.HORIZONTAL.iterator()) {
            val direction2 = direction.rotateYClockwise()
            val x = node.x + direction.offsetX + direction2.offsetX
            val z = node.z + direction.offsetZ + direction2.offsetZ
            val pathNode2 = this.getNode(x, node.y, z) ?: continue
            if (isAccessibleDiagonal(pathNode2, map[direction], map[direction2])) {
                successors[i++] = pathNode2
            }
        }

        // Upward non-diagonals
        for (direction in Direction.Type.HORIZONTAL.iterator()) {
            val pathNode2 = getNode(node.x + direction.offsetX, node.y + 1, node.z + direction.offsetZ) ?: continue
            if (upIsOpen && hasNotVisited(pathNode2)) {
                successors[i++] = pathNode2
                upperMap[direction] = pathNode2
            }
        }

        // Upward diagonals
        for (direction in Direction.Type.HORIZONTAL.iterator()) {
            val direction2 = direction.rotateYClockwise()
            val pathNode2 = getNode(node.x + direction.offsetX + direction2.offsetX, node.y + 1, node.z + direction.offsetZ + direction2.offsetZ) ?: continue
            if (isAccessibleDiagonal(pathNode2, upperMap[direction], upperMap[direction2])) {
                successors[i++] = pathNode2
            }
        }

        val connectingBlockPos = BlockPos.Mutable()
        // Downward non-diagonals
        for (direction in Direction.Type.HORIZONTAL.iterator()) {
            connectingBlockPos.set(node.blockPos.add(direction.vector))
            val blockState = cachedWorld.getBlockState(connectingBlockPos)
            val traversableByTangent = blockState.canPathfindThrough(cachedWorld, connectingBlockPos, NavigationType.AIR)
            val pathNode2 = getNode(node.x + direction.offsetX, node.y - 1, node.z + direction.offsetZ) ?: continue
            if (hasNotVisited(pathNode2) && traversableByTangent) {
                successors[i++] = pathNode2
                lowerMap[direction] = pathNode2
            }
        }

        // Downward diagonals
        for (direction in Direction.Type.HORIZONTAL.iterator()) {
            val direction2 = direction.rotateYClockwise()
            val pathNode2 = getNode(node.x + direction.offsetX + direction2.offsetX, node.y - 1, node.z + direction.offsetZ + direction2.offsetZ) ?: continue
            if (isAccessibleDiagonal(pathNode2, lowerMap[direction], lowerMap[direction2])) {
                successors[i++] = pathNode2
            }
        }

        return i
    }

    fun hasNotVisited(pathNode: PathNode?): Boolean {
        return pathNode != null && !pathNode.visited
    }

    fun isAccessibleDiagonal(pathNode: PathNode?, vararg borderNodes: PathNode?): Boolean {
        return hasNotVisited(pathNode) && borderNodes.all { it != null && it.penalty >= 0.0F }
    }

    fun isValidPathNodeType(pathNodeType: PathNodeType): Boolean {
        return when {
            (pathNodeType == PathNodeType.BREACH || pathNodeType == PathNodeType.WATER || pathNodeType == PathNodeType.WATER_BORDER) && canSwimInWater() -> true
            pathNodeType == PathNodeType.OPEN && canFly() -> true
            pathNodeType == PathNodeType.WALKABLE && (canWalk() || canFly()) -> true
            else -> false
        }
    }

    override fun getNode(x: Int, y: Int, z: Int): PathNode? {
        var nodePenalty = 0F
        var pathNode: PathNode? = null

        val pathNodeType = addPathNodePos(x, y, z)
        if (isValidPathNodeType(pathNodeType) &&
            entity.getPathfindingPenalty(pathNodeType).also { nodePenalty = it } >= 0.0f &&
            super.getNode(x, y, z).also { pathNode = it } != null
        ) {
            pathNode!!.type = pathNodeType
            pathNode!!.penalty = pathNode!!.penalty.coerceAtLeast(nodePenalty)
        }
        return pathNode
    }

    fun addPathNodePos(x: Int, y: Int, z: Int): PathNodeType {
        return nodePosToType.computeIfAbsent(BlockPos.asLong(x, y, z), Long2ObjectFunction { getNodeType(cachedWorld, x, y, z, entity, entityBlockXSize, entityBlockYSize, entityBlockZSize, false, true) })
    }

    override fun getDefaultNodeType(world: BlockView, x: Int, y: Int, z: Int): PathNodeType {
        val pos = BlockPos(x, y, z)
        val below = BlockPos(x, y - 1, z)
        val blockState = world.getBlockState(pos)
        val blockStateBelow = world.getBlockState(below)
        val belowSolid = blockStateBelow.isSolidBlock(world, below)
        val isWater = blockState.fluidState.isIn(FluidTags.WATER)
        val isLava = blockState.fluidState.isIn(FluidTags.LAVA)
        val canBreatheUnderFluid = canSwimUnderFluid(blockState.fluidState)
        return if (isWater && belowSolid && !canSwimInWater() && canBreatheUnderFluid) {
            PathNodeType.WALKABLE
//        } else if (isWater && !belowSolid && !canSwimInWater() && canBreatheUnderFluid) {
//            PathNodeType.OPEN
        } else if (isWater || (isLava && canSwimUnderlava())) {
            PathNodeType.WATER
        } else if (blockState.canPathfindThrough(world, pos, NavigationType.LAND) && !blockStateBelow.canPathfindThrough(world, below, NavigationType.AIR)) {
            PathNodeType.WALKABLE
        } else if (blockState.canPathfindThrough(world, pos, NavigationType.AIR) && blockStateBelow.canPathfindThrough(world, below, NavigationType.AIR)) {
            PathNodeType.OPEN
        } else PathNodeType.BLOCKED
    }

    override fun getNodeType(
        world: BlockView,
        x: Int,
        y: Int,
        z: Int,
        mob: MobEntity?,
        sizeX: Int,
        sizeY: Int,
        sizeZ: Int,
        canOpenDoors: Boolean,
        canEnterOpenDoors: Boolean
    ): PathNodeType {
        val set = EnumSet.noneOf(PathNodeType::class.java)
        var type = findNearbyNodeTypes(world, x, y, z, sizeX, sizeY, sizeZ, canOpenDoors, canEnterOpenDoors, set, PathNodeType.BLOCKED, BlockPos(x, y, z))
//        if (type == PathNodeType.WATER && PathNodeType.WALKABLE in set) {
//            type = PathNodeType.WALKABLE
//        }

        return if (set.contains(PathNodeType.FENCE)) {
            PathNodeType.FENCE
        } else if (set.contains(PathNodeType.UNPASSABLE_RAIL)) {
            PathNodeType.UNPASSABLE_RAIL
        } else {
            var pathNodeType2: PathNodeType? = PathNodeType.BLOCKED
            val nearbyTypeIterator = set.iterator()
            while (nearbyTypeIterator.hasNext()) {
                val nearbyType = nearbyTypeIterator.next()
                if (mob!!.getPathfindingPenalty(nearbyType) < 0) {
                    return nearbyType
                }
                // The || is because we prefer WALKABLE where possible - OPEN is legit but if there's either OPEN or WALKABLE then WALKABLE is better since land pokes can read that.
                if (mob.getPathfindingPenalty(nearbyType) > mob.getPathfindingPenalty(pathNodeType2) || (nearbyType == PathNodeType.WALKABLE)) {
                    pathNodeType2 = nearbyType
                } else if (type == PathNodeType.WATER && nearbyType == PathNodeType.WATER) {
                    pathNodeType2 = PathNodeType.WATER
                }

            }
            if (type == PathNodeType.OPEN && mob!!.getPathfindingPenalty(pathNodeType2) == 0.0f && sizeX <= 1) {
                PathNodeType.OPEN
            } else {
                pathNodeType2!!
            }
        }
    }

    fun findNearbyNodeTypes(
        world: BlockView,
        x: Int,
        y: Int,
        z: Int,
        sizeX: Int,
        sizeY: Int,
        sizeZ: Int,
        canOpenDoors: Boolean,
        canEnterOpenDoors: Boolean,
        nearbyTypes: EnumSet<PathNodeType>,
        type: PathNodeType,
        pos: BlockPos
    ): PathNodeType {
        var type = type
        for (i in 0 until sizeX) {
            for (j in 0 until sizeY) {
                for (k in 0 until sizeZ) {
                    val l = i + x
                    val m = j + y
                    val n = k + z
                    var pathNodeType = getDefaultNodeType(world, l, m, n)
                    pathNodeType = this.adjustNodeType(world, canOpenDoors, canEnterOpenDoors, pos, pathNodeType)
                    if (i == 0 && j == 0 && k == 0) {
                        type = pathNodeType
                    }
                    nearbyTypes.add(pathNodeType)
                }
            }
        }
        return type
    }

    protected fun adjustNodeType(
        world: BlockView,
        canOpenDoors: Boolean,
        canEnterOpenDoors: Boolean,
        pos: BlockPos,
        type: PathNodeType
    ): PathNodeType {
        var type = type
        if (type == PathNodeType.DOOR_WOOD_CLOSED && canOpenDoors && canEnterOpenDoors) {
            type = PathNodeType.WALKABLE_DOOR
        }
        if (type == PathNodeType.DOOR_OPEN && !canEnterOpenDoors) {
            type = PathNodeType.BLOCKED
        }
        if (type == PathNodeType.RAIL && world.getBlockState(pos).block !is AbstractRailBlock && world.getBlockState(pos.down()).block !is AbstractRailBlock) {
            type = PathNodeType.UNPASSABLE_RAIL
        }
        if (type == PathNodeType.LEAVES) {
            type = PathNodeType.BLOCKED
        }
        return type
    }

    fun canWalk(): Boolean {
        return if (this.entity is PokemonEntity) {
            (this.entity as PokemonEntity).behaviour.moving.walk.canWalk
        } else {
            true
        }
    }

     fun canSwimInWater(): Boolean {
         return if (this.entity is PokemonEntity) {
             (this.entity as PokemonEntity).behaviour.moving.swim.canSwimInWater
         } else {
             false
         }
     }

    fun canSwimUnderlava(): Boolean {
        return if (this.entity is PokemonEntity) {
            (this.entity as PokemonEntity).behaviour.moving.swim.canBreatheUnderlava
        } else {
            false
        }
    }

    fun canSwimUnderFluid(fluidState: FluidState): Boolean {
        return if (this.entity is PokemonEntity) {
            if (fluidState.isIn(FluidTags.LAVA)) {
                (this.entity as PokemonEntity).behaviour.moving.swim.canBreatheUnderlava
            } else if (fluidState.isIn(FluidTags.WATER)) {
                (this.entity as PokemonEntity).behaviour.moving.swim.canBreatheUnderwater
            } else {
                false
            }
        } else {
            false
        }
    }

    fun canFly(): Boolean {
        return if (this.entity is PokemonEntity) {
            (this.entity as PokemonEntity).behaviour.moving.fly.canFly
        } else {
            false
        }
    }
}