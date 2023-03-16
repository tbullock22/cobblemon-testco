/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block.entity

import com.cobblemon.mod.common.CobblemonBlockEntities
import com.cobblemon.mod.common.CobblemonBlocks
import com.cobblemon.mod.common.api.storage.pc.link.PCLinkManager
import com.cobblemon.mod.common.api.storage.pc.link.ProximityPCLink
import com.cobblemon.mod.common.block.PCBlock
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.TypeFilter
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.world.World

class PCBlockEntity(
    val blockPos: BlockPos,
    val blockState: BlockState
) : BlockEntity(CobblemonBlockEntities.PC.get(), blockPos, blockState) {

    companion object {
        internal val TICKER = BlockEntityTicker<PCBlockEntity> { world, _, _, blockEntity ->
            if (world.isClient) return@BlockEntityTicker

            blockEntity.togglePCOn(blockEntity.getInRangeViewerCount(world, blockEntity.blockPos) > 0)
        }
    }

    private fun togglePCOn(on: Boolean) {
        val pcBlock = blockState.block as PCBlock

        if (world != null && !world!!.isClient) {
            val world = world!!
            val posBottom = pcBlock.getBasePosition(blockState, blockPos)
            val stateBottom = world.getBlockState(posBottom)

            val posTop = pcBlock.getPositionOfOtherPart(stateBottom, posBottom)
            val stateTop = world.getBlockState(posTop)

            try {
                if (stateBottom.get(PCBlock.ON) != on) {
                    world.setBlockState(posTop, stateTop.with(PCBlock.ON, on))
                    world.setBlockState(posBottom, stateBottom.with(PCBlock.ON, on))
                }
            } catch (exception: IllegalArgumentException) {
                // This is probably a PC from before 1.3. Break it.
                if (world.getBlockState(pos.up()).block is PCBlock) {
                    world.setBlockState(pos.up(), Blocks.AIR.defaultState)
                } else {
                    world.setBlockState(pos.down(), Blocks.AIR.defaultState)
                }
                world.setBlockState(pos, Blocks.AIR.defaultState)
                world.spawnEntity(ItemEntity(world, pos.x + 0.5, pos.y + 1.0, pos.z + 0.5, ItemStack(CobblemonBlocks.PC.get())))
            }
        }
    }

    private fun isPlayerViewing(player: PlayerEntity): Boolean {
        val pcLink = PCLinkManager.getLink(player.uuid)
        return pcLink != null
                && pcLink is ProximityPCLink
                && pcLink.pos == blockPos
                && pcLink.world!!.dimension == player.world.dimension
    }

    private fun getInRangeViewerCount(world: World, pos: BlockPos, range: Double = 5.0): Int {
        val box = Box(
            pos.x.toDouble() - range,
            pos.y.toDouble() - range,
            pos.z.toDouble() - range,
            (pos.x + 1).toDouble() + range,
            (pos.y + 1).toDouble() + range,
            (pos.z + 1).toDouble() + range
        )

        return world.getEntitiesByType(TypeFilter.instanceOf(PlayerEntity::class.java), box) { player: PlayerEntity? -> isPlayerViewing(player!!) }.size
    }
}