/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.storage

import com.cobblemon.mod.common.api.pokemon.evolution.EvolutionDisplay
import com.cobblemon.mod.common.api.pokemon.evolution.EvolutionLike
import com.cobblemon.mod.common.net.messages.client.PokemonUpdatePacket
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.network.PacketByteBuf

/**
 * The base of all evolution related updates.
 *
 * @param C The type of [EvolutionLike] on the current side.
 * @param S The type of [EvolutionLike] being sent to the other side.
 *
 * @author Licious
 * @since April 28th, 2022.
 */
abstract class EvolutionLikeUpdatePacket<C : EvolutionLike, S : EvolutionLike> : PokemonUpdatePacket() {

    abstract var current: C
    abstract var sending: S

    /**
     * Creates the [S] expected from the [C].
     *
     * @param pokemon The [Pokemon] being affected.
     * @return The resulting [EvolutionDisplay].
     */
    protected abstract fun createSending(pokemon: Pokemon): S

    final override fun encode(buffer: PacketByteBuf) {
        super.encode(buffer)
        this.encodeSending(buffer)
    }

    final override fun decode(buffer: PacketByteBuf) {
        super.decode(buffer)
        this.decodeSending(buffer)
    }

    abstract fun encodeSending(buffer: PacketByteBuf)

    abstract fun decodeSending(buffer: PacketByteBuf)

}