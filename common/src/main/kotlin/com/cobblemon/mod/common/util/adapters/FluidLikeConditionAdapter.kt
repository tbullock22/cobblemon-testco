/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.adapters

import com.cobblemon.mod.common.api.conditional.RegistryLikeAdapter
import com.cobblemon.mod.common.api.conditional.RegistryLikeIdentifierCondition
import com.cobblemon.mod.common.api.conditional.RegistryLikeTagCondition
import com.cobblemon.mod.common.registry.FluidIdentifierCondition
import com.cobblemon.mod.common.registry.FluidTagCondition
import net.minecraft.fluid.Fluid
import net.minecraft.util.registry.Registry

object FluidLikeConditionAdapter : RegistryLikeAdapter<Fluid> {
    override val registryLikeConditions = mutableListOf(
        RegistryLikeTagCondition.resolver(Registry.FLUID_KEY, ::FluidTagCondition),
        RegistryLikeIdentifierCondition.resolver(::FluidIdentifierCondition)
    )
}

