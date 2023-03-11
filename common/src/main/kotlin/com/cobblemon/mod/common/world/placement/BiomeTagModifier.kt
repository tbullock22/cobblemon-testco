/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.world.placement

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.tag.TagKey
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.random.Random
import net.minecraft.util.registry.Registry
import net.minecraft.world.biome.Biome
import net.minecraft.world.gen.feature.FeaturePlacementContext
import net.minecraft.world.gen.placementmodifier.AbstractConditionalPlacementModifier
import net.minecraft.world.gen.placementmodifier.PlacementModifierType

/**
 * A world generation placement filter which makes placing conditional on
 * the biome being a part of the given [TagKey].
 *
 * Largely helped by BYG code which had the same idea as us but actually
 * knew how to do it and saved some time.
 *
 * @author Hiroku
 * @since March 25th, 2022
 */
class BiomeTagModifier(private val tag: TagKey<Biome>) : AbstractConditionalPlacementModifier() {
    override fun getType(): PlacementModifierType<*> = CobblemonPlacementTypes.BIOME_TAG_FILTER
    override fun shouldPlace(ctx: FeaturePlacementContext, r: Random, pos: BlockPos) = ctx.world.getBiome(pos).isIn(tag)

    companion object {
        val CODEC: Codec<BiomeTagModifier> = RecordCodecBuilder.create { builder ->
            builder
                .group(
                    TagKey.unprefixedCodec(Registry.BIOME_KEY)
                        .fieldOf("tag")
                        .forGetter { it.tag }
                )
                .apply(builder) { BiomeTagModifier(it) }
        }
    }
}