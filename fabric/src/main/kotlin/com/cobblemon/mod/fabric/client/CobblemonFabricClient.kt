/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.fabric.client

import com.cobblemon.mod.common.CobblemonClientImplementation
import com.cobblemon.mod.common.CobblemonEntities
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.CobblemonClient.reloadCodedAssets
import com.cobblemon.mod.common.client.keybind.CobblemonKeyBinds
import com.cobblemon.mod.common.client.keybind.keybinds.HidePartyBinding
import com.cobblemon.mod.common.util.cobblemonResource
import dev.architectury.init.fabric.ArchitecturyClient
import java.util.function.Supplier
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.client.model.TexturedModelData
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.render.entity.model.EntityModelLayer
import net.minecraft.client.util.InputUtil
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceType
import org.lwjgl.glfw.GLFW

class CobblemonFabricClient: ClientModInitializer, CobblemonClientImplementation {
    override fun onInitializeClient() {
        ArchitecturyClient.init()

        CobblemonClient.initialize(this)

        EntityRendererRegistry.register(CobblemonEntities.POKEMON.get()) { CobblemonClient.registerPokemonRenderer(it) }
        EntityRendererRegistry.register(CobblemonEntities.EMPTY_POKEBALL.get()) { CobblemonClient.registerPokeBallRenderer(it) }

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(object : SimpleSynchronousResourceReloadListener {
            override fun getFabricId() = cobblemonResource("resources")
            override fun reload(resourceManager: ResourceManager) { reloadCodedAssets(resourceManager) }
        })
        CobblemonKeyBinds.register(KeyBindingHelper::registerKeyBinding)
    }

    override fun registerLayer(modelLayer: EntityModelLayer, supplier: Supplier<TexturedModelData>) {
        EntityModelLayerRegistry.registerModelLayer(modelLayer) { supplier.get() }
    }
}