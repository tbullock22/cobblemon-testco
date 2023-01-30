/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.battles

import com.cobblemon.mod.common.net.IntSize
import com.cobblemon.mod.common.util.readSizedInt
import com.cobblemon.mod.common.util.writeSizedInt
import net.minecraft.network.PacketByteBuf
class InBattleMove {
    lateinit var id: String
    lateinit var move: String
    var pp: Int = 100
    var maxpp: Int = 100
    var target: MoveTarget = MoveTarget.self
    var disabled: Boolean = false

    companion object {
        fun loadFromBuffer(buffer: PacketByteBuf): InBattleMove {
            return InBattleMove().apply {
                id = buffer.readString()
                move = buffer.readString()
                pp = buffer.readSizedInt(IntSize.U_BYTE)
                maxpp = buffer.readSizedInt(IntSize.U_BYTE)
                target = buffer.readEnumConstant(MoveTarget::class.java)
                disabled = buffer.readBoolean()
            }
        }
    }

    fun getTargets(user: ActiveBattlePokemon) = target.targetList(user)
    fun canBeUsed() = (pp > 0 && !disabled) || mustBeUsed() // Second case is like Thrash, forced choice
    fun mustBeUsed() = maxpp == 100 && pp == 100 && target == MoveTarget.self
    fun saveToBuffer(buffer: PacketByteBuf) {
        buffer.writeString(id)
        buffer.writeString(move)
        buffer.writeSizedInt(IntSize.U_BYTE, pp)
        buffer.writeSizedInt(IntSize.U_BYTE, maxpp)
        buffer.writeEnumConstant(target)
        buffer.writeBoolean(disabled)
    }
}