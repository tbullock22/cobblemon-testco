/*
 * Copyright (C) 2022 Pokemon Cobbled Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cablemc.pokemoncobbled.common.pokemon.effects

import com.cablemc.pokemoncobbled.common.api.pokemon.effect.ShoulderEffect
import com.cablemc.pokemoncobbled.common.pokemon.Pokemon
import com.google.gson.JsonObject
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.server.network.ServerPlayerEntity
import java.util.*

/**
 * Effect that allows for slow falling after [slowAfter] blocks.
 * The value for [slowAfter] can be set per Form in the Species JSON.
 *
 * @author Qu
 * @since 2022-01-29
 */
class SlowFallEffect: ShoulderEffect {
    companion object {
        init {
            //MinecraftForge.EVENT_BUS.register(this)
        }
        private val SLOW_FALLING_ID = UUID.fromString("A5B6CF2A-2F7C-31EF-9022-7C3E7D5E6ABB")
        private val SLOW_FALLING = EntityAttributeModifier(
            SLOW_FALLING_ID,
            "Slow falling acceleration reduction",
            -0.07,
            EntityAttributeModifier.Operation.ADDITION
        ) // Add -0.07 to 0.08 so we get the vanilla default of 0.01

        private const val SLOW_AFTER_PROPERTY = "slowAfter"
        private val observeMap = mutableMapOf<ServerPlayerEntity, SlowFallEffect>()

//        @SubscribeEvent
//        fun onLivingUpdate(event: LivingEvent.LivingUpdateEvent) {
//            if (event.entity !is ServerPlayerEntity) return
//            val player = event.entity as ServerPlayerEntity
//            if (player !in observeMap) return
//
//            if (!player.gameMode.isSurvival) return
//
//            if (player.fallDistance > 0) observeMap[player]?.onFall(player)
//        }
//
//        @SubscribeEvent
//        fun onFallEnd(event: LivingFallEvent) {
//            if (event.entity !is ServerPlayerEntity) return
//            val player = event.entity as ServerPlayerEntity
//            if (player !in observeMap) return
//
//            event.damageMultiplier = 0.0F
//            observeMap[player]?.onFallEnd(player)
//        }
    }

    /**
     * Amount of Blocks the [ServerPlayerEntity] needs to fall to trigger the [SlowFallEffect]
     */
    private var slowAfter = 5

    override fun applyEffect(pokemon: Pokemon, player: ServerPlayerEntity, isLeft: Boolean) {
        observeMap[player] = this
    }

    override fun removeEffect(pokemon: Pokemon, player: ServerPlayerEntity, isLeft: Boolean) {
        observeMap.remove(player)
        removeEffect(player)
    }

    /**
     * Triggers if the [ServerPlayerEntity] is falling
     */
    fun onFall(player: ServerPlayerEntity) {
        if (player.fallDistance >= slowAfter) {
            addEffect(player)
        }
    }

    /**
     * Triggers when the [ServerPlayerEntity] finished falling
     */
    fun onFallEnd(player: ServerPlayerEntity) {
        player.fallDistance = 0F
        removeEffect(player)
    }

    private fun addEffect(player: ServerPlayerEntity) {
//        player.getAttribute(ForgeMod.ENTITY_GRAVITY.get())?.let {
//            if (!it.hasModifier(SLOW_FALLING)) it.addTransientModifier(SLOW_FALLING)
//        }
    }

    private fun removeEffect(player: ServerPlayerEntity) {
//        player.getAttribute(ForgeMod.ENTITY_GRAVITY.get())?.let {
//            if (it.hasModifier(SLOW_FALLING)) it.removeModifier(SLOW_FALLING)
//        }
    }

    override fun serialize(json: JsonObject): JsonObject {
        json.addProperty(SLOW_AFTER_PROPERTY, slowAfter)
        return json
    }

    override fun deserialize(json: JsonObject): ShoulderEffect {
        slowAfter = json.get(SLOW_AFTER_PROPERTY).asInt
        return this
    }
}