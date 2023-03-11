/*
 * Copyright (C) 2023 Cobblemon Contributors
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
import com.cobblemon.mod.common.particle.CobblemonParticles
import com.cobblemon.mod.common.particle.SnowstormParticleType
import java.util.function.Supplier
import net.minecraft.client.MinecraftClient
import net.minecraft.client.model.TexturedModelData
import net.minecraft.client.particle.ParticleFactory
import net.minecraft.client.particle.SpriteProvider
import net.minecraft.client.render.entity.EntityRenderers
import net.minecraft.client.render.entity.model.EntityModelLayer
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.particle.ParticleEffect
import net.minecraft.particle.ParticleType
import net.minecraft.resource.ReloadableResourceManagerImpl
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.SynchronousResourceReloader
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.client.ForgeHooksClient
import net.minecraftforge.client.event.ModelEvent
import net.minecraftforge.client.event.RegisterKeyMappingsEvent
import net.minecraftforge.client.event.RegisterParticleProvidersEvent
import net.minecraftforge.client.event.RenderGuiOverlayEvent
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent

@EventBusSubscriber(
    modid = Cobblemon.MODID,
    bus = EventBusSubscriber.Bus.MOD,
    value = [Dist.CLIENT]
)
object CobblemonForgeClient : CobblemonClientImplementation {
    val particleFactories = mutableListOf<PreparedParticleFactory<*>>()
    class PreparedParticleFactory<T : ParticleEffect>(val type: ParticleType<T>, val factory: (SpriteProvider) -> ParticleFactory<T>) {
        fun register(event: RegisterParticleProvidersEvent) {
            // Use the lambda version of the function so that it is interpreted as a sprite-aware factory
            // otherwise the textures associated with it won't be added to the sprite atlas
            event.register(type, factory)
        }
    }
    init {
        with(thedarkcolour.kotlinforforge.forge.MOD_BUS) {
            addListener(this@CobblemonForgeClient::register3dPokeballModels)
            addListener(this@CobblemonForgeClient::onRegisterParticleProviders)
            addListener(this@CobblemonForgeClient::onKeyMappingRegister)
            addListener(this@CobblemonForgeClient::onClientSetup)
        }

        registerParticleFactory(CobblemonParticles.SNOWSTORM_PARTICLE, SnowstormParticleType::Factory)

        MinecraftForge.EVENT_BUS.addListener<RenderGuiOverlayEvent.Pre> { event ->
            if (event.overlay.id == VanillaGuiOverlay.CHAT_PANEL.id()) {
                CobblemonClient.beforeChatRender(event.poseStack, event.partialTick)
            }
        }
    }

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

    override fun <T : ParticleEffect> registerParticleFactory(type: ParticleType<T>, factory: (SpriteProvider) -> ParticleFactory<T>) {
        particleFactories.add(PreparedParticleFactory(type, factory))
    }

    fun onRegisterParticleProviders(event: RegisterParticleProvidersEvent) {
        for (factoryProviders in particleFactories) {
            factoryProviders.register(event)
        }
    }

    fun onKeyMappingRegister(event: RegisterKeyMappingsEvent) {
        CobblemonKeyBinds.register(event::register)
    }

    private fun register3dPokeballModels(event: ModelEvent.RegisterAdditional) {
        PokeBalls.all().forEach { pokeball ->
            event.register(ModelIdentifier(pokeball.model3d))
        }
    }

}