/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.battle

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.net.IntSize
import com.cobblemon.mod.common.net.messages.PokemonDTO
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.readSizedInt
import com.cobblemon.mod.common.util.writeSizedInt
import net.minecraft.network.PacketByteBuf


/**
 * Gives the client the true details of their team in the battle. This is so that switch choices can be made with
 * full details.
 *
 * Handled by [com.cobblemon.mod.common.client.net.battle.BattleSetTeamPokemonHandler].
 *
 * @author Hiroku
 * @since June 6th, 2022
 */
class BattleSetTeamPokemonPacket() : NetworkPacket {
    val team = mutableListOf<PokemonDTO>()

    constructor(team: List<Pokemon>): this() {
        this.team.addAll(team.map { PokemonDTO(it, toClient = true) })
    }

    override fun encode(buffer: PacketByteBuf) {
        buffer.writeSizedInt(IntSize.U_BYTE, team.size)
        for (pokemon in team) {
            pokemon.encode(buffer)
        }
    }

    override fun decode(buffer: PacketByteBuf) {
        repeat(times = buffer.readSizedInt(IntSize.U_BYTE)) {
            team.add(PokemonDTO().also { it.decode(buffer) })
        }
    }
}