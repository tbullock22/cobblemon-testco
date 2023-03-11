/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.particle

import com.cobblemon.mod.common.registry.CompletableRegistry
import dev.architectury.registry.registries.RegistrySupplier
import java.util.function.Supplier
import net.minecraft.particle.ParticleType
import net.minecraft.util.registry.Registry

object CobblemonParticles : CompletableRegistry<ParticleType<*>>(Registry.PARTICLE_TYPE_KEY) {
    private fun <T : ParticleType<*>> register(name: String, particleType: Supplier<T>): RegistrySupplier<T> {
        return queue(name, particleType)
    }

    val SNOWSTORM_PARTICLE = SnowstormParticleType()

    val SNOWSTORM_PARTICLE_TYPE = register("snowstorm") { SNOWSTORM_PARTICLE }
}