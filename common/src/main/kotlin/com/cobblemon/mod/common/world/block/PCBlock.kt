/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.world.block

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.storage.pc.link.PCLinkManager
import com.cobblemon.mod.common.api.storage.pc.link.ProximityPCLink
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.net.messages.client.storage.pc.OpenPCPacket
import com.cobblemon.mod.common.util.isInBattle
import com.cobblemon.mod.common.util.lang
import com.cobblemon.mod.common.util.playSoundServer
import com.cobblemon.mod.common.util.toVec3d
import com.cobblemon.mod.common.world.block.entity.PCBlockEntity
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.pathing.NavigationType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.state.StateManager
import net.minecraft.state.property.EnumProperty
import net.minecraft.util.*
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.World

class PCBlock(properties: Settings): BlockWithEntity(properties) {
    companion object {
        val NORTH_AABB = Block.createCuboidShape(1.5, 0.0, 0.0, 14.5, 16.0, 16.0)
        val SOUTH_AABB = Block.createCuboidShape(1.5, 0.0, 0.0, 14.5, 16.0, 16.0)
        val WEST_AABB = Block.createCuboidShape(0.0, 0.0, 1.5, 16.0, 16.0, 14.5)
        val EAST_AABB = Block.createCuboidShape(0.0, 0.0, 1.5, 16.0, 16.0, 14.5)
        val PART = EnumProperty.of("part", PCPart::class.java)
    }

    enum class PCPart(private val label: String) : StringIdentifiable {
        TOP("top"),
        BOTTOM("bottom");
        override fun asString() = label
    }

    init {
        defaultState = this.stateManager.defaultState.with(HorizontalFacingBlock.FACING, Direction.NORTH).with(
            PART,
            PCPart.BOTTOM
        )
    }

    override fun createBlockEntity(blockPos: BlockPos, blockState: BlockState) = PCBlockEntity(blockPos, blockState)

    @Deprecated("Deprecated in Java")
    override fun getCollisionShape(blockState: BlockState, blockGetter: BlockView, blockPos: BlockPos, collisionContext: ShapeContext): VoxelShape {
        return when (blockState.get(HorizontalFacingBlock.FACING)) {
            Direction.SOUTH -> SOUTH_AABB
            Direction.WEST -> WEST_AABB
            Direction.EAST -> EAST_AABB
            else -> NORTH_AABB
        }
    }

    fun getPositionOfOtherPart(state: BlockState, pos: BlockPos): BlockPos {
        return if (state.get(PART) == PCPart.BOTTOM) {
            pos.up()
        } else {
            pos.down()
        }
    }

    fun getBase(state: BlockState, pos: BlockPos): BlockPos {
        return if (state.get(PART) == PCPart.TOP) {
            pos.down()
        } else {
            pos
        }
    }
    override fun onBreak(world: World, pos: BlockPos, state: BlockState, player: PlayerEntity?) {
        super.onBreak(world, pos, state, player)
        val otherPart = world.getBlockState(getPositionOfOtherPart(state, pos))
        if (otherPart.block is PCBlock) {
            world.setBlockState(getPositionOfOtherPart(state, pos), Blocks.AIR.defaultState, 35)
            world.syncWorldEvent(player, 2001, getPositionOfOtherPart(state, pos), Block.getRawIdFromState(otherPart))
        }
    }

    override fun onPlaced(
        world: World,
        pos: BlockPos,
        state: BlockState,
        placer: LivingEntity?,
        itemStack: ItemStack?
    ) {
        super.onPlaced(world, pos, state, placer, itemStack)
        if (!world.isClient) {
            val blockPos = getPositionOfOtherPart(state, pos)
            world.setBlockState(blockPos, state.with(PART, PCPart.TOP), 3)
            world.updateNeighbors(pos, Blocks.AIR)
            state.updateNeighbors(world, pos, 3)
        }
    }

    override fun getPlacementState(blockPlaceContext: ItemPlacementContext): BlockState? {
        val abovePosition = blockPlaceContext.blockPos.up()
        val world = blockPlaceContext.world
        if (world.getBlockState(abovePosition).canReplace(blockPlaceContext) && !world.isOutOfHeightLimit(abovePosition)) {
            return defaultState
                .with(HorizontalFacingBlock.FACING, blockPlaceContext.playerFacing)
                .with(PART, PCPart.BOTTOM)
        }

        return null

    }

    @Deprecated("Deprecated in Java")
    override fun canPathfindThrough(blockState: BlockState, blockGetter: BlockView, blockPos: BlockPos, pathComputationType: NavigationType) = false

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(HorizontalFacingBlock.FACING)
        builder.add(PART)
    }

    @Deprecated("Deprecated in Java")
    override fun rotate(blockState: BlockState, rotation: BlockRotation) =
        blockState.with(HorizontalFacingBlock.FACING, rotation.rotate(blockState.get(HorizontalFacingBlock.FACING)))

    @Deprecated("Deprecated in Java")
    override fun mirror(blockState: BlockState, mirror: BlockMirror): BlockState {
        return blockState.rotate(mirror.getRotation(blockState.get(HorizontalFacingBlock.FACING)))
    }

    @Deprecated("Deprecated in Java")
    override fun onUse(blockState: BlockState, world: World, blockPos: BlockPos, player: PlayerEntity, interactionHand: Hand, blockHitResult: BlockHitResult): ActionResult {
        if (player !is ServerPlayerEntity) {
            return ActionResult.SUCCESS
        }

        val blockEntity = world.getBlockEntity(blockPos)
        if (blockEntity !is PCBlockEntity) {
            return ActionResult.SUCCESS
        }
        if (player.isInBattle()) {
            player.sendMessage(lang("pc.inbattle").red())
            return ActionResult.SUCCESS
        }
        val pc = Cobblemon.storage.getPCForPlayer(player, blockEntity) ?: return ActionResult.SUCCESS
        // TODO add event to check if they can open this PC?
        PCLinkManager.addLink(ProximityPCLink(pc, player.uuid, blockEntity))
        OpenPCPacket(pc.uuid).sendToPlayer(player)
        world.playSoundServer(position = blockPos.toVec3d(), sound = CobblemonSounds.PC_ON.get(), volume = 1F, pitch = 1F)
        return ActionResult.SUCCESS
    }

    override fun <T : BlockEntity> getTicker(world: World, blockState: BlockState, BlockWithEntityType: BlockEntityType<T>) = null

    @Deprecated("Deprecated in Java")
    override fun getRenderType(blockState: BlockState): BlockRenderType {
        return if (blockState.get(PART) == PCPart.TOP) {
            BlockRenderType.INVISIBLE
        } else {
            BlockRenderType.MODEL
        }
    }
}