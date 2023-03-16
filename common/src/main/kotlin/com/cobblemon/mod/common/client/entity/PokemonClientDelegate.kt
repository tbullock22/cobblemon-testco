/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.entity

import com.cobblemon.mod.common.api.entity.PokemonSideDelegate
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.scheduling.after
import com.cobblemon.mod.common.api.scheduling.lerp
import com.cobblemon.mod.common.client.render.models.blockbench.PoseableEntityState
import com.cobblemon.mod.common.client.render.models.blockbench.additives.EarBounceAdditive
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import java.lang.Float.min
import kotlin.math.abs
import net.minecraft.entity.Entity
import net.minecraft.util.Identifier
class PokemonClientDelegate : PoseableEntityState<PokemonEntity>(), PokemonSideDelegate {
    companion object {
        const val BEAM_SHRINK_TIME = 0.8F
        const val BEAM_EXTEND_TIME = 0.2F
    }

    lateinit var entity: PokemonEntity
    var phaseTarget: Entity? = null
    var entityScaleModifier = 1F

    var animTick = 0F
    var previousVerticalVelocity = 0F

    var beamStartTime = System.currentTimeMillis()

    val secondsSinceBeamEffectStarted: Float
        get() = (System.currentTimeMillis() - beamStartTime) / 1000F

    private val minimumFallSpeed = -0.1F
    private val intensityVelocityCap = -0.5F
    override fun changePokemon(pokemon: Pokemon) {
        pokemon.isClient = true
        entity.subscriptions.add(entity.species.subscribeIncludingCurrent {
            currentPose = null
            entity.pokemon.species = PokemonSpecies.getByIdentifier(Identifier(it))!! // TODO exception handling
        })

        entity.subscriptions.add(entity.deathEffectsStarted.subscribe {
            if (it) {
                val model = (currentModel ?: return@subscribe) as PokemonPoseableModel
                val animation = model.getFaintAnimation(entity, this) ?: return@subscribe
                statefulAnimations.add(animation)
            }
        })

        entity.subscriptions.add(entity.labelLevel.subscribeIncludingCurrent { if (it > 0) entity.pokemon.level = it })

        entity.subscriptions.add(entity.phasingTargetId.subscribe {
            if (it != -1) {
                setPhaseTarget(it)
            } else {
                phaseTarget = null
            }
        })

//        pokemon.aspects = entity.aspects.get()
//        entity.aspects.pipe(emitWhile { pokemon == entity.pokemon }).subscribe {
//            pokemon.aspects = it
//        }

        entity.subscriptions.add(entity.beamModeEmitter.subscribeIncludingCurrent {
            if (it == 0.toByte()) {
                // Do nothing
            } else if (it == 1.toByte()) {
                // Scaling up out of pokeball
                entityScaleModifier = 0F
                beamStartTime = System.currentTimeMillis()
                entity.isInvisible = true
                after(seconds = BEAM_EXTEND_TIME) {
                    lerp(BEAM_SHRINK_TIME) { entityScaleModifier = it }
                    entity.isInvisible = false
                }
            } else {
                // Scaling down into pokeball
                entityScaleModifier = 1F
                beamStartTime = System.currentTimeMillis()
                after(seconds = BEAM_EXTEND_TIME) {
                    lerp(BEAM_SHRINK_TIME) {
                        entityScaleModifier = (1 - it)
                    }
                }
            }
        })
    }

    override fun initialize(entity: PokemonEntity) {
        this.entity = entity
    }

    override fun tick(entity: PokemonEntity) {
        val downSpeed = entity.velocity.y
        if (downSpeed > previousVerticalVelocity && downSpeed > minimumFallSpeed) {
            // Stopped falling
            val highestFallVelocity = previousVerticalVelocity
            if (abs(highestFallVelocity) > abs(minimumFallSpeed)) {
                val intensity = abs(min(highestFallVelocity / intensityVelocityCap, 1F))
                if (additives.none { it is EarBounceAdditive }) {
                    additives.add(EarBounceAdditive(intensity, 18))
                }
            }
        }

        previousVerticalVelocity = entity.velocity.y.toFloat()
    }

    fun setPhaseTarget(targetId: Int) {
        this.phaseTarget = entity.world.getEntityById(targetId)
    }

    override fun handleStatus(status: Byte) {
        if (status == 10.toByte()) {
            val model = (currentModel ?: return) as PokemonPoseableModel
            val animation = model.getEatAnimation(entity, this) ?: return
            statefulAnimations.add(animation)
        }
    }
}