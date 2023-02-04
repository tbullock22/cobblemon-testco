/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client

import com.cobblemon.mod.common.Cobblemon.LOGGER
import com.cobblemon.mod.common.CobblemonBlockEntities
import com.cobblemon.mod.common.CobblemonBlocks
import com.cobblemon.mod.common.CobblemonClientImplementation
import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.api.scheduling.ScheduledTaskTracker
import com.cobblemon.mod.common.client.battle.ClientBattle
import com.cobblemon.mod.common.client.gui.PartyOverlay
import com.cobblemon.mod.common.client.gui.battle.BattleOverlay
import com.cobblemon.mod.common.client.net.ClientPacketRegistrar
import com.cobblemon.mod.common.client.render.block.HealingMachineRenderer
import com.cobblemon.mod.common.client.render.item.CobblemonBuiltinItemRendererRegistry
import com.cobblemon.mod.common.client.render.item.PokemonItemRenderer
import com.cobblemon.mod.common.client.render.layer.PokemonOnShoulderRenderer
import com.cobblemon.mod.common.client.render.models.blockbench.bedrock.animation.BedrockAnimationRepository
import com.cobblemon.mod.common.client.render.models.blockbench.repository.PokeBallModelRepository
import com.cobblemon.mod.common.client.render.models.blockbench.repository.PokemonModelRepository
import com.cobblemon.mod.common.client.render.pokeball.PokeBallRenderer
import com.cobblemon.mod.common.client.render.pokemon.PokemonRenderer
import com.cobblemon.mod.common.client.starter.ClientPlayerData
import com.cobblemon.mod.common.client.storage.ClientStorageManager
import com.cobblemon.mod.common.data.CobblemonDataProvider
import dev.architectury.event.events.client.ClientPlayerEvent.CLIENT_PLAYER_JOIN
import dev.architectury.event.events.client.ClientPlayerEvent.CLIENT_PLAYER_QUIT
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry
import dev.architectury.registry.client.rendering.ColorHandlerRegistry
import dev.architectury.registry.client.rendering.RenderTypeRegistry
import net.minecraft.client.color.block.BlockColorProvider
import net.minecraft.client.color.item.ItemColorProvider
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.entity.EntityRenderer
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.LivingEntityRenderer
import net.minecraft.client.render.entity.model.PlayerEntityModel
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.resource.ResourceManager

object CobblemonClient {
    lateinit var implementation: CobblemonClientImplementation
    val storage = ClientStorageManager()
    var battle: ClientBattle? = null
    var clientPlayerData = ClientPlayerData()
    /** If true then we won't bother them anymore about choosing a starter even if it's a thing they can do. */
    var checkedStarterScreen = false

    val overlay: PartyOverlay by lazy { PartyOverlay() }
    val battleOverlay: BattleOverlay by lazy { BattleOverlay() }

    fun onLogin() {
        clientPlayerData = ClientPlayerData()
        storage.onLogin()
        CobblemonDataProvider.canReload = false
    }

    fun onLogout() {
        storage.onLogout()
        battle = null
        battleOverlay.onLogout()
        ScheduledTaskTracker.clear()
        checkedStarterScreen = false
        CobblemonDataProvider.canReload = true
    }

    fun initialize(implementation: CobblemonClientImplementation) {
        LOGGER.info("Initializing Cobblemon client")
        this.implementation = implementation

        CLIENT_PLAYER_JOIN.register { onLogin() }
        CLIENT_PLAYER_QUIT.register { onLogout() }

        ClientPacketRegistrar.registerHandlers()

        LOGGER.info("Initializing PokéBall models")
        PokeBallModelRepository.init()

        BlockEntityRendererRegistry.register(CobblemonBlockEntities.HEALING_MACHINE.get(), ::HealingMachineRenderer)

        registerBlockRenderTypes()
        registerColors()
        LOGGER.info("Registering custom BuiltinItemRenderers")
        CobblemonBuiltinItemRendererRegistry.register(CobblemonItems.POKEMON_MODEL, PokemonItemRenderer())
    }

    fun registerColors() {
        ColorHandlerRegistry.registerBlockColors(BlockColorProvider { blockState, blockAndTintGetter, blockPos, i ->
            return@BlockColorProvider 0x71c219
        }, CobblemonBlocks.APRICORN_LEAVES.get())

        ColorHandlerRegistry.registerItemColors(ItemColorProvider { itemStack, i ->
            return@ItemColorProvider 0x71c219
        }, CobblemonItems.APRICORN_LEAVES.get())
    }

    private fun registerBlockRenderTypes() {
        RenderTypeRegistry.register(RenderLayer.getCutout(),
            CobblemonBlocks.APRICORN_DOOR.get(),
            CobblemonBlocks.APRICORN_TRAPDOOR.get(),
            CobblemonBlocks.BLACK_APRICORN_SAPLING.get(),
            CobblemonBlocks.BLUE_APRICORN_SAPLING.get(),
            CobblemonBlocks.GREEN_APRICORN_SAPLING.get(),
            CobblemonBlocks.PINK_APRICORN_SAPLING.get(),
            CobblemonBlocks.RED_APRICORN_SAPLING.get(),
            CobblemonBlocks.WHITE_APRICORN_SAPLING.get(),
            CobblemonBlocks.YELLOW_APRICORN_SAPLING.get(),
            CobblemonBlocks.BLACK_APRICORN.get(),
            CobblemonBlocks.BLUE_APRICORN.get(),
            CobblemonBlocks.GREEN_APRICORN.get(),
            CobblemonBlocks.PINK_APRICORN.get(),
            CobblemonBlocks.RED_APRICORN.get(),
            CobblemonBlocks.WHITE_APRICORN.get(),
            CobblemonBlocks.YELLOW_APRICORN.get(),
            CobblemonBlocks.HEALING_MACHINE.get())
    }

    fun beforeChatRender(matrixStack: MatrixStack, partialDeltaTicks: Float) {
        if (battle == null) {
            overlay.render(matrixStack, partialDeltaTicks)
        } else {
            battleOverlay.render(matrixStack, partialDeltaTicks)
        }
    }

    fun onAddLayer(skinMap: Map<String, EntityRenderer<out PlayerEntity>>?) {
        var renderer: LivingEntityRenderer<PlayerEntity, PlayerEntityModel<PlayerEntity>>? = skinMap?.get("default") as LivingEntityRenderer<PlayerEntity, PlayerEntityModel<PlayerEntity>>
        renderer?.addFeature(PokemonOnShoulderRenderer(renderer))
        renderer = skinMap.get("slim") as LivingEntityRenderer<PlayerEntity, PlayerEntityModel<PlayerEntity>>?
        renderer?.addFeature(PokemonOnShoulderRenderer(renderer))
    }

    fun registerPokemonRenderer(context: EntityRendererFactory.Context): PokemonRenderer {
        LOGGER.info("Registering Pokémon renderer")
        PokemonModelRepository.initializeModels(context)
        return PokemonRenderer(context)
    }

    fun registerPokeBallRenderer(context: EntityRendererFactory.Context): PokeBallRenderer {
        LOGGER.info("Registering PokéBall renderer")
        PokeBallModelRepository.initializeModels(context)
        return PokeBallRenderer(context)
    }

    fun reloadCodedAssets(resourceManager: ResourceManager) {
        LOGGER.info("Reloading assets")
        BedrockAnimationRepository.loadAnimations(resourceManager)
        PokemonModelRepository.reload(resourceManager)
        LOGGER.info("Loaded assets")
//        PokeBallModelRepository.reload(resourceManager)
    }

    fun endBattle() {
        battle = null
    }
}