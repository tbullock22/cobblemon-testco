/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.forge.client

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonClientImplementation
import com.cobblemon.mod.common.CobblemonEntities
import com.cobblemon.mod.common.api.pokeball.PokeBalls
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.keybind.CobblemonKeyBinds
import java.util.function.Supplier
import net.minecraft.client.MinecraftClient
import net.minecraft.client.model.TexturedModelData
import net.minecraft.client.render.entity.EntityRenderers
import net.minecraft.client.render.entity.model.EntityModelLayer
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.resource.ReloadableResourceManagerImpl
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.SynchronousResourceReloader
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.client.ForgeHooksClient
import net.minecraftforge.client.event.ModelEvent
import net.minecraftforge.client.event.RegisterKeyMappingsEvent
import net.minecraftforge.client.event.RenderGuiOverlayEvent
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext

@EventBusSubscriber(
    modid = Cobblemon.MODID,
    bus = EventBusSubscriber.Bus.MOD,
    value = [Dist.CLIENT]
)
object CobblemonForgeClient : CobblemonClientImplementation {

    init {
        FMLJavaModLoadingContext.get().modEventBus.addListener(this::register3dPokeballModels)
    }

    @JvmStatic
    @SubscribeEvent
    fun onClientSetup(event: FMLClientSetupEvent) {
        (MinecraftClient.getInstance().resourceManager as ReloadableResourceManagerImpl)
            .registerReloader(object : SynchronousResourceReloader {
                override fun reload(resourceManager: ResourceManager) {
                    CobblemonClient.reloadCodedAssets(resourceManager)
                }
            })
        CobblemonClient.reloadCodedAssets(MinecraftClient.getInstance().resourceManager)
        MinecraftForge.EVENT_BUS.register(this)
        event.enqueueWork {
            CobblemonClient.initialize(this)
            EntityRenderers.register(CobblemonEntities.POKEMON.get()) { CobblemonClient.registerPokemonRenderer(it) }
            EntityRenderers.register(CobblemonEntities.EMPTY_POKEBALL.get()) { CobblemonClient.registerPokeBallRenderer(it) }
        }

    }

    override fun registerLayer(modelLayer: EntityModelLayer, supplier: Supplier<TexturedModelData>) {
        ForgeHooksClient.registerLayerDefinition(modelLayer, supplier)
    }

    @SubscribeEvent
    fun onRenderGameOverlay(event: RenderGuiOverlayEvent.Pre) {
        if (event.overlay.id == VanillaGuiOverlay.CHAT_PANEL.id()) {
            CobblemonClient.beforeChatRender(event.poseStack, event.partialTick)
        }
    }

    @JvmStatic
    @SubscribeEvent
    fun onKeyMappingRegister(event: RegisterKeyMappingsEvent) {
        CobblemonKeyBinds.register(event::register)
    }

    private fun register3dPokeballModels(event: ModelEvent.RegisterAdditional) {
        PokeBalls.all().forEach { pokeball ->
            event.register(ModelIdentifier(pokeball.model3d))
        }
    }

}