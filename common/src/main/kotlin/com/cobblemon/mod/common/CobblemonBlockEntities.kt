/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common

import com.cobblemon.mod.common.registry.CompletableRegistry
import com.cobblemon.mod.common.block.entity.HealingMachineBlockEntity
import com.cobblemon.mod.common.block.entity.PCBlockEntity
import dev.architectury.registry.registries.RegistrySupplier
import java.util.function.Supplier
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.util.registry.Registry

object CobblemonBlockEntities : CompletableRegistry<BlockEntityType<*>>(Registry.BLOCK_ENTITY_TYPE_KEY) {
    private fun <T : BlockEntityType<*>> register(name: String, blockEntityType: Supplier<T>) : RegistrySupplier<T> {
        return queue(name, blockEntityType)
    }

    @JvmField
    val HEALING_MACHINE = com.cobblemon.mod.common.CobblemonBlockEntities.register("healing_machine") {
        BlockEntityType.Builder.create(
            ::HealingMachineBlockEntity,
            CobblemonBlocks.HEALING_MACHINE.get()
        ).build(null)
    }
    @JvmField
    val PC = com.cobblemon.mod.common.CobblemonBlockEntities.register("pc") {
        BlockEntityType.Builder.create(
            ::PCBlockEntity,
            CobblemonBlocks.PC.get()
        ).build(null)
    }
}