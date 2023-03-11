/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common

import java.util.function.Supplier
import net.minecraft.client.model.TexturedModelData
import net.minecraft.client.particle.ParticleFactory
import net.minecraft.client.particle.SpriteProvider
import net.minecraft.client.render.entity.model.EntityModelLayer
import net.minecraft.particle.ParticleEffect
import net.minecraft.particle.ParticleType

interface CobblemonClientImplementation {
    fun registerLayer(modelLayer: EntityModelLayer, supplier: Supplier<TexturedModelData>)
    fun <T : ParticleEffect> registerParticleFactory(
        type: ParticleType<T>,
        factory: (SpriteProvider) -> ParticleFactory<T>
    )
}