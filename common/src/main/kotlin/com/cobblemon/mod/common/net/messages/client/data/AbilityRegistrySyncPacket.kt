/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.data

import com.cobblemon.mod.common.api.abilities.Abilities
import com.cobblemon.mod.common.api.abilities.AbilityTemplate
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.PacketByteBuf

class AbilityRegistrySyncPacket : DataRegistrySyncPacket<AbilityTemplate> {
    constructor(): super(emptyList())
    constructor(abilities: Collection<AbilityTemplate>): super(abilities)

    override fun encodeEntry(buffer: PacketByteBuf, entry: AbilityTemplate) {
        buffer.writeString(entry.name)
        buffer.writeString(entry.displayName)
        buffer.writeString(entry.description)
    }

    override fun decodeEntry(buffer: PacketByteBuf): AbilityTemplate {
        return AbilityTemplate(
            name = buffer.readString(),
            displayName = buffer.readString(),
            description = buffer.readString()
        )
    }

    override fun synchronizeDecoded(entries: Collection<AbilityTemplate>) {
        Abilities.receiveSyncPacket(entries)
    }
}