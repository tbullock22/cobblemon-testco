/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.particle

import com.cobblemon.mod.common.Cobblemon.LOGGER
import com.cobblemon.mod.common.api.snowstorm.BedrockParticleEffect
import com.cobblemon.mod.common.particle.SnowstormParticleReader
import com.cobblemon.mod.common.util.endsWith
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import java.nio.charset.StandardCharsets
import net.minecraft.resource.ResourceManager
import net.minecraft.util.Identifier

/**
 * Client-side storage of loaded [BedrockParticleEffect]s.
 *
 * @author Hiroku
 * @since February 11th, 2023
 */
object BedrockParticleEffectRepository {
    private val GSON = GsonBuilder().create()
    private val effects = mutableMapOf<Identifier, BedrockParticleEffect>()

    fun loadEffects(resourceManager: ResourceManager) {
        LOGGER.info("Loading particle effects...")
        effects.clear()

        resourceManager.findResources("bedrock/particles") { path -> path.endsWith(".particle.json") }.forEach { identifier, resource ->
            resource.inputStream.use { stream ->
                val json = String(stream.readAllBytes(), StandardCharsets.UTF_8)
                val effect = SnowstormParticleReader.loadEffect(GSON.fromJson(json, JsonObject::class.java))
                effects[effect.id] = effect
            }
        }

        LOGGER.info("Loaded ${effects.size} particle effects")
    }

    fun getEffect(identifier: Identifier): BedrockParticleEffect? = effects[identifier]
}