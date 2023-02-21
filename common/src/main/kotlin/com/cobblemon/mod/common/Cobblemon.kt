/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common

import com.cobblemon.mod.common.advancement.CobblemonCriteria
import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.api.SeasonResolver
import com.cobblemon.mod.common.api.data.DataProvider
import com.cobblemon.mod.common.api.drop.CommandDropEntry
import com.cobblemon.mod.common.api.drop.DropEntry
import com.cobblemon.mod.common.api.drop.ItemDropEntry
import com.cobblemon.mod.common.api.events.CobblemonEvents.BATTLE_VICTORY
import com.cobblemon.mod.common.api.events.CobblemonEvents.DATA_SYNCHRONIZED
import com.cobblemon.mod.common.api.events.CobblemonEvents.EVOLUTION_COMPLETE
import com.cobblemon.mod.common.api.events.CobblemonEvents.LIVING_DEATH
import com.cobblemon.mod.common.api.events.CobblemonEvents.PLAYER_QUIT
import com.cobblemon.mod.common.api.events.CobblemonEvents.POKEMON_CAPTURED
import com.cobblemon.mod.common.api.events.CobblemonEvents.SERVER_STARTED
import com.cobblemon.mod.common.api.events.CobblemonEvents.SERVER_STOPPED
import com.cobblemon.mod.common.api.events.CobblemonEvents.TICK_POST
import com.cobblemon.mod.common.api.net.serializers.PoseTypeDataSerializer
import com.cobblemon.mod.common.api.net.serializers.StringSetDataSerializer
import com.cobblemon.mod.common.api.net.serializers.Vec3DataSerializer
import com.cobblemon.mod.common.api.permission.PermissionValidator
import com.cobblemon.mod.common.api.pokeball.catching.calculators.CaptureCalculator
import com.cobblemon.mod.common.api.pokeball.catching.calculators.CaptureCalculators
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.pokemon.aspect.AspectProvider
import com.cobblemon.mod.common.api.pokemon.effect.ShoulderEffectRegistry
import com.cobblemon.mod.common.api.pokemon.experience.ExperienceCalculator
import com.cobblemon.mod.common.api.pokemon.experience.ExperienceGroups
import com.cobblemon.mod.common.api.pokemon.experience.StandardExperienceCalculator
import com.cobblemon.mod.common.api.pokemon.feature.ChoiceSpeciesFeatureProvider
import com.cobblemon.mod.common.api.pokemon.feature.FlagSpeciesFeature
import com.cobblemon.mod.common.api.pokemon.feature.FlagSpeciesFeatureProvider
import com.cobblemon.mod.common.api.pokemon.feature.SpeciesFeatures
import com.cobblemon.mod.common.api.pokemon.helditem.HeldItemProvider
import com.cobblemon.mod.common.api.pokemon.stats.EvCalculator
import com.cobblemon.mod.common.api.pokemon.stats.Generation8EvCalculator
import com.cobblemon.mod.common.api.pokemon.stats.StatProvider
import com.cobblemon.mod.common.api.properties.CustomPokemonProperty
import com.cobblemon.mod.common.api.reactive.Observable.Companion.filter
import com.cobblemon.mod.common.api.reactive.Observable.Companion.map
import com.cobblemon.mod.common.api.reactive.Observable.Companion.takeFirst
import com.cobblemon.mod.common.api.scheduling.ScheduledTaskTracker
import com.cobblemon.mod.common.api.spawning.BestSpawner
import com.cobblemon.mod.common.api.spawning.CobblemonSpawningProspector
import com.cobblemon.mod.common.api.spawning.context.AreaContextResolver
import com.cobblemon.mod.common.api.spawning.prospecting.SpawningProspector
import com.cobblemon.mod.common.api.starter.StarterHandler
import com.cobblemon.mod.common.api.storage.PokemonStoreManager
import com.cobblemon.mod.common.api.storage.adapter.conversions.ReforgedConversion
import com.cobblemon.mod.common.api.storage.adapter.flatifle.FileStoreAdapter
import com.cobblemon.mod.common.api.storage.adapter.flatifle.JSONStoreAdapter
import com.cobblemon.mod.common.api.storage.adapter.flatifle.NBTStoreAdapter
import com.cobblemon.mod.common.api.storage.factory.FileBackedPokemonStoreFactory
import com.cobblemon.mod.common.api.storage.pc.PCStore
import com.cobblemon.mod.common.api.storage.pc.link.PCLinkManager
import com.cobblemon.mod.common.api.storage.player.PlayerDataStoreManager
import com.cobblemon.mod.common.battles.BattleFormat
import com.cobblemon.mod.common.battles.BattleRegistry
import com.cobblemon.mod.common.battles.BattleSide
import com.cobblemon.mod.common.battles.ShowdownThread
import com.cobblemon.mod.common.battles.actor.PokemonBattleActor
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.config.CobblemonConfig
import com.cobblemon.mod.common.config.LastChangedVersion
import com.cobblemon.mod.common.config.constraint.IntConstraint
import com.cobblemon.mod.common.config.starter.StarterConfig
import com.cobblemon.mod.common.data.CobblemonDataProvider
import com.cobblemon.mod.common.events.AdvancementHandler
import com.cobblemon.mod.common.events.ServerTickHandler
import com.cobblemon.mod.common.item.PokeBallItem
import com.cobblemon.mod.common.net.messages.client.settings.ServerSettingsPacket
import com.cobblemon.mod.common.net.serverhandling.ServerPacketRegistrar
import com.cobblemon.mod.common.particle.CobblemonParticles
import com.cobblemon.mod.common.permission.LaxPermissionValidator
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.aspects.GENDER_ASPECT
import com.cobblemon.mod.common.pokemon.aspects.SHINY_ASPECT
import com.cobblemon.mod.common.pokemon.evolution.variants.BlockClickEvolution
import com.cobblemon.mod.common.pokemon.feature.*
import com.cobblemon.mod.common.pokemon.helditem.CobblemonHeldItemManager
import com.cobblemon.mod.common.pokemon.properties.HiddenAbilityPropertyType
import com.cobblemon.mod.common.pokemon.properties.UncatchableProperty
import com.cobblemon.mod.common.pokemon.properties.UntradeableProperty
import com.cobblemon.mod.common.pokemon.properties.tags.PokemonFlagProperty
import com.cobblemon.mod.common.pokemon.stat.CobblemonStatProvider
import com.cobblemon.mod.common.registry.CompletableRegistry
import com.cobblemon.mod.common.starter.CobblemonStarterHandler
import com.cobblemon.mod.common.util.DataKeys
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.getServer
import com.cobblemon.mod.common.util.ifDedicatedServer
import com.cobblemon.mod.common.util.isLaterVersion
import com.cobblemon.mod.common.util.party
import com.cobblemon.mod.common.util.removeAmountIf
import com.cobblemon.mod.common.world.gamerules.CobblemonGameRules
import com.cobblemon.mod.common.world.feature.CobblemonOrePlacedFeatures
import com.cobblemon.mod.common.world.placement.CobblemonPlacementTypes
import dev.architectury.event.EventResult
import dev.architectury.event.events.common.CommandRegistrationEvent
import dev.architectury.event.events.common.InteractionEvent
import dev.architectury.hooks.item.tool.AxeItemHooks
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.PrintWriter
import java.util.UUID
import kotlin.properties.Delegates
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.WorldSavePath
import net.minecraft.util.registry.RegistryKey
import net.minecraft.world.World
import org.apache.logging.log4j.LogManager

object Cobblemon {
    const val MODID = "cobblemon"
    const val VERSION = "1.3.0"
    const val CONFIG_PATH = "config/$MODID/main.json"
    val LOGGER = LogManager.getLogger()

    lateinit var implementation: CobblemonImplementation
    @Deprecated("This field is now a config value", ReplaceWith("Cobblemon.config.captureCalculator"))
    var captureCalculator: CaptureCalculator
        get() = this.config.captureCalculator
        set(value) {
            this.config.captureCalculator = value
        }
    var experienceCalculator: ExperienceCalculator = StandardExperienceCalculator
    var evYieldCalculator: EvCalculator = Generation8EvCalculator
    var starterHandler: StarterHandler = CobblemonStarterHandler()
    var isDedicatedServer = false
    val showdownThread = ShowdownThread()
    lateinit var config: CobblemonConfig
    var prospector: SpawningProspector = CobblemonSpawningProspector
    var areaContextResolver: AreaContextResolver = object : AreaContextResolver {}
    val bestSpawner = BestSpawner
    val battleRegistry = BattleRegistry
    var storage = PokemonStoreManager()
    lateinit var playerData: PlayerDataStoreManager
    lateinit var starterConfig: StarterConfig
    val dataProvider: DataProvider = CobblemonDataProvider
    var permissionValidator: PermissionValidator by Delegates.observable(LaxPermissionValidator().also { it.initialize() }) { _, _, newValue -> newValue.initialize() }
    var statProvider: StatProvider = CobblemonStatProvider
    var seasonResolver: SeasonResolver = TagSeasonResolver

    fun preInitialize(implementation: CobblemonImplementation) {
        DropEntry.register("command", CommandDropEntry::class.java)
        DropEntry.register("item", ItemDropEntry::class.java, isDefault = true)

        ExperienceGroups.registerDefaults()
        CaptureCalculators.registerDefaults()

        this.loadConfig()
        this.implementation = implementation

        CobblemonCriteria // Init the fields and register the criteria
        CobblemonEntities.register()
        CobblemonBlocks.register()
        CobblemonBlockEntities.register()
        CobblemonItems.register()
        ServerPacketRegistrar.registerHandlers()
        CobblemonSounds.register()
        CobblemonFeatures.register()
        CobblemonGameRules.register()
        CobblemonParticles.register()

        ShoulderEffectRegistry.register()

        DATA_SYNCHRONIZED.subscribe {
            storage.onPlayerDataSync(it)
            playerData.get(it).sendToPlayer(it)
            starterHandler.handleJoin(it)
            ServerSettingsPacket().sendToPlayer(it)
        }
        PLAYER_QUIT.subscribe {
            PCLinkManager.removeLink(it.uuid)
            BattleRegistry.getBattleByParticipatingPlayer(it)?.stop()
        }
        LIVING_DEATH.pipe(filter { it is ServerPlayerEntity }, map { it as ServerPlayerEntity }).subscribe {
            PCLinkManager.removeLink(it.uuid)
            battleRegistry.getBattleByParticipatingPlayer(it)?.stop()
        }

        InteractionEvent.RIGHT_CLICK_BLOCK.register(InteractionEvent.RightClickBlock { pl, _, pos, _ ->
            val player = pl as? ServerPlayerEntity ?: return@RightClickBlock EventResult.pass()
            val block = player.world.getBlockState(pos).block
            player.party().forEach { pokemon ->
                pokemon.evolutions
                    .filterIsInstance<BlockClickEvolution>()
                    .forEach { evolution ->
                        evolution.attemptEvolution(pokemon, BlockClickEvolution.BlockInteractionContext(block, player.world))
                    }
            }
            return@RightClickBlock EventResult.pass()
        })
        TrackedDataHandlerRegistry.register(Vec3DataSerializer)
        TrackedDataHandlerRegistry.register(StringSetDataSerializer)
        TrackedDataHandlerRegistry.register(PoseTypeDataSerializer)

        HeldItemProvider.register(CobblemonHeldItemManager, Priority.LOWEST)
    }

    fun initialize() {
        showdownThread.launch()

        CompletableRegistry.allRegistriesCompleted.thenAccept {
            LOGGER.info("All registries loaded.")
        }

        CobblemonPlacementTypes.register()
        CobblemonOrePlacedFeatures.register()

        // Start up the data provider.
        CobblemonDataProvider.registerDefaults()

        SHINY_ASPECT.register()
        GENDER_ASPECT.register()

        SpeciesFeatures.types["choice"] = ChoiceSpeciesFeatureProvider::class.java
        SpeciesFeatures.types["flag"] = FlagSpeciesFeatureProvider::class.java

        SpeciesFeatures.register(
            DataKeys.HAS_BEEN_SHEARED,
            FlagSpeciesFeatureProvider(keys = listOf(DataKeys.HAS_BEEN_SHEARED), default = false))

        CustomPokemonProperty.register(UntradeableProperty)
        CustomPokemonProperty.register(UncatchableProperty)
        CustomPokemonProperty.register(PokemonFlagProperty)
        CustomPokemonProperty.register(HiddenAbilityPropertyType)

        CommandRegistrationEvent.EVENT.register(CobblemonCommands::register)

        ifDedicatedServer {
            isDedicatedServer = true
            TICK_POST.subscribe { ScheduledTaskTracker.update() }
            CobblemonNetwork.clientHandlersRegistered.complete(Unit)
        }

        CobblemonBlocks.completed.thenAccept {
            AxeItemHooks.addStrippable(CobblemonBlocks.APRICORN_LOG.get(), CobblemonBlocks.STRIPPED_APRICORN_LOG.get())
            AxeItemHooks.addStrippable(CobblemonBlocks.APRICORN_WOOD.get(), CobblemonBlocks.STRIPPED_APRICORN_WOOD.get())
        }

        SERVER_STARTED.subscribe { server ->
            playerData = PlayerDataStoreManager().also { it.setup(server) }
            val pokemonStoreRoot = server.getSavePath(WorldSavePath.ROOT).resolve("pokemon").toFile()

            storage.registerFactory(
                priority = Priority.LOWEST,
                factory = FileBackedPokemonStoreFactory(
                    adapter = if (config.storageFormat == "nbt") {
                        NBTStoreAdapter(pokemonStoreRoot.absolutePath, useNestedFolders = true, folderPerClass = true)
                    } else {
                        JSONStoreAdapter(pokemonStoreRoot.absolutePath, useNestedFolders = true, folderPerClass = true)
                    }.with(ReforgedConversion(server.getSavePath(WorldSavePath.ROOT))) as FileStoreAdapter<*>,
                    createIfMissing = true,
                    pcConstructor = { uuid -> PCStore(uuid).also { it.resize(config.defaultBoxCount) } }
                )
            )
        }

        SERVER_STOPPED.subscribe {
            storage.unregisterAll()
            playerData.saveAll()
        }
        SERVER_STARTED.subscribe {
            bestSpawner.onServerStarted()
            battleRegistry.onServerStarted()
        }
        TICK_POST.subscribe { ServerTickHandler.onTick(it) }
        POKEMON_CAPTURED.subscribe { AdvancementHandler.onCapture(it) }
//        EGG_HATCH.subscribe { AdvancementHandler.onHatch(it) }
        BATTLE_VICTORY.subscribe { AdvancementHandler.onWinBattle(it) }
        EVOLUTION_COMPLETE.subscribe(Priority.LOWEST) { event ->
            AdvancementHandler.onEvolve(event)
            val pokemon = event.pokemon
            val ninjaskIdentifier = cobblemonResource("ninjask")
            // Ensure the config option is enabled and that the result was a ninjask and that shedinja exists
            if (this.config.ninjaskCreatesShedinja && pokemon.species.resourceIdentifier == ninjaskIdentifier && PokemonSpecies.getByIdentifier(Pokemon.SHEDINJA) != null) {
                val player = pokemon.getOwnerPlayer() ?: return@subscribe
                if (player.inventory.containsAny { it.item is PokeBallItem }) {
                    player.inventory.removeAmountIf(1) { it.item is PokeBallItem }
                    val properties = event.evolution.result.copy()
                    properties.species = Pokemon.SHEDINJA.toString()
                    val product = pokemon.clone()
                    properties.apply(product)
                    pokemon.storeCoordinates.get()?.store?.add(product)
                }
            }
        }

        PokemonSpecies.observable.subscribe {
            LOGGER.info("Starting dummy Showdown battle to force it to pre-load data.")
            battleRegistry.startBattle(
                BattleFormat.GEN_8_SINGLES,
                BattleSide(PokemonBattleActor(UUID.randomUUID(), BattlePokemon(Pokemon().initialize()), -1F)),
                BattleSide(PokemonBattleActor(UUID.randomUUID(), BattlePokemon(Pokemon().initialize()), -1F))
            ).apply { mute = true }
        }
    }

    fun getLevel(dimension: RegistryKey<World>): World? {
        return if (isDedicatedServer) {
            getServer()?.getWorld(dimension)
        } else {
            val mc = MinecraftClient.getInstance()
            return mc.server?.getWorld(dimension) ?: mc.world
        }
    }

    fun loadConfig() {
        val configFile = File(CONFIG_PATH)
        configFile.parentFile.mkdirs()

        // Check config existence and load if it exists, otherwise create default.
        if (configFile.exists()) {
            try {
                val fileReader = FileReader(configFile)
                this.config = CobblemonConfig.GSON.fromJson(fileReader, CobblemonConfig::class.java)
                fileReader.close()
            } catch (exception: Exception) {
                LOGGER.error("Failed to load the config! Using default config until the following has been addressed:")
                this.config = CobblemonConfig()
                exception.printStackTrace()
            }

            val defaultConfig = CobblemonConfig()

            CobblemonConfig::class.memberProperties.forEach {
                val field = it.javaField!!
                it.isAccessible = true
                field.annotations.forEach {
                    when (it) {
                        is LastChangedVersion -> {
                            val defaultChangedVersion = it.version
                            val lastSavedVersion = config.lastSavedVersion
                            if (defaultChangedVersion.isLaterVersion(lastSavedVersion)) {
                                field.set(config, field.get(defaultConfig))
                            }
                        }
                        is IntConstraint -> {
                            var value = field.get(config)
                            if (value is Int) {
                                value = value.coerceIn(it.min, it.max)
                                field.set(config, value)
                            }
                        }
                    }
                }
            }
        } else {
            this.config = CobblemonConfig()
        }

        config.lastSavedVersion = VERSION
        this.saveConfig()

        bestSpawner.loadConfig()
        PokemonSpecies.observable.subscribe { starterConfig = this.loadStarterConfig() }
    }

    fun loadStarterConfig(): StarterConfig {
        if (config.exportStarterConfig) {
            val file = File("config/cobblemon/starters.json")
            file.parentFile.mkdirs()
            if (!file.exists()) {
                val config = StarterConfig()
                val pw = PrintWriter(file)
                StarterConfig.GSON.toJson(config, pw)
                pw.close()
                return config
            }
            val reader = FileReader(file)
            val config = StarterConfig.GSON.fromJson(reader, StarterConfig::class.java)
            reader.close()
            return config
        } else {
            return StarterConfig()
        }
    }

    fun saveConfig() {
        try {
            val configFile = File(CONFIG_PATH)
            val fileWriter = FileWriter(configFile)
            // Put the config to json then flush the writer to commence writing.
            CobblemonConfig.GSON.toJson(this.config, fileWriter)
            fileWriter.flush()
            fileWriter.close()
        } catch (exception: Exception) {
            LOGGER.error("Failed to save the config! Please consult the following stack trace:")
            exception.printStackTrace()
        }
    }
}
