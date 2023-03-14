/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.spawn

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.FormData
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.entity.Entity
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.util.Identifier
import java.util.*

class SpawnPokemonPacket(
    private val uuid: UUID,
    private val scaleModifier: Float,
    private val species: Species,
    private val form: FormData,
    private val aspects: Set<String>,
    private val phasingTargetId: Int,
    private val beamModeEmitter: Byte,
    private val labelLevel: Int,
    vanillaSpawnPacket: EntitySpawnS2CPacket
) : SpawnExtraDataEntityPacket<SpawnPokemonPacket, PokemonEntity>(vanillaSpawnPacket) {

    override val id: Identifier = ID

    constructor(entity: PokemonEntity, vanillaSpawnPacket: EntitySpawnS2CPacket) : this(
        entity.pokemon.uuid,
        entity.pokemon.scaleModifier,
        entity.pokemon.species,
        entity.pokemon.form,
        entity.pokemon.aspects,
        entity.phasingTargetId.get(),
        entity.beamModeEmitter.get(),
        if (Cobblemon.config.displayEntityLevelLabel) entity.labelLevel.get() else -1,
        vanillaSpawnPacket
    )

    override fun encodeEntityData(buffer: PacketByteBuf) {
        buffer.writeUuid(this.uuid)
        buffer.writeFloat(this.scaleModifier)
        buffer.writeIdentifier(this.species.resourceIdentifier)
        buffer.writeString(this.form.formOnlyShowdownId())
        buffer.writeCollection(this.aspects) { pb, value -> pb.writeString(value) }
        buffer.writeInt(this.phasingTargetId)
        buffer.writeByte(this.beamModeEmitter.toInt())
        buffer.writeInt(this.labelLevel)
    }

    override fun applyData(entity: PokemonEntity) {
        entity.pokemon.apply {
            uuid = this@SpawnPokemonPacket.uuid
            scaleModifier = this@SpawnPokemonPacket.scaleModifier
            species = this@SpawnPokemonPacket.species
            form = this@SpawnPokemonPacket.form
            aspects = this@SpawnPokemonPacket.aspects
        }
        entity.phasingTargetId.set(this.phasingTargetId)
        entity.beamModeEmitter.set(this.beamModeEmitter)
        entity.labelLevel.set(this.labelLevel)
        entity.species.set(entity.pokemon.species.resourceIdentifier.toString())
        entity.aspects.set(aspects)
    }

    override fun checkType(entity: Entity): Boolean = entity is PokemonEntity

    companion object {
        val ID = cobblemonResource("spawn_pokemon_entity")
        fun decode(buffer: PacketByteBuf): SpawnPokemonPacket {
            val uuid = buffer.readUuid()
            val scaleModifier = buffer.readFloat()
            val species = PokemonSpecies.getByIdentifier(buffer.readIdentifier())!!
            val showdownId = buffer.readString()
            val form = species.forms.firstOrNull { it.formOnlyShowdownId() == showdownId } ?: species.standardForm
            val aspects = buffer.readList(PacketByteBuf::readString).toSet()
            val phasingTargetId = buffer.readInt()
            val beamModeEmitter = buffer.readByte()
            val labelLevel = buffer.readInt()
            val vanillaPacket = decodeVanillaPacket(buffer)
            return SpawnPokemonPacket(uuid, scaleModifier, species, form, aspects, phasingTargetId, beamModeEmitter, labelLevel, vanillaPacket)
        }
    }

}