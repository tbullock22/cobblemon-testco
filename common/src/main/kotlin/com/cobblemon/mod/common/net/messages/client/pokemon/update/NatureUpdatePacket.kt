/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.pokemon.update

import com.cobblemon.mod.common.Cobblemon.LOGGER
import com.cobblemon.mod.common.api.pokemon.Natures
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.util.Identifier
import net.minecraft.util.InvalidIdentifierException

class NatureUpdatePacket(
    private var mintNature : Boolean = false
) : StringUpdatePacket() {
    constructor(pokemon: Pokemon, value: String, mintNature: Boolean): this() {
        this.setTarget(pokemon)
        this.value = value
        this.mintNature = mintNature
    }

    override fun set(pokemon: Pokemon, value: String) {
        // Check for removing mint
        if (mintNature && value.isEmpty()) {
            pokemon.mintedNature = null
            return
        }

        try {
            val nature = Natures.getNature(Identifier(value))
            // Validate the nature locally
            if (nature == null) {
                LOGGER.warn("A invalid nature of '$value' was attempted to be put onto: '$pokemon'")
                return
            }

            // Check which nature to modify
            if (!mintNature) {
                pokemon.nature = nature
            } else {
                pokemon.mintedNature = nature
            }
        } catch (e: InvalidIdentifierException) {
            // This should never happen
            LOGGER.error("Failed to resolve nature value in NatureUpdatePacket", e)
        }
    }
}