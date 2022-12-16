/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.context

import com.cobblemon.mod.common.api.spawning.SpawnCause
import com.cobblemon.mod.common.api.spawning.condition.BasicSpawningCondition
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence
import com.cobblemon.mod.common.api.spawning.spawner.Spawner
import net.minecraft.block.Block
import net.minecraft.entity.Entity
import net.minecraft.fluid.Fluid
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import net.minecraft.world.biome.Biome

/**
 * A context upon which spawning is being attempted. This supplies all the information that can be used to asynchronously
 * check for suitability of spawning.
 *
 * @author Hiroku
 * @since January 24th, 2022
 */
abstract class SpawningContext {
    companion object {
        val contexts = mutableListOf<RegisteredSpawningContext<*>>()
        fun getByName(name: String) = contexts.find { it.name == name }
        fun getByClass(ctx: SpawningContext) = contexts.find { it.clazz == ctx::class.java }
        fun <T : SpawningContext> register(name: String, clazz: Class<T>, defaultCondition: String = BasicSpawningCondition.NAME) {
            contexts.add(
                RegisteredSpawningContext(
                    name = name,
                    clazz = clazz,
                    defaultCondition = defaultCondition
                )
            )
        }
    }

    /** What caused the spawn context, as a [SpawnCause]. */
    abstract val cause: SpawnCause
    val spawner: Spawner
        get() = cause.spawner
    /** The [World] the spawning context exists in. */
    abstract val world: World
    /** The location of the spawning attempt. */
    abstract val position: BlockPos
    /** The light level at this location. */
    abstract val light: Int
    /** Whether or not the sky is visible at this location. */
    abstract val canSeeSky: Boolean
    /** A list of [SpawningInfluence]s that apply due to this specific context. */
    abstract val influences: MutableList<SpawningInfluence>
    /** The current phase of the moon at this location. */
    val moonPhase: Int by lazy { world.moonPhase }
    /** The biome of this location. */
    val biome: Biome by lazy { world.getBiome(position).value() }

    val biomeRegistry: Registry<Biome> by lazy { world.registryManager.get(Registry.BIOME_KEY) }
    val blockRegistry: Registry<Block> by lazy { world.registryManager.get(Registry.BLOCK_KEY) }
    val fluidRegistry: Registry<Fluid> by lazy { world.registryManager.get(Registry.FLUID_KEY)}

    val biomeName: Identifier
        get() = this.biomeRegistry.getId(biome)!!

    /**
     * Filters a spawning detail by some extra condition defined by the context itself. This is for API purposes.
     * @return true if the [SpawnDetail] is acceptable by the context's own logic.
     */
    open fun preFilter(detail: SpawnDetail): Boolean {
        /** Returns true if none of the influences.affectSpawnable return false */
        return influences.none { !it.affectSpawnable(detail, this) }
    }
    open fun afterSpawn(entity: Entity) {
        influences.forEach { it.affectSpawn(entity) }
    }

    open fun getWeight(detail: SpawnDetail): Float {
        var weight = detail.weight
        for (influence in influences + detail.weightMultipliers) {
            weight = influence.affectWeight(detail, this, weight)
        }
        return weight
    }
}