/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.net

import net.minecraft.network.PacketByteBuf

/**
 * Represents an object that can be encoded to a [PacketByteBuf].
 *
 * @author Licious
 * @since October 14th, 2022
 */
interface Encodable {

    /**
     * Writes this instance to the given buffer.
     *
     * @param buffer The [PacketByteBuf] being written to.
     */
    fun encode(buffer: PacketByteBuf)

}