/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.pokemon.update

import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.net.IntSize
import com.cobblemon.mod.common.pokemon.EVs
import com.cobblemon.mod.common.pokemon.IVs
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.PokemonStats
import com.cobblemon.mod.common.util.readSizedInt
import com.cobblemon.mod.common.util.writeSizedInt
import net.minecraft.network.PacketByteBuf

/**
 * Base packet class for things extending [PokemonStats].
 *
 * @author Hiroku
 * @since November 23rd, 2022
 */
abstract class StatsUpdatePacket<T : PokemonStats>(value: T) : SingleUpdatePacket<T>(value) {
    override fun encodeValue(buffer: PacketByteBuf, value: T) {
        for (stat in Stats.PERMANENT.filterIsInstance<Stats>()) {
            buffer.writeSizedInt(IntSize.U_BYTE, value[stat] ?: 0)
        }
    }

    override fun decodeValue(buffer: PacketByteBuf): T {
        for (stat in Stats.PERMANENT.filterIsInstance<Stats>()) {
            value[stat] = buffer.readSizedInt(IntSize.U_BYTE)
        }
        return value
    }

    abstract fun getStatContainer(pokemon: Pokemon): T

    override fun set(pokemon: Pokemon, value: T) {
        val stats = getStatContainer(pokemon)
        for ((stat, ev) in value) {
            stats[stat] = ev
        }
    }
}

/**
 * Packet used for when EVs have changed.
 *
 * @author Hiroku
 * @since November 23rd, 2022
 */
class EVsUpdatePacket() : StatsUpdatePacket<EVs>(EVs()) {
    override fun getStatContainer(pokemon: Pokemon) = pokemon.evs
    constructor(pokemon: Pokemon, evs: EVs) : this() {
        this.setTarget(pokemon)
        this.value = evs
    }
}

/**
 * Packet used for when IVs have changed.
 *
 * @author Hiroku
 * @since November 23rd, 2022
 */
class IVsUpdatePacket() : StatsUpdatePacket<IVs>(IVs()) {
    override fun getStatContainer(pokemon: Pokemon) = pokemon.ivs
    constructor(pokemon: Pokemon, ivs: IVs) : this() {
        this.setTarget(pokemon)
        this.value = ivs
    }
}