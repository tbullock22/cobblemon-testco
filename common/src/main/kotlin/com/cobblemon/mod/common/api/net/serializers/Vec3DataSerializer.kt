/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.net.serializers

import net.minecraft.entity.data.TrackedDataHandler
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.math.Vec3d

object Vec3DataSerializer : TrackedDataHandler<Vec3d> {
    override fun write(buffer: PacketByteBuf, vec: Vec3d) {
        buffer.writeDouble(vec.x)
        buffer.writeDouble(vec.y)
        buffer.writeDouble(vec.z)
    }

    override fun read(buffer: PacketByteBuf) = Vec3d(
        buffer.readDouble(),
        buffer.readDouble(),
        buffer.readDouble()
    )

    override fun copy(vec: Vec3d) = Vec3d(vec.x, vec.y, vec.z)
}