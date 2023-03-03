package com.cobblemon.mod.common.block

import dev.architectury.registry.registries.RegistrySupplier
import net.minecraft.block.BlockState
import net.minecraft.block.CropBlock
import net.minecraft.block.ShapeContext
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemConvertible
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView

class MintBlock(val seed: RegistrySupplier<BlockItem>, settings: Settings) : CropBlock(settings) {

    private val AGE_TO_SHAPE = arrayOf(
        createCuboidShape(0.0, 0.0, 0.0, 16.0, 2.0, 16.0),
        createCuboidShape(0.0, 0.0, 0.0, 16.0, 3.0, 16.0),
        createCuboidShape(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),
        createCuboidShape(0.0, 0.0, 0.0, 16.0, 5.0, 16.0),
        createCuboidShape(0.0, 0.0, 0.0, 16.0, 6.0, 16.0),
        createCuboidShape(0.0, 0.0, 0.0, 16.0, 7.0, 16.0),
        createCuboidShape(0.0, 0.0, 0.0, 16.0, 8.0, 16.0),
        createCuboidShape(0.0, 0.0, 0.0, 16.0, 9.0, 16.0)
    )

    override fun getSeedsItem(): ItemConvertible {
        return seed.get()
    }

    override fun getOutlineShape(
        state: BlockState?,
        world: BlockView?,
        pos: BlockPos?,
        context: ShapeContext?
    ): VoxelShape {
        return AGE_TO_SHAPE[(state!!.get(this.ageProperty) as Int)]
    }
}