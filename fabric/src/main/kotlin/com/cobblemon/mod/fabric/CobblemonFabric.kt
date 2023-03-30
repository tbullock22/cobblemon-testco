/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.fabric

import com.cobblemon.mod.common.*
import com.cobblemon.mod.common.item.group.CobblemonItemGroups
import com.cobblemon.mod.common.particle.CobblemonParticles
import com.cobblemon.mod.common.platform.events.PlatformEvents
import com.cobblemon.mod.common.platform.events.ServerEvent
import com.cobblemon.mod.common.platform.events.ServerPlayerEvent
import com.cobblemon.mod.common.platform.events.ServerTickEvent
import com.cobblemon.mod.common.util.didSleep
import com.cobblemon.mod.common.world.feature.CobblemonFeatures
import com.cobblemon.mod.fabric.net.CobblemonFabricNetworkManager
import com.cobblemon.mod.fabric.permission.FabricPermissionValidator
import com.mojang.brigadier.arguments.ArgumentType
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import kotlin.reflect.KClass
import net.fabricmc.api.EnvType
import net.fabricmc.fabric.api.biome.v1.BiomeModifications
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricDefaultAttributeRegistry
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.advancement.criterion.Criteria
import net.minecraft.advancement.criterion.Criterion
import net.minecraft.block.Block
import net.minecraft.client.MinecraftClient
import net.minecraft.command.argument.serialize.ArgumentSerializer
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.tag.TagKey
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceReloader
import net.minecraft.resource.ResourceType
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.ActionResult
import net.minecraft.util.Identifier
import net.minecraft.util.profiler.Profiler
import net.minecraft.world.GameRules
import net.minecraft.world.biome.Biome
import net.minecraft.world.gen.GenerationStep
import net.minecraft.world.gen.feature.PlacedFeature

object CobblemonFabric : CobblemonImplementation {
    override val modAPI = ModAPI.FABRIC

    private var server: MinecraftServer? = null

    override val networkManager: NetworkManager = CobblemonFabricNetworkManager

    fun initialize() {
        Cobblemon.preInitialize(this)
        this.networkManager.initServer()

        Cobblemon.initialize()
        /*
        if (FabricLoader.getInstance().getModContainer("luckperms").isPresent) {
            Cobblemon.permissionValidator = LuckPermsPermissionValidator()
        }
         */
        if (FabricLoader.getInstance().getModContainer("fabric-permissions-api-v0").isPresent) {
            Cobblemon.permissionValidator = FabricPermissionValidator()
        }
        EntitySleepEvents.STOP_SLEEPING.register { playerEntity, _ ->
            if (playerEntity !is ServerPlayerEntity) {
                return@register
            }

            playerEntity.didSleep()
        }

        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register { player, isLogin ->
            if (isLogin) {
                Cobblemon.dataProvider.sync(player)
            }
        }
        ServerLifecycleEvents.SERVER_STARTING.register { server ->
            this.server = server
            PlatformEvents.SERVER_STARTING.post(ServerEvent.Starting(server))
        }
        ServerLifecycleEvents.SERVER_STARTED.register { server -> PlatformEvents.SERVER_STARTED.post(ServerEvent.Started(server)) }
        ServerLifecycleEvents.SERVER_STOPPING.register { server -> PlatformEvents.SERVER_STOPPING.post(ServerEvent.Stopping(server)) }
        ServerLifecycleEvents.SERVER_STOPPED.register { server -> PlatformEvents.SERVER_STOPPED.post(ServerEvent.Stopped(server)) }
        ServerTickEvents.START_SERVER_TICK.register { server -> PlatformEvents.SERVER_TICK_PRE.post(ServerTickEvent.Pre(server)) }
        ServerTickEvents.END_SERVER_TICK.register { server -> PlatformEvents.SERVER_TICK_POST.post(ServerTickEvent.Post(server)) }
        ServerPlayConnectionEvents.JOIN.register { handler, _, _ -> PlatformEvents.SERVER_PLAYER_LOGIN.post(ServerPlayerEvent.Login(handler.player)) }
        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ -> PlatformEvents.SERVER_PLAYER_LOGOUT.post(ServerPlayerEvent.Logout(handler.player)) }
        ServerLivingEntityEvents.ALLOW_DEATH.register { entity, _, _ ->
            if (entity is ServerPlayerEntity) {
                PlatformEvents.PLAYER_DEATH.postThen(
                    event = ServerPlayerEvent.Death(entity),
                    ifSucceeded = {},
                    ifCanceled = { return@register false }
                )
            }
            return@register true
        }

        UseBlockCallback.EVENT.register { player, world, hand, hitResult ->
            val serverPlayer = player as? ServerPlayerEntity ?: return@register ActionResult.PASS
            PlatformEvents.RIGHT_CLICK_BLOCK.postThen(
                event = ServerPlayerEvent.RightClickBlock(serverPlayer, hitResult.blockPos, hand, hitResult.side),
                ifSucceeded = {},
                ifCanceled = { return@register ActionResult.FAIL }
            )
            return@register ActionResult.PASS
        }

        CommandRegistrationCallback.EVENT.register(CobblemonCommands::register)
    }

    override fun isModInstalled(id: String) = FabricLoader.getInstance().isModLoaded(id)

    override fun environment(): Environment {
        return when(FabricLoader.getInstance().environmentType) {
            EnvType.CLIENT -> Environment.CLIENT
            EnvType.SERVER -> Environment.SERVER
            else -> throw IllegalStateException("Fabric implementation cannot resolve environment yet")
        }
    }

    override fun registerPermissionValidator() {
        if (this.isModInstalled("fabric-permissions-api-v0")) {
            Cobblemon.permissionValidator = FabricPermissionValidator()
        }
    }

    override fun registerSoundEvents() {
        CobblemonSounds.register { identifier, sound -> Registry.register(CobblemonSounds.registry, identifier, sound) }
    }

    override fun registerItems() {
        CobblemonItems.register { identifier, item -> Registry.register(CobblemonItems.registry, identifier, item) }
        CobblemonItemGroups.register { provider ->
            FabricItemGroup.builder(provider.identifier)
                .displayName(provider.displayName)
                .icon(provider.icon)
                .build()
        }
        CobblemonItems.registerToItemGroups { group, item -> ItemGroupEvents.modifyEntriesEvent(group).register { entries -> entries.add(item) } }
    }

    override fun registerBlocks() {
        CobblemonBlocks.register { identifier, item -> Registry.register(CobblemonBlocks.registry, identifier, item) }
        CobblemonBlocks.strippedBlocks().forEach { (input, output) -> StrippableBlockRegistry.register(input, output) }
        CobblemonBlocks.registerFlammableCobblemonBlocks { block: Block, burn: Int, spread: Int -> FlammableBlockRegistry.getDefaultInstance().add(block, burn, spread) }
    }

    override fun registerEntityTypes() {
        CobblemonEntities.register { identifier, type -> Registry.register(CobblemonEntities.registry, identifier, type) }
    }

    override fun registerEntityAttributes() {
        CobblemonEntities.registerAttributes { entityType, builder -> FabricDefaultAttributeRegistry.register(entityType, builder) }
    }

    override fun registerBlockEntityTypes() {
        CobblemonBlockEntities.register { identifier, type -> Registry.register(CobblemonBlockEntities.registry, identifier, type) }
    }

    override fun registerWorldGenFeatures() {
        CobblemonFeatures.register { identifier, feature -> Registry.register(CobblemonFeatures.registry, identifier, feature) }
    }

    override fun registerParticles() {
        CobblemonParticles.register { identifier, particleType -> Registry.register(CobblemonParticles.registry, identifier, particleType) }
    }

    override fun addFeatureToWorldGen(feature: RegistryKey<PlacedFeature>, step: GenerationStep.Feature, validTag: TagKey<Biome>?) {
        val predicate: (BiomeSelectionContext) -> Boolean = { context -> validTag == null || context.hasTag(validTag) }
        BiomeModifications.addFeature(predicate, step, feature)
    }

    override fun <A : ArgumentType<*>, T : ArgumentSerializer.ArgumentTypeProperties<A>> registerCommandArgument(identifier: Identifier, argumentClass: KClass<A>, serializer: ArgumentSerializer<A, T>) {
        ArgumentTypeRegistry.registerArgumentType(identifier, argumentClass.java, serializer)
    }

    override fun <T : GameRules.Rule<T>> registerGameRule(name: String, category: GameRules.Category, type: GameRules.Type<T>): GameRules.Key<T> = GameRuleRegistry.register(name, category, type)

    override fun <T : Criterion<*>> registerCriteria(criteria: T): T = Criteria.register(criteria)

    override fun registerResourceReloader(identifier: Identifier, reloader: ResourceReloader, type: ResourceType, dependencies: Collection<Identifier>) {
        ResourceManagerHelper.get(type).registerReloadListener(CobblemonReloadListener(identifier, reloader, dependencies))
    }

    override fun server(): MinecraftServer? = if (this.environment() == Environment.CLIENT) MinecraftClient.getInstance().server else this.server

    private class CobblemonReloadListener(private val identifier: Identifier, private val reloader: ResourceReloader, private val dependencies: Collection<Identifier>) : IdentifiableResourceReloadListener {

        override fun reload(synchronizer: ResourceReloader.Synchronizer, manager: ResourceManager, prepareProfiler: Profiler, applyProfiler: Profiler, prepareExecutor: Executor, applyExecutor: Executor): CompletableFuture<Void> = this.reloader.reload(synchronizer, manager, prepareProfiler, applyProfiler, prepareExecutor, applyExecutor)

        override fun getFabricId(): Identifier = this.identifier

        override fun getName(): String = this.reloader.name

        override fun getFabricDependencies(): MutableCollection<Identifier> = this.dependencies.toMutableList()
    }

}