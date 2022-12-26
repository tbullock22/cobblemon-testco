/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.pc

import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.client.util.math.MatrixStack

class GrabbedStorageSlot(
    x: Int, y: Int,
    parent: StorageWidget,
    private val pokemon: Pokemon
) : StorageSlot(x, y, parent, {}) {

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        renderSlot(matrices, mouseX - (width / 2), mouseY - (height / 2))
    }

    override fun getPokemon(): Pokemon? {
        return pokemon
    }

    override fun isSelected(): Boolean {
        return true
    }
}