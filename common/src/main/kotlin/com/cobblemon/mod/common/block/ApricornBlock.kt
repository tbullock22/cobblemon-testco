/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block

import com.cobblemon.mod.common.api.apricorn.Apricorn
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.farming.ApricornHarvestEvent
import com.cobblemon.mod.common.api.tags.CobblemonBlockTags
import com.cobblemon.mod.common.util.playSoundServer
import com.cobblemon.mod.common.util.toVec3d
import net.minecraft.block.*
import net.minecraft.entity.ai.pathing.NavigationType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundEvents
import net.minecraft.state.StateManager
import net.minecraft.state.property.IntProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess
import net.minecraft.world.WorldView
import net.minecraft.world.event.GameEvent

// Note we cannot make this inherit from CocoaBlock since our age properties differ, it is however safe to copy most of the logic from it
class ApricornBlock(settings: Settings, val apricorn: Apricorn) : HorizontalFacingBlock(settings), Fertilizable {

    init {
        this.defaultState = this.stateManager.defaultState
            .with(FACING, Direction.NORTH)
            .with(AGE, MIN_AGE)
    }

    override fun hasRandomTicks(state: BlockState) = state.get(AGE) < MAX_AGE

    @Deprecated("Deprecated in Java")
    override fun randomTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
        // Cocoa block uses a 5 here might as well stay consistent
        if (world.random.nextInt(5) == 0) {
            val currentAge = state.get(AGE)
            if (currentAge < MAX_AGE) {
                world.setBlockState(pos, state.with(AGE, currentAge + 1), 2)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun canPlaceAt(state: BlockState, world: WorldView, pos: BlockPos): Boolean {
        val blockState = world.getBlockState(pos.offset(state.get(FACING) as Direction))
        return blockState.isIn(CobblemonBlockTags.APRICORN_LEAVES)
    }

    @Deprecated("Deprecated in Java")
    override fun getOutlineShape(state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext): VoxelShape {
        val age = state.get(AGE)
        return when (state.get(FACING)) {
            Direction.NORTH -> NORTH_AABB[age]
            Direction.EAST -> EAST_AABB[age]
            Direction.SOUTH -> SOUTH_AABB[age]
            Direction.WEST -> WEST_AABB[age]
            else -> NORTH_AABB[age]
        }
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        var blockState = defaultState
        val worldView = ctx.world
        val blockPos = ctx.blockPos
        ctx.placementDirections.forEach { direction ->
            if (direction.axis.isHorizontal) {
                blockState = blockState.with(FACING, direction) as BlockState
                if (blockState.canPlaceAt(worldView, blockPos)) {
                    return blockState
                }
            }
        }
        return null
    }

    @Deprecated("Deprecated in Java")
    override fun getStateForNeighborUpdate(state: BlockState, direction: Direction, neighborState: BlockState, world: WorldAccess, pos: BlockPos, neighborPos: BlockPos): BlockState? {
        return if (direction == state.get(FACING) && !state.canPlaceAt(world, pos)) Blocks.AIR.defaultState
            else super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos)
    }

    override fun isFertilizable(world: BlockView, pos: BlockPos, state: BlockState, isClient: Boolean) = state.get(AGE) < MAX_AGE

    override fun canGrow(world: World, random: Random, pos: BlockPos, state: BlockState) = true

    override fun grow(world: ServerWorld, random: Random, pos: BlockPos, state: BlockState) {
        world.setBlockState(pos, state.with(AGE, state.get(AGE) + 1), 2)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(FACING, AGE)
    }

    @Deprecated("Deprecated in Java")
    override fun canPathfindThrough(state: BlockState, world: BlockView, pos: BlockPos, type: NavigationType) = false

    @Deprecated("Deprecated in Java")
    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
        if (state.get(AGE) != MAX_AGE) {
            return super.onUse(state, world, pos, player, hand, hit)
        }

        val resetState = this.harvest(world, state, pos)
        world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(player, resetState))

        if (!world.isClient) {
            world.playSoundServer(position = pos.toVec3d(), sound = SoundEvents.ENTITY_ITEM_PICKUP, volume = 0.7F, pitch = 1.4F)

            if (world is ServerWorld && player is ServerPlayerEntity) {
                CobblemonEvents.APRICORN_HARVESTED.post(ApricornHarvestEvent(player, world, pos))
            }
        }
        return ActionResult.success(world.isClient)
    }

    // We need to point back to the actual apricorn item, see SweetBerryBushBlock for example
    override fun getPickStack(world: BlockView, pos: BlockPos, state: BlockState) = ItemStack(this.apricorn.item())

    /**
     * Harvests the apricorn at the given params.
     * This uses [Block.dropStacks] to handle the drops.
     * It will also reset the [BlockState] of this block at the given location to the start of growth.
     *
     * @param world The [World] the apricorn is in.
     * @param state The [BlockState] of the apricorn.
     * @param pos The [BlockPos] of the apricorn.
     * @return The [BlockState] after harvest.
     */
    fun harvest(world: World, state: BlockState, pos: BlockPos): BlockState {
        // Uses loot tables, to change the drops use 'data/cobblemon/loot_tables/blocks/<color>_apricorn.json'
        Block.dropStacks(state, world, pos)
        // Don't use default as we want to keep the facing
        val resetState = state.with(AGE, MIN_AGE)
        world.setBlockState(pos, resetState, Block.NOTIFY_LISTENERS)
        return resetState
    }

    companion object {

        val AGE: IntProperty = Properties.AGE_3
        const val MAX_AGE = Properties.AGE_3_MAX
        const val MIN_AGE = 0

        // North
        // Stage 1
        private val NORTH_TOP_STAGE_1 = Block.createCuboidShape(7.0, 11.0, 0.5, 9.0, 11.5, 2.5)
        private val NORTH_BODY_STAGE_1 = Block.createCuboidShape(6.5, 9.0, 0.0, 9.5, 11.0, 3.0)
        private val NORTH_BOTTOM_STAGE_1 = Block.createCuboidShape(7.0, 8.5, 0.5, 9.0, 9.0, 2.5)

        // Stage 2
        private val NORTH_TOP_STAGE_2 = Block.createCuboidShape(6.5, 10.5, 0.5, 9.5, 11.0, 3.5)
        private val NORTH_BODY_STAGE_2 = Block.createCuboidShape(6.0, 7.5, 0.0, 10.0, 10.5, 4.0)
        private val NORTH_BOTTOM_STAGE_2 = Block.createCuboidShape(6.5, 7.0, 0.5, 9.5, 7.5, 3.5)

        // Stage 3
        private val NORTH_TOP_STAGE_3 = Block.createCuboidShape(6.0, 9.75, 0.5, 10.0, 10.5, 4.5)
        private val NORTH_BODY_STAGE_3 = Block.createCuboidShape(5.5, 5.75, 0.0, 10.5, 9.75, 5.0)
        private val NORTH_BOTTOM_STAGE_3 = Block.createCuboidShape(6.0, 5.0, 0.5, 10.0, 5.75, 4.5)

        // Full fruit
        private val NORTH_TOP_FRUIT = Block.createCuboidShape(6.0, 9.0, 1.0, 10.0, 10.0, 5.0)
        private val NORTH_BODY_FRUIT = Block.createCuboidShape(5.0, 4.0, 0.0, 11.0, 9.0, 6.0)
        private val NORTH_BOTTOM_FRUIT = Block.createCuboidShape(5.5, 3.0, 0.5, 10.5, 4.0, 5.5)

        private val NORTH_AABB = arrayOf(
            VoxelShapes.union(NORTH_BODY_STAGE_1, NORTH_TOP_STAGE_1, NORTH_BOTTOM_STAGE_1),
            VoxelShapes.union(NORTH_BODY_STAGE_2, NORTH_TOP_STAGE_2, NORTH_BOTTOM_STAGE_2),
            VoxelShapes.union(NORTH_BODY_STAGE_3, NORTH_TOP_STAGE_3, NORTH_BOTTOM_STAGE_3),
            VoxelShapes.union(NORTH_BODY_FRUIT, NORTH_TOP_FRUIT, NORTH_BOTTOM_FRUIT)
        )

        // South
        // Stage 1
        private val SOUTH_TOP_STAGE_1 = Block.createCuboidShape(7.0, 11.0, 13.5, 9.0, 11.5, 15.5)
        private val SOUTH_BODY_STAGE_1 = Block.createCuboidShape(6.5, 9.0, 13.0, 9.5, 11.0, 16.0)
        private val SOUTH_BOTTOM_STAGE_1 = Block.createCuboidShape(7.0, 8.5, 13.5, 9.0, 9.0, 15.5)

        // Stage 2
        private val SOUTH_TOP_STAGE_2 = Block.createCuboidShape(6.5, 10.5, 12.5, 9.5, 11.0, 15.5)
        private val SOUTH_BODY_STAGE_2 = Block.createCuboidShape(6.0, 7.5, 12.0, 10.0, 10.5, 16.0)
        private val SOUTH_BOTTOM_STAGE_2 = Block.createCuboidShape(6.5, 7.0, 12.5, 9.5, 7.5, 15.5)

        // Stage 3
        private val SOUTH_TOP_STAGE_3 = Block.createCuboidShape(6.0, 9.75, 11.5, 10.0, 10.5, 15.5)
        private val SOUTH_BODY_STAGE_3 = Block.createCuboidShape(5.5, 5.75, 11.0, 10.5, 9.75, 16.0)
        private val SOUTH_BOTTOM_STAGE_3 = Block.createCuboidShape(6.0, 5.0, 11.5, 10.0, 5.75, 15.5)

        // Full fruit
        private val SOUTH_TOP_FRUIT = Block.createCuboidShape(6.0, 9.0, 11.0, 10.0, 10.0, 15.0)
        private val SOUTH_BODY_FRUIT = Block.createCuboidShape(5.0, 4.0, 10.0, 11.0, 9.0, 16.0)
        private val SOUTH_BOTTOM_FRUIT = Block.createCuboidShape(5.5, 3.0, 10.5, 10.5, 4.0, 15.5)

        private val SOUTH_AABB = arrayOf(
            VoxelShapes.union(SOUTH_BODY_STAGE_1, SOUTH_TOP_STAGE_1, SOUTH_BOTTOM_STAGE_1),
            VoxelShapes.union(SOUTH_BODY_STAGE_2, SOUTH_TOP_STAGE_2, SOUTH_BOTTOM_STAGE_2),
            VoxelShapes.union(SOUTH_BODY_STAGE_3, SOUTH_TOP_STAGE_3, SOUTH_BOTTOM_STAGE_3),
            VoxelShapes.union(SOUTH_BODY_FRUIT, SOUTH_TOP_FRUIT, SOUTH_BOTTOM_FRUIT)
        )

        // East
        // Stage 1
        private val EAST_TOP_STAGE_1 = Block.createCuboidShape(13.5, 11.0, 7.0, 15.5, 11.5, 9.0)
        private val EAST_BODY_STAGE_1 = Block.createCuboidShape(13.0, 9.0, 6.5, 16.0, 11.0, 9.5)
        private val EAST_BOTTOM_STAGE_1 = Block.createCuboidShape(13.5, 8.5, 7.0, 15.5, 9.0, 9.0)

        // Stage 2
        private val EAST_TOP_STAGE_2 = Block.createCuboidShape(12.5, 10.5, 6.5, 15.5, 11.0, 9.5)
        private val EAST_BODY_STAGE_2 = Block.createCuboidShape(12.0, 7.5, 6.0, 16.0, 10.5, 10.0)
        private val EAST_BOTTOM_STAGE_2 = Block.createCuboidShape(12.5, 7.0, 6.5, 15.5, 7.5, 9.5)

        // Stage 3
        private val EAST_TOP_STAGE_3 = Block.createCuboidShape(11.5, 9.75, 6.0, 15.5, 10.5, 10.0)
        private val EAST_BODY_STAGE_3 = Block.createCuboidShape(11.0, 5.75, 5.5, 16.0, 9.75, 10.5)
        private val EAST_BOTTOM_STAGE_3 = Block.createCuboidShape(11.5, 5.0, 6.0, 15.5, 5.75, 10.0)

        // Full fruit
        private val EAST_TOP_FRUIT = Block.createCuboidShape(11.0, 9.0, 6.0, 15.0, 10.0, 10.0)
        private val EAST_BODY_FRUIT = Block.createCuboidShape(10.0, 4.0, 5.0, 16.0, 9.0, 11.0)
        private val EAST_BOTTOM_FRUIT = Block.createCuboidShape(10.5, 3.0, 5.5, 15.5, 4.0, 10.5)

        private val EAST_AABB = arrayOf(
            VoxelShapes.union(EAST_BODY_STAGE_1, EAST_TOP_STAGE_1, EAST_BOTTOM_STAGE_1),
            VoxelShapes.union(EAST_BODY_STAGE_2, EAST_TOP_STAGE_2, EAST_BOTTOM_STAGE_2),
            VoxelShapes.union(EAST_BODY_STAGE_3, EAST_TOP_STAGE_3, EAST_BOTTOM_STAGE_3),
            VoxelShapes.union(EAST_BODY_FRUIT, EAST_TOP_FRUIT, EAST_BOTTOM_FRUIT)
        )

        // West
        // Stage 1
        private val WEST_TOP_STAGE_1 = Block.createCuboidShape(0.5, 11.0, 7.0, 2.5, 11.5, 9.0)
        private val WEST_BODY_STAGE_1 = Block.createCuboidShape(0.0, 9.0, 6.5, 3.0, 11.0, 9.5)
        private val WEST_BOTTOM_STAGE_1 = Block.createCuboidShape(0.5, 8.5, 7.0, 2.5, 9.0, 9.0)

        // Stage 2
        private val WEST_TOP_STAGE_2 = Block.createCuboidShape(0.5, 10.5, 6.5, 3.5, 11.0, 9.5)
        private val WEST_BODY_STAGE_2 = Block.createCuboidShape(0.0, 7.5, 6.0, 4.0, 10.5, 10.0)
        private val WEST_BOTTOM_STAGE_2 = Block.createCuboidShape(0.5, 7.0, 6.5, 3.5, 7.5, 9.5)

        // Stage 3
        private val WEST_TOP_STAGE_3 = Block.createCuboidShape(0.5, 9.75, 6.0, 4.5, 10.5, 10.0)
        private val WEST_BODY_STAGE_3 = Block.createCuboidShape(0.0, 5.75, 5.5, 5.0, 9.75, 10.5)
        private val WEST_BOTTOM_STAGE_3 = Block.createCuboidShape(0.5, 5.0, 6.0, 4.5, 5.75, 10.0)

        // Full fruit
        private val WEST_TOP_FRUIT = Block.createCuboidShape(1.0, 9.0, 6.0, 5.0, 10.0, 10.0)
        private val WEST_BODY_FRUIT = Block.createCuboidShape(0.0, 4.0, 5.0, 6.0, 9.0, 11.0)
        private val WEST_BOTTOM_FRUIT = Block.createCuboidShape(0.5, 3.0, 5.5, 5.5, 4.0, 10.5)

        private val WEST_AABB = arrayOf(
            VoxelShapes.union(WEST_BODY_STAGE_1, WEST_TOP_STAGE_1, WEST_BOTTOM_STAGE_1),
            VoxelShapes.union(WEST_BODY_STAGE_2, WEST_TOP_STAGE_2, WEST_BOTTOM_STAGE_2),
            VoxelShapes.union(WEST_BODY_STAGE_3, WEST_TOP_STAGE_3, WEST_BOTTOM_STAGE_3),
            VoxelShapes.union(WEST_BODY_FRUIT, WEST_TOP_FRUIT, WEST_BOTTOM_FRUIT)
        )

    }


}