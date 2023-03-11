/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.server.battle

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.battles.ShowdownActionResponse
import com.cobblemon.mod.common.net.IntSize
import com.cobblemon.mod.common.util.readSizedInt
import com.cobblemon.mod.common.util.writeSizedInt
import java.util.UUID
import net.minecraft.network.PacketByteBuf
class BattleSelectActionsPacket() : NetworkPacket {
    lateinit var battleId: UUID
    lateinit var showdownActionResponses: List<ShowdownActionResponse>

    constructor(battleId: UUID, showdownActionResponses: List<ShowdownActionResponse>): this() {
        this.battleId = battleId
        this.showdownActionResponses = showdownActionResponses
    }

    override fun encode(buffer: PacketByteBuf) {
        buffer.writeUuid(battleId)
        buffer.writeSizedInt(IntSize.U_BYTE, showdownActionResponses.size)
        showdownActionResponses.forEach { it.saveToBuffer(buffer) }
    }

    override fun decode(buffer: PacketByteBuf) {
        battleId = buffer.readUuid()
        val responses = mutableListOf<ShowdownActionResponse>()
        repeat(times = buffer.readSizedInt(IntSize.U_BYTE)) {
            responses.add(ShowdownActionResponse.loadFromBuffer(buffer))
        }
        showdownActionResponses = responses
    }

}