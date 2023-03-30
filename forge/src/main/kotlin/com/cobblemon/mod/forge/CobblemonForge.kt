/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.forge

import com.cobblemon.mod.common.*
import com.cobblemon.mod.common.item.group.CobblemonItemGroups
import com.cobblemon.mod.common.mixin.invoker.FireBlockInvoker
import com.cobblemon.mod.common.particle.CobblemonParticles
import com.cobblemon.mod.common.util.didSleep
import com.cobblemon.mod.common.world.feature.CobblemonFeatures
import com.cobblemon.mod.forge.client.CobblemonForgeClient
import com.cobblemon.mod.forge.event.ForgePlatformEventHandler
import com.cobblemon.mod.forge.net.CobblemonForgeNetworkManager
import com.cobblemon.mod.forge.permission.ForgePermissionValidator
import com.cobblemon.mod.forge.worldgen.CobblemonBiomeModifiers
import com.mojang.brigadier.arguments.ArgumentType
import java.util.*
import kotlin.reflect.KClass
import net.minecraft.advancement.criterion.Criteria
import net.minecraft.advancement.criterion.Criterion
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.block.FireBlock
import net.minecraft.command.argument.ArgumentTypes
import net.minecraft.command.argument.serialize.ArgumentSerializer
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey
import net.minecraft.resource.ResourceReloader
import net.minecraft.resource.ResourceType
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.world.GameRules
import net.minecraft.world.biome.Biome
import net.minecraft.world.gen.GenerationStep
import net.minecraft.world.gen.feature.PlacedFeature
import net.minecraftforge.common.ForgeMod
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.ToolActions
import net.minecraftforge.event.AddReloadListenerEvent
import net.minecraftforge.event.CreativeModeTabEvent
import net.minecraftforge.event.OnDatapackSyncEvent
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.event.entity.EntityAttributeCreationEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent
import net.minecraftforge.event.level.BlockEvent
import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import net.minecraftforge.fml.loading.FMLEnvironment
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.RegisterEvent
import net.minecraftforge.server.ServerLifecycleHooks

@Mod(Cobblemon.MODID)
class CobblemonForge : CobblemonImplementation {
    override val modAPI = ModAPI.FORGE
    private val hasBeenSynced = hashSetOf<UUID>()


    private val commandArgumentTypes = DeferredRegister.create(RegistryKeys.COMMAND_ARGUMENT_TYPE, Cobblemon.MODID)
    private val reloadableResources = arrayListOf<ResourceReloader>()

    override val networkManager: NetworkManager = CobblemonForgeNetworkManager

    init {
        CobblemonBiomeModifiers.register()
        with(FMLJavaModLoadingContext.get().modEventBus) {
            this@CobblemonForge.commandArgumentTypes.register(this)
            addListener(this@CobblemonForge::initialize)
            addListener(this@CobblemonForge::serverInit)
            Cobblemon.preInitialize(this@CobblemonForge)
        }
        with(MinecraftForge.EVENT_BUS) {
            addListener(this@CobblemonForge::onDataPackSync)
            addListener(this@CobblemonForge::onLogin)
            addListener(this@CobblemonForge::onLogout)
            addListener(this@CobblemonForge::wakeUp)
            addListener(this@CobblemonForge::handleBlockStripping)
            addListener(this@CobblemonForge::registerCommands)
            addListener(this@CobblemonForge::onReload)
        }
        ForgePlatformEventHandler.register()
    }

    fun wakeUp(event: PlayerWakeUpEvent) {
        val playerEntity = event.entity as? ServerPlayerEntity ?: return
        playerEntity.didSleep()
    }

    fun serverInit(event: FMLDedicatedServerSetupEvent) {
    }

    fun initialize(event: FMLCommonSetupEvent) {
        Cobblemon.LOGGER.info("Initializing...")
        this.networkManager.initClient()
        this.networkManager.initServer()
        Cobblemon.initialize()

        val fireblock = ((Blocks.FIRE as FireBlock) as FireBlockInvoker)
        event.enqueueWork {
            CobblemonBlocks.registerFlammableCobblemonBlocks { block: Block, burn: Int, spread: Int -> fireblock.invokeRegisterFlammableBlock(block, burn, spread) }
        }
    }

    fun onDataPackSync(event: OnDatapackSyncEvent) {
        Cobblemon.dataProvider.sync(event.player ?: return)
    }

    fun onLogin(event: PlayerEvent.PlayerLoggedInEvent) {
        this.hasBeenSynced.add(event.entity.uuid)
    }

    fun onLogout(event: PlayerEvent.PlayerLoggedOutEvent) {
        this.hasBeenSynced.remove(event.entity.uuid)
    }

    override fun isModInstalled(id: String) = ModList.get().isLoaded(id)

    override fun environment(): Environment {
        return if (FMLEnvironment.dist.isClient) Environment.CLIENT else Environment.SERVER
    }

    override fun registerPermissionValidator() {
        Cobblemon.permissionValidator = ForgePermissionValidator
    }

    override fun registerSoundEvents() {
        FMLJavaModLoadingContext.get().modEventBus.addListener<RegisterEvent> { event ->
            event.register(CobblemonSounds.registryKey) { helper ->
                CobblemonSounds.register { identifier, sounds -> helper.register(identifier, sounds) }
            }
        }
    }

    override fun registerBlocks() {
        FMLJavaModLoadingContext.get().modEventBus.addListener<RegisterEvent> { event ->
            event.register(CobblemonBlocks.registryKey) { helper ->
                CobblemonBlocks.register { identifier, block -> helper.register(identifier, block) }
            }
        }
    }

    override fun registerParticles() {
        FMLJavaModLoadingContext.get().modEventBus.addListener<RegisterEvent> { event ->
            event.register(CobblemonParticles.registryKey) { helper ->
                CobblemonParticles.register { identifier, particleType -> helper.register(identifier, particleType) }
            }
        }
    }

    private fun handleBlockStripping(e: BlockEvent.BlockToolModificationEvent) {
        if (e.toolAction == ToolActions.AXE_STRIP) {
            val start = e.state.block
            val result = CobblemonBlocks.strippedBlocks()[start] ?: return
            e.finalState = result.getStateWithProperties(e.state)
        }
    }

    override fun registerItems() {
        with(FMLJavaModLoadingContext.get().modEventBus) {
            addListener<RegisterEvent> { event ->
                event.register(CobblemonItems.registryKey) { helper ->
                    CobblemonItems.register { identifier, item -> helper.register(identifier, item) }
                }
            }
            // ToDo sort out ordering being insertion
            addListener<CreativeModeTabEvent.Register> { event ->
                CobblemonItemGroups.register { provider ->
                    event.registerCreativeModeTab(provider.identifier) { builder ->
                        builder.displayName(provider.displayName)
                        builder.icon(provider.icon)
                    }
                }
            }
            addListener<CreativeModeTabEvent.BuildContents> { event ->
                CobblemonItems.registerToItemGroups { group, item ->
                    if (event.tab == group) {
                        event.add(item)
                    }
                }
            }
        }
    }

    override fun registerEntityTypes() {
        FMLJavaModLoadingContext.get().modEventBus.addListener<RegisterEvent> { event ->
            event.register(CobblemonEntities.registryKey) { helper ->
                CobblemonEntities.register { identifier, type -> helper.register(identifier, type) }
            }
        }
    }

    override fun registerEntityAttributes() {
        FMLJavaModLoadingContext.get().modEventBus.addListener<EntityAttributeCreationEvent> { event ->
            CobblemonEntities.registerAttributes { entityType, builder ->
                builder.add(ForgeMod.ENTITY_GRAVITY.get())
                    .add(ForgeMod.NAMETAG_DISTANCE.get())
                    .add(ForgeMod.SWIM_SPEED.get())
                event.put(entityType, builder.build())
            }
        }
    }

    override fun registerBlockEntityTypes() {
        FMLJavaModLoadingContext.get().modEventBus.addListener<RegisterEvent> { event ->
            event.register(CobblemonBlockEntities.registryKey) { helper ->
                CobblemonBlockEntities.register { identifier, type -> helper.register(identifier, type) }
            }
        }
    }

    override fun registerWorldGenFeatures() {
        FMLJavaModLoadingContext.get().modEventBus.addListener<RegisterEvent> { event ->
            event.register(CobblemonFeatures.registryKey) { helper ->
                CobblemonFeatures.register { identifier, feature -> helper.register(identifier, feature) }
            }
        }
    }

    override fun addFeatureToWorldGen(feature: RegistryKey<PlacedFeature>, step: GenerationStep.Feature, validTag: TagKey<Biome>?) {
        CobblemonBiomeModifiers.add(feature, step, validTag)
    }

    override fun <A : ArgumentType<*>, T : ArgumentSerializer.ArgumentTypeProperties<A>> registerCommandArgument(identifier: Identifier, argumentClass: KClass<A>, serializer: ArgumentSerializer<A, T>) {
        this.commandArgumentTypes.register(identifier.path) { ArgumentTypes.registerByClass(argumentClass.java, serializer) }
    }

    private fun registerCommands(e: RegisterCommandsEvent) {
        CobblemonCommands.register(e.dispatcher, e.buildContext, e.commandSelection)
    }

    override fun <T : GameRules.Rule<T>> registerGameRule(name: String, category: GameRules.Category, type: GameRules.Type<T>): GameRules.Key<T> = GameRules.register(name, category, type)

    override fun <T : Criterion<*>> registerCriteria(criteria: T): T = Criteria.register(criteria)

    override fun registerResourceReloader(identifier: Identifier, reloader: ResourceReloader, type: ResourceType, dependencies: Collection<Identifier>) {
        if (type == ResourceType.SERVER_DATA) {
            this.reloadableResources += reloader
        }
        else {
            CobblemonForgeClient.registerResourceReloader(reloader)
        }
    }

    private fun onReload(e: AddReloadListenerEvent) {
        this.reloadableResources.forEach(e::addListener)
    }

    override fun server(): MinecraftServer? = ServerLifecycleHooks.getCurrentServer()

}