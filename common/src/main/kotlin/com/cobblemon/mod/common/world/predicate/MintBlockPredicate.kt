package com.cobblemon.mod.common.world.predicate

import com.cobblemon.mod.common.util.cobblemonResource
import com.mojang.serialization.Codec
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry
import net.minecraft.world.StructureWorldAccess
import net.minecraft.world.gen.blockpredicate.BlockPredicate
import net.minecraft.world.gen.blockpredicate.BlockPredicateType

class MintBlockPredicate : BlockPredicate {
    override fun test(world: StructureWorldAccess, block: BlockPos): Boolean {
        return block.y >= 80
    }

    override fun getType(): BlockPredicateType<MintBlockPredicate> {
        return TYPE
    }

    companion object {
        val INSTANCE = MintBlockPredicate()
        val CODEC : Codec<MintBlockPredicate> = Codec.unit { INSTANCE }
        val TYPE : BlockPredicateType<MintBlockPredicate> = register("mint_block", CODEC)

        fun <P : BlockPredicate?> register(id: String, codec: Codec<P>): BlockPredicateType<P> {
            return Registry.register(Registry.BLOCK_PREDICATE_TYPE, id, BlockPredicateType { codec })
        }
    }
}