/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.starter

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.config.starter.RenderableStarterCategory
import com.cobblemon.mod.common.config.starter.StarterCategory
import com.cobblemon.mod.common.pokemon.RenderablePokemon
import net.minecraft.network.PacketByteBuf
class OpenStarterUIPacket internal constructor() : NetworkPacket {

    constructor(categories: List<StarterCategory>) : this() {
        this.categories.addAll(categories.map { it.asRenderableStarterCategory() })
    }

    val categories = mutableListOf<RenderableStarterCategory>()

    override fun encode(buffer: PacketByteBuf) {
        buffer.writeInt(categories.size)
        categories.forEach {
            buffer.writeString(it.name)
            buffer.writeString(it.displayName)
            buffer.writeInt(it.pokemon.size)
            it.pokemon.forEach { it.saveToBuffer(buffer) }
        }
    }

    override fun decode(buffer: PacketByteBuf) {
        val numCategories = buffer.readInt()
        for (i in 0 until numCategories) {
            val name = buffer.readString()
            val displayName = buffer.readString()
            val numProperties = buffer.readInt()
            val renderablePokemon = mutableListOf<RenderablePokemon>()
            repeat(times = numProperties) {
                renderablePokemon.add(RenderablePokemon.loadFromBuffer(buffer))
            }
            categories.add(
                RenderableStarterCategory(
                    name = name,
                    displayName = displayName,
                    pokemon = renderablePokemon
                )
            )
        }
    }
}