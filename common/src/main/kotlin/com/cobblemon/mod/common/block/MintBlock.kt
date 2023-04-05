/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block

import com.cobblemon.mod.common.mint.MintType
import dev.architectury.registry.registries.RegistrySupplier
import net.minecraft.block.*
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemConvertible
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.StateManager
import net.minecraft.state.property.IntProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.random.Random
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldView

class MintBlock(private val mintType: MintType, settings: Settings) : PlantBlock(settings), Fertilizable {

    companion object {
        val AGE: IntProperty = Properties.AGE_7
        const val MATURE_AGE = 7
        val AGE_TO_SHAPE = arrayOf(
            createCuboidShape(0.0, 0.0, 0.0, 16.0, 2.0, 16.0),
            createCuboidShape(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),
            createCuboidShape(0.0, 0.0, 0.0, 16.0, 6.0, 16.0),
            createCuboidShape(0.0, 0.0, 0.0, 16.0, 8.0, 16.0),
            createCuboidShape(0.0, 0.0, 0.0, 16.0, 10.0, 16.0),
            createCuboidShape(0.0, 0.0, 0.0, 16.0, 12.0, 16.0),
            createCuboidShape(0.0, 0.0, 0.0, 16.0, 14.0, 16.0),
            createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 16.0)
        )
    }

    init {
        defaultState = stateManager.defaultState.with(AGE, 0)
    }

    override fun getPickStack(world: BlockView, pos: BlockPos, state: BlockState): ItemStack {
        return ItemStack(mintType.getSeed())
    }

    private fun isMature(state: BlockState) = state.get(AGE) == MATURE_AGE

    override fun isFertilizable(world: BlockView, pos: BlockPos, state: BlockState, isClient: Boolean): Boolean {
        return !isMature(state)
    }

    override fun canGrow(world: World, random: Random, pos: BlockPos, state: BlockState): Boolean {
        return true
    }

    override fun grow(world: ServerWorld, random: Random, pos: BlockPos, state: BlockState) {
        val newAge = Integer.min(state.get(AGE) + 1, MATURE_AGE)
        world.setBlockState(pos, state.with(AGE, newAge), 2)
    }

    override fun getOutlineShape(state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext): VoxelShape {
        return AGE_TO_SHAPE[state.get(AGE)]
    }

    override fun hasRandomTicks(state: BlockState): Boolean {
        return !isMature(state)
    }

    override fun randomTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
        if (world.getBaseLightLevel(pos, 0) >= 0) {
            if (isMature(state)) return
            if (random.nextInt(25) != 0) return

            val currentAge = state.get(AGE)
            world.setBlockState(pos, defaultState.with(AGE, currentAge + 1), 2)
        }
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(AGE)
    }

}