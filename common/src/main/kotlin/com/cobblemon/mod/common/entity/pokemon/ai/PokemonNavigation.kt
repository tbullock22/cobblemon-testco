/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.ai

import com.cobblemon.mod.common.entity.pokemon.PokemonBehaviourFlag
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.ai.OmniPathNodeMaker
import com.cobblemon.mod.common.util.getWaterAndLavaIn
import com.cobblemon.mod.common.util.toVec3d
import com.google.common.collect.ImmutableSet
import kotlin.math.abs
import net.minecraft.entity.Entity
import net.minecraft.entity.ai.pathing.MobNavigation
import net.minecraft.entity.ai.pathing.NavigationType
import net.minecraft.entity.ai.pathing.Path
import net.minecraft.entity.ai.pathing.PathNode
import net.minecraft.entity.ai.pathing.PathNodeNavigator
import net.minecraft.entity.ai.pathing.PathNodeType
import net.minecraft.tag.FluidTags
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

class PokemonNavigation(val world: World, val pokemonEntity: PokemonEntity) : MobNavigation(pokemonEntity, world) {
    val moving = pokemonEntity.behaviour.moving

    override fun createPathNodeNavigator(range: Int): PathNodeNavigator {
        this.nodeMaker = OmniPathNodeMaker()
        nodeMaker.setCanEnterOpenDoors(true)
        return PathNodeNavigator(nodeMaker, range)
    }

    override fun isAtValidPosition(): Boolean {
        val (_, isTouchingLava) = entity.world.getWaterAndLavaIn(entity.boundingBox)
        val isAtValidPosition = (!entity.isInLava && !entity.isSubmergedIn(FluidTags.LAVA)) ||
                (isTouchingLava && moving.swim.canSwimInLava) ||
                this.entity.hasVehicle()
        return isAtValidPosition
    }

    override fun canSwim(): Boolean {
        return moving.swim.canSwimInWater
    }

    override fun getPos() = Vec3d(entity.x, getPathfindingY().toDouble(), entity.z)

    override fun continueFollowingPath() {
        val vec3d = this.pos
        nodeReachProximity = if (entity.width > 0.75f) entity.width / 2.0f else 0.75f - entity.width / 2.0f
        /*
         * The difference between this and the overrided function is that we use the vector
         * position for the d,e,f which improves behaviour of larger pokemon
         */
        val targetVec3d = currentPath!!.getNodePosition(entity)
        val d = abs(entity.x - targetVec3d.x)
        val e = abs(entity.y - targetVec3d.y)
        val f = abs(entity.z - targetVec3d.z)
        val bl = d < nodeReachProximity.toDouble() && f < this.nodeReachProximity.toDouble() && e < 1.0
        if (bl || entity.canJumpToNextPathNode(currentPath!!.currentNode.type) && shouldJumpToNextNode(vec3d)) {
            currentPath!!.next()
        }

        checkTimeouts(vec3d)
    }

    fun isAirborne(world: World, pos: BlockPos) =
        world.getBlockState(pos).canPathfindThrough(world, pos, NavigationType.AIR)
                && world.getBlockState(pos.down(1)).canPathfindThrough(world, pos.down(1), NavigationType.AIR)
                && world.getBlockState(pos.down(2)).canPathfindThrough(world, pos.down(2), NavigationType.AIR)

    override fun tick() {
        super.tick()
        val node = getCurrentPath()?.lastNode

        val isFlying = pokemonEntity.getBehaviourFlag(PokemonBehaviourFlag.FLYING)
        val canWalk = pokemonEntity.behaviour.moving.walk.canWalk
        val canFly = pokemonEntity.behaviour.moving.fly.canFly
        if (node != null) {
            if (node.type == PathNodeType.OPEN) {
                val canFly = moving.fly.canFly
                if (canFly && !pokemonEntity.getBehaviourFlag(PokemonBehaviourFlag.FLYING)) {
                    pokemonEntity.setBehaviourFlag(PokemonBehaviourFlag.FLYING, true)
                }
            } else if (node.type != PathNodeType.OPEN && isFlying && canWalk) {
                pokemonEntity.setBehaviourFlag(PokemonBehaviourFlag.FLYING, false)
            }
        } else if (!isFlying && canFly && isAirborne(pokemonEntity.world, pokemonEntity.blockPos)) {
            pokemonEntity.setBehaviourFlag(PokemonBehaviourFlag.FLYING, true)
        } else if (isFlying && canWalk && !pokemonEntity.world.getBlockState(pokemonEntity.blockPos).canPathfindThrough(pokemonEntity.world, pokemonEntity.blockPos.down(), NavigationType.LAND)) {
            pokemonEntity.setBehaviourFlag(PokemonBehaviourFlag.FLYING, false)
        }
    }

    var checked = false

    fun findPath(target: BlockPos, distance: Int): Path? = findPathTo(ImmutableSet.of(target), 8, false, distance)

    override fun findPathTo(target: BlockPos, distance: Int): Path? {
        var target = target

        var blockPos: BlockPos
        if (this.world.getBlockState(target).isAir && !pokemonEntity.behaviour.moving.fly.canFly) {
            blockPos = target.down()
            while (blockPos.y > this.world.bottomY && this.world.getBlockState(blockPos).isAir) {
                blockPos = blockPos.down()
            }
            while (blockPos.y < this.world.topY && this.world.getBlockState(blockPos).isAir) {
                blockPos = blockPos.up()
            }
            target = blockPos
        }

        val path = if (!this.world.getBlockState(target).material.isSolid) {
            findPath(target, distance)
        } else {
            blockPos = target.up()
            while (blockPos.y < this.world.topY && this.world.getBlockState(blockPos).material.isSolid) {
                blockPos = blockPos.up()
            }
            findPath(blockPos, distance)
        }

//        path?.let {
//            try {
//                var i = 0
//                while (true) {
//                    val node = it.getNode(i)
//                    val blockState = if (node.type == PathNodeType.OPEN) {
//                        Blocks.BONE_BLOCK.defaultState
//                    } else if (node.type == PathNodeType.WALKABLE) {
//                        Blocks.GOLD_BLOCK.defaultState
//                    } else if (node.type == PathNodeType.WATER) {
//                        Blocks.PACKED_ICE.defaultState
//                    } else {
//                        Blocks.COAL_BLOCK.defaultState
//                    }
//                    entity.world.setBlockState(node.blockPos, blockState)
//                    i++
//                }
//            } catch(e: Exception) {
//            }
//            entity.remove(Entity.RemovalReason.DISCARDED)
//        }

        return path
    }

    override fun findPathTo(entity: Entity, distance: Int): Path? {
        return this.findPathTo(entity.blockPos, distance)
    }

    override fun adjustPath() {
        super.adjustPath()
        val path = getCurrentPath() ?: return
        var i = 2
        while (i < path.length) {
            val firstNode = path.getNode(i - 2)
            val middleNode = path.getNode(i - 1)
            val nextNode = path.getNode(i)

            val directionToMiddle = middleNode.blockPos.subtract(firstNode.blockPos).toVec3d().normalize()
            val nodeType = firstNode.type
            if (nodeType != middleNode.type || nodeType != nextNode.type || nodeType == PathNodeType.WALKABLE) {
                i++
                continue
            }

            val directionToEnd = nextNode.blockPos.subtract(middleNode.blockPos).toVec3d().normalize()
            if (directionToEnd != directionToMiddle) {
                i++
                continue
            }

            // 3 of the same node in a row in the same direction - the second node is unnecessary
            val remainingNodes = mutableListOf<PathNode>()
            var j = i
            while (true) {
                try {
                    remainingNodes.add(path.getNode(j))
                    j++
                } catch(e: Exception) { break }
            }

            path.length = i + remainingNodes.size - 1
            for (k in remainingNodes.indices) {
                path.setNode(i - 1 + k, remainingNodes[k])
            }
        }

        // Could check for direct sunlight or rain or something
//        if (avoidSunlight) {
//            if (this.world.isSkyVisible(BlockPos(entity.x, entity.y + 0.5, entity.z))) {
//                return
//            }
//            for (i in 0 until currentPath!!.length) {
//                val pathNode = currentPath!!.getNode(i)
//                if (!this.world.isSkyVisible(BlockPos(pathNode.x, pathNode.y, pathNode.z))) continue
//                currentPath!!.length = i
//                return
//            }
//        }
    }

    fun getPathfindingY(): Int {
        val inSwimmableFluid = (entity.isSubmergedIn(FluidTags.WATER) && moving.swim.canSwimInWater) ||
                (entity.isSubmergedIn(FluidTags.LAVA) && moving.swim.canSwimInLava)
        if (!inSwimmableFluid) {
            return MathHelper.floor(entity.y + 0.5)
        }

        return entity.blockY
    }
}