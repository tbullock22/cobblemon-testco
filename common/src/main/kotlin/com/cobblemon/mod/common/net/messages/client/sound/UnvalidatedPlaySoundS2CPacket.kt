/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.sound

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket
import net.minecraft.sound.SoundCategory
import net.minecraft.util.Identifier

/**
 * A class meant to mimic [PlaySoundS2CPacket] without validating the Sound Event registry.
 * This should only be used for our dynamic sounds such as Pokémon ambience.
 *
 * @author Licious
 * @since December 29th, 2022
 */
internal class UnvalidatedPlaySoundS2CPacket constructor() : NetworkPacket {

    var sound: Identifier = cobblemonResource("dummy")
    var category: SoundCategory = SoundCategory.MASTER
    private var fixedX = 0
    private var fixedY = 0
    private var fixedZ = 0
    var volume = 0F
    var pitch = 0F

    val x get() = this.fixedX / 8.0
    val y get() = this.fixedY / 8.0
    val z get() = this.fixedZ / 8.0

    constructor(sound: Identifier, category: SoundCategory, x: Double, y: Double, z: Double, volume: Float, pitch: Float) : this() {
        this.sound = sound
        this.category = category
        this.fixedX = (x * 8.0).toInt()
        this.fixedY = (y * 8.0).toInt()
        this.fixedZ = (z * 8.0).toInt()
        this.volume = volume
        this.pitch = pitch
    }

    override fun encode(buffer: PacketByteBuf) {
        buffer.writeIdentifier(this.sound)
        buffer.writeEnumConstant(this.category)
        buffer.writeInt(this.fixedX)
        buffer.writeInt(this.fixedY)
        buffer.writeInt(this.fixedZ)
        buffer.writeFloat(this.volume)
        buffer.writeFloat(this.pitch)
    }

    override fun decode(buffer: PacketByteBuf) {
        this.sound = buffer.readIdentifier()
        this.category = buffer.readEnumConstant(SoundCategory::class.java)
        this.fixedX = buffer.readInt()
        this.fixedY = buffer.readInt()
        this.fixedZ = buffer.readInt()
        this.volume = buffer.readFloat()
        this.pitch = buffer.readFloat()
    }

}