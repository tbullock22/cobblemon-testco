/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.ai.goals

import com.cobblemon.mod.common.api.pokemon.status.Statuses
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.status.PersistentStatusContainer
import net.minecraft.block.BedBlock
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.tag.BlockTags
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box

/**
 * AI goal for sleeping on top of a player when they hop into a bed, like cats.
 *
 * @author Hiroku
 * @since July 18th, 2022
 */
class SleepOnTrainerGoal(private val pokemonEntity: PokemonEntity) : Goal() {
    private var owner: PlayerEntity? = null
    private var bedPos: BlockPos? = null
    private var ticksOnBed = 0

    override fun canStart(): Boolean {
        if (!pokemonEntity.pokemon.isPlayerOwned() || !pokemonEntity.behaviour.resting.willSleepOnBed || pokemonEntity.pokemon.status != null) {
            return false
        }
        val livingEntity = pokemonEntity.owner
        if (livingEntity is PlayerEntity) {
            owner = livingEntity
            if (!livingEntity.isSleeping()) {
                return false
            }
            if (pokemonEntity.squaredDistanceTo(owner) > 100.0) {
                return false
            }
            val blockPos = owner!!.blockPos
            val blockState = pokemonEntity.world.getBlockState(blockPos)
            if (blockState.isIn(BlockTags.BEDS)) {
                bedPos = blockState.getOrEmpty(BedBlock.FACING).orElse(null)
                    ?.let { direction -> blockPos.offset(direction.opposite) }
                    ?: BlockPos(blockPos)
                return !cannotSleep()
            }
        }

        return false
    }

    private fun cannotSleep(): Boolean {
        val closePokemon = pokemonEntity.world.getNonSpectatingEntities(PokemonEntity::class.java, Box(bedPos).expand(2.0))
        return closePokemon.any { it.pokemon.status?.status == Statuses.SLEEP && it != pokemonEntity }
    }

    override fun shouldContinue(): Boolean {
        val owner = owner
        return owner is ServerPlayerEntity && owner.isSleeping && bedPos != null && !cannotSleep()
    }

    override fun start() {
        if (bedPos != null) {
            pokemonEntity.navigation.startMovingTo(
                bedPos!!.x.toDouble(),
                bedPos!!.y.toDouble(),
                bedPos!!.z.toDouble(),
                0.7
            )
        }
    }

    override fun stop() {
        pokemonEntity.pokemon.status = null
//        val f = pokemonEntity.world.getSkyAngle(1.0f)
//        if (owner!!.sleepTimer >= 100 && f.toDouble() > 0.77 && f.toDouble() < 0.8 && pokemonEntity.world.getRandom().nextFloat() < 0.7) {
//            dropMorningGifts()
//        }
        ticksOnBed = 0
        pokemonEntity.navigation.stop()
    }

//    private fun dropMorningGifts() {
//        val random = cat.random
//        val mutable = BlockPos.Mutable()
//        mutable.set(cat.blockPos)
//        cat.teleport(
//            (mutable.x + random.nextInt(11) - 5).toDouble(),
//            (mutable.y + random.nextInt(5) - 2).toDouble(),
//            (mutable.z + random.nextInt(11) - 5).toDouble(),
//            false
//        )
//        mutable.set(cat.blockPos)
//        val lootTable = cat.world.server!!.lootManager.getTable(LootTables.CAT_MORNING_GIFT_GAMEPLAY)
//        val builder = LootContext.Builder(cat.world as ServerWorld)
//            .parameter(LootContextParameters.ORIGIN, cat.pos).parameter(
//                LootContextParameters.THIS_ENTITY,
//                cat
//            ).random(random)
//        val list = lootTable.generateLoot(builder.build(LootContextTypes.GIFT))
//        val var6: Iterator<*> = list.iterator()
//        while (var6.hasNext()) {
//            val itemStack = var6.next() as ItemStack
//            cat.world.spawnEntity(
//                ItemEntity(
//                    cat.world, mutable.x.toDouble() - MathHelper.sin(
//                        cat.bodyYaw * 0.017453292f
//                    ).toDouble(), mutable.y.toDouble(), mutable.z.toDouble() + MathHelper.cos(
//                        cat.bodyYaw * 0.017453292f
//                    ).toDouble(), itemStack
//                )
//            )
//        }
//    }

    override fun tick() {
        if (owner != null && bedPos != null) {
            if (pokemonEntity.squaredDistanceTo(owner) < 1.5) {
                ++ticksOnBed
                if (ticksOnBed > getTickCount(16)) {
                    pokemonEntity.pokemon.status = PersistentStatusContainer(Statuses.SLEEP)
                } else {
                    pokemonEntity.lookAtEntity(owner, 45.0f, 45.0f)
                }
            } else {
                pokemonEntity.pokemon.status = null
            }
        }
    }
}
