/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.pokemon.update.evolution

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.pokemon.evolution.EvolutionDisplayEvent
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.pokemon.evolution.Evolution
import com.cobblemon.mod.common.api.pokemon.evolution.EvolutionDisplay
import com.cobblemon.mod.common.net.IntSize
import com.cobblemon.mod.common.net.messages.client.storage.EvolutionLikeUpdatePacket
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.evolution.CobblemonEvolutionDisplay
import com.cobblemon.mod.common.pokemon.evolution.variants.DummyEvolution
import com.cobblemon.mod.common.util.readSizedInt
import com.cobblemon.mod.common.util.writeSizedInt
import net.minecraft.network.PacketByteBuf

/**
 * The base for all [Evolution] updates.
 *
 * @author Licious
 * @since April 28th, 2022.
 */
abstract class EvolutionUpdatePacket : EvolutionLikeUpdatePacket<Evolution, EvolutionDisplay>() {

    override var current: Evolution = DummyEvolution()
    override var sending: EvolutionDisplay = CobblemonEvolutionDisplay("dummy", Pokemon())

    override fun createSending(pokemon: Pokemon): EvolutionDisplay {
        return Companion.createSending(pokemon, this.current)
    }

    final override fun encodeSending(buffer: PacketByteBuf) {
        Companion.encodeSending(this.sending, buffer)
    }

    final override fun decodeSending(buffer: PacketByteBuf) {
        this.sending = Companion.decodeSending(buffer)
    }

    companion object {

        // Hacks for DRY, see CobblemonServerEvolutionController for context

        internal fun createSending(pokemon: Pokemon, evolution: Evolution): EvolutionDisplay {
            val result = pokemon.clone()
            evolution.result.apply(result)
            val expectedDisplay = CobblemonEvolutionDisplay(evolution.id, result)
            val event = EvolutionDisplayEvent(result, expectedDisplay, evolution)
            CobblemonEvents.EVOLUTION_DISPLAY.post(event)
            return event.display
        }

        internal fun encodeSending(display: EvolutionDisplay, buffer: PacketByteBuf) {
            buffer.writeString(display.id)
            buffer.writeIdentifier(display.species.resourceIdentifier)
            buffer.writeSizedInt(IntSize.U_BYTE, display.aspects.size)
            display.aspects.forEach { aspect ->
                buffer.writeString(aspect)
            }
        }

        internal fun decodeSending(buffer: PacketByteBuf): EvolutionDisplay {
            val id = buffer.readString()
            val speciesIdentifier = buffer.readIdentifier()
            val species = PokemonSpecies.getByIdentifier(speciesIdentifier) ?: throw IllegalArgumentException("Cannot resolve species from $speciesIdentifier")
            val aspects = mutableSetOf<String>()
            repeat(buffer.readSizedInt(IntSize.U_BYTE)) {
                aspects += buffer.readString()
            }
            return CobblemonEvolutionDisplay(id, species, aspects)
        }

    }

}