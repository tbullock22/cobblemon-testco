/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.evolution.requirements

import com.cobblemon.mod.common.api.conditional.RegistryLikeCondition
import com.cobblemon.mod.common.api.pokemon.evolution.requirement.EvolutionRequirement
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.registry.ItemIdentifierCondition
import net.minecraft.item.Item
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

/**
 * An [EvolutionRequirement] for a [Pokemon.heldItem].
 *
 * @property itemCondition The [RegistryLikeCondition] of [Item] expected to match the [Pokemon.heldItem].
 * @author Licious
 * @since March 21st, 2022
 */
class HeldItemRequirement(itemCondition: RegistryLikeCondition<Item>) : EvolutionRequirement {

    constructor() : this(ItemIdentifierCondition(Identifier("air")))

    val itemCondition: RegistryLikeCondition<Item> = itemCondition

    override fun check(pokemon: Pokemon): Boolean = this.itemCondition.fits(pokemon.heldItemNoCopy().item, Registry.ITEM)

    companion object {
        const val ADAPTER_VARIANT = "held_item"
    }
}