/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.data

import com.cobblemon.mod.common.api.moves.MoveTemplate
import com.cobblemon.mod.common.api.moves.Moves
import com.cobblemon.mod.common.api.moves.categories.DamageCategories
import com.cobblemon.mod.common.api.types.ElementalTypes
import com.cobblemon.mod.common.battles.MoveTarget
import com.cobblemon.mod.common.net.IntSize
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readSizedInt
import com.cobblemon.mod.common.util.writeSizedInt
import net.minecraft.network.PacketByteBuf

class MovesRegistrySyncPacket : DataRegistrySyncPacket<MoveTemplate> {
    constructor(): super(emptyList())
    constructor(moves: List<MoveTemplate>): super(moves)

    override fun encodeEntry(buffer: PacketByteBuf, entry: MoveTemplate) {
        buffer.writeSizedInt(IntSize.U_SHORT, entry.id)
        buffer.writeString(entry.name)
        buffer.writeString(entry.elementalType.name)
        buffer.writeString(entry.damageCategory.name)
        buffer.writeDouble(entry.power)
        buffer.writeEnumConstant(entry.target)
        buffer.writeDouble(entry.accuracy)
        buffer.writeInt(entry.pp)
        buffer.writeInt(entry.priority)
        buffer.writeDouble(entry.critRatio)
        buffer.writeVarInt(entry.effectChances.size)
        entry.effectChances.forEach { chance -> buffer.writeDouble(chance) }
    }

    override fun decodeEntry(buffer: PacketByteBuf): MoveTemplate {
        val id = buffer.readSizedInt(IntSize.U_SHORT)
        val name = buffer.readString()
        val type = ElementalTypes.getOrException(buffer.readString())
        val damageCategory = DamageCategories.getOrException(buffer.readString())
        val power = buffer.readDouble()
        val target = buffer.readEnumConstant(MoveTarget::class.java)
        val accuracy = buffer.readDouble()
        val pp = buffer.readInt()
        val priority = buffer.readInt()
        val critRatio = buffer.readDouble()
        val effectChances = arrayListOf<Double>()
        repeat(buffer.readVarInt()) {
            effectChances += buffer.readDouble()
        }
        return MoveTemplate(name, type, damageCategory, power, target, accuracy, pp, priority, critRatio, effectChances.toTypedArray()).apply { this.id = id }
    }

    override fun synchronizeDecoded(entries: Collection<MoveTemplate>) {
        Moves.receiveSyncPacket(entries)
    }
}