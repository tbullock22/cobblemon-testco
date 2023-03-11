/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.net.serializers

import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.entity.data.TrackedDataHandler
import net.minecraft.network.PacketByteBuf

object PoseTypeDataSerializer : TrackedDataHandler<PoseType> {
    override fun read(buf: PacketByteBuf) = PoseType.values()[buf.readInt()]
    override fun copy(value: PoseType) = value
    override fun write(buf: PacketByteBuf, value: PoseType) {
        buf.writeInt(value.ordinal)
    }
}