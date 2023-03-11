/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.battles.actor

import com.cobblemon.mod.common.api.battles.model.actor.AIBattleActor
import com.cobblemon.mod.common.api.battles.model.actor.ActorType
import com.cobblemon.mod.common.api.battles.model.actor.EntityBackedBattleActor
import com.cobblemon.mod.common.api.battles.model.actor.FleeableBattleActor
import com.cobblemon.mod.common.api.battles.model.ai.BattleAI
import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.battles.ai.RandomBattleAI
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.client.battle.BattleEndPacket
import java.util.Optional
import java.util.UUID
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.math.Vec3d

open class PokemonBattleActor(
    uuid: UUID,
    val pokemon: BattlePokemon,
    override val fleeDistance: Float,
    artificialDecider: BattleAI = RandomBattleAI()
) : AIBattleActor(uuid, listOf(pokemon), artificialDecider), EntityBackedBattleActor<PokemonEntity>, FleeableBattleActor {
    override fun getName() = pokemon.effectedPokemon.species.translatedName
    override fun nameOwned(name: String): MutableText = Text.literal(name)
    override fun getWorldAndPosition(): Pair<ServerWorld, Vec3d>? {
        // This isn't a great solution, but basically capturing a Pokémon
        // removes the entity from the world, which sure does look similar
        // to an entity perishing -> which is grounds for flee triggering.
        val ownerPlayer = pokemon.effectedPokemon.getOwnerPlayer()
        if (ownerPlayer != null) {
            return ownerPlayer.getWorld() to ownerPlayer.pos
        }

        val entity = this.entity ?: return null
        val world = entity.world as? ServerWorld ?: return null
        return world to entity.pos
    }

    override fun sendUpdate(packet: NetworkPacket) {
        super.sendUpdate(packet)
        if (packet is BattleEndPacket) {
            // Do some shit
            val entity = entity ?: return
            entity.battleId.set(Optional.empty())
        }
    }

    override val entity: PokemonEntity?
        get() = pokemon.entity

    override val type = ActorType.WILD
}