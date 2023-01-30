/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.pokemon.update

import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf

class HeldItemUpdatePacket internal constructor() : SingleUpdatePacket<ItemStack>(ItemStack.EMPTY) {

    constructor(pokemon: Pokemon, value: ItemStack) : this() {
        setTarget(pokemon)
        this.value = value
    }

    override fun encodeValue(buffer: PacketByteBuf, value: ItemStack) {
        buffer.writeItemStack(value)
    }

    override fun decodeValue(buffer: PacketByteBuf): ItemStack = buffer.readItemStack()

    override fun set(pokemon: Pokemon, value: ItemStack) {
        pokemon.swapHeldItem(value, false)
    }

}