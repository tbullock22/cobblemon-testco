/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.item

import dev.architectury.registry.registries.RegistrySupplier
import net.minecraft.item.Item
import net.minecraft.util.Identifier

/**
 * A registry of [CobblemonBuiltinItemRenderer]s.
 *
 * @author Nick, Licious
 * @since December 28th, 2022
 */
object CobblemonBuiltinItemRendererRegistry {

    private val renderers = hashMapOf<Identifier, CobblemonBuiltinItemRenderer>()

    fun register(item: RegistrySupplier<out Item>, renderer: CobblemonBuiltinItemRenderer) {
        this.register(item.id, renderer)
    }

    fun register(itemIdentifier: Identifier, renderer: CobblemonBuiltinItemRenderer) {
        this.renderers[itemIdentifier] = renderer
    }

    fun rendererOf(item: Item): CobblemonBuiltinItemRenderer? = this.renderers[item.`arch$registryName`()]

}