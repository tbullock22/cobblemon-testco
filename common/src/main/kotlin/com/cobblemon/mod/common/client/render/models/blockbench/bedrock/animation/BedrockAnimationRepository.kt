/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.bedrock.animation

import com.cobblemon.mod.common.Cobblemon.LOGGER
import com.cobblemon.mod.common.util.fromJson
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import net.minecraft.resource.ResourceManager


/**
 * Handles the loading and retrieval of bedrock animations.
 *
 * @author landonjw
 * @since January 5, 2022
 */
object BedrockAnimationRepository {

    private val gson = GsonBuilder()
        .disableHtmlEscaping()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .registerTypeAdapter(BedrockAnimation::class.java, BedrockAnimationAdapter)
        .create()

    private val animationGroups = mutableMapOf<String, BedrockAnimationGroup>()

    fun loadAnimations(resourceManager: ResourceManager, directories: List<String>) {
        LOGGER.info("Loading animations...")
        var animationCount = 0
        animationGroups.clear()
        for (directory in directories) {
            resourceManager.findResources(directory) { it.path.endsWith(".animation.json") }
                .forEach { (identifier, resource) ->
                    val animationGroup = gson.fromJson<BedrockAnimationGroup>(resource.inputStream.reader())
                    val animationGroupName = identifier.path.substringAfterLast("/").replace(".animation.json", "")
                    animationGroups[animationGroupName] = animationGroup
                    animationCount += animationGroup.animations.size
                }
        }
        LOGGER.info("Loaded $animationCount animations from ${animationGroups.size} animation groups")
    }

    fun getAnimation(fileName: String, animationName: String): BedrockAnimation {
        val animationGroup = animationGroups[fileName]
            ?: throw IllegalArgumentException("Unknown animation group: $fileName")

        return animationGroup.animations[animationName]
            ?: throw IllegalArgumentException("Animation $animationName not found in animation group $fileName")
    }
}