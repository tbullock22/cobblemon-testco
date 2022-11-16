/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon

import com.caoccao.javet.interop.V8Host
import com.caoccao.javet.interop.V8Runtime
import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.abilities.AbilityPool
import com.cobblemon.mod.common.api.abilities.AbilityTemplate
import com.cobblemon.mod.common.api.ai.SleepDepth
import com.cobblemon.mod.common.api.conditional.RegistryLikeCondition
import com.cobblemon.mod.common.api.data.JsonDataRegistry
import com.cobblemon.mod.common.api.drop.DropEntry
import com.cobblemon.mod.common.api.drop.ItemDropMethod
import com.cobblemon.mod.common.api.entity.EntityDimensionsAdapter
import com.cobblemon.mod.common.api.moves.MoveTemplate
import com.cobblemon.mod.common.api.moves.adapters.MoveTemplateAdapter
import com.cobblemon.mod.common.api.pokemon.effect.ShoulderEffect
import com.cobblemon.mod.common.api.pokemon.effect.adapter.ShoulderEffectAdapter
import com.cobblemon.mod.common.api.pokemon.egg.EggGroup
import com.cobblemon.mod.common.api.pokemon.evolution.Evolution
import com.cobblemon.mod.common.api.pokemon.evolution.PreEvolution
import com.cobblemon.mod.common.api.pokemon.evolution.requirement.EvolutionRequirement
import com.cobblemon.mod.common.api.pokemon.experience.ExperienceGroup
import com.cobblemon.mod.common.api.pokemon.experience.ExperienceGroupAdapter
import com.cobblemon.mod.common.api.pokemon.moves.Learnset
import com.cobblemon.mod.common.api.pokemon.stats.Stat
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.api.spawning.condition.TimeRange
import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.api.types.ElementalTypes
import com.cobblemon.mod.common.api.types.adapters.ElementalTypeAdapter
import com.cobblemon.mod.common.net.messages.client.data.SpeciesRegistrySyncPacket
import com.cobblemon.mod.common.pokemon.FormData
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.pokemon.evolution.adapters.CobblemonEvolutionAdapter
import com.cobblemon.mod.common.pokemon.evolution.adapters.CobblemonPreEvolutionAdapter
import com.cobblemon.mod.common.pokemon.evolution.adapters.CobblemonRequirementAdapter
import com.cobblemon.mod.common.util.adapters.AbilityPoolAdapter
import com.cobblemon.mod.common.util.adapters.AbilityTemplateAdapter
import com.cobblemon.mod.common.util.adapters.BiomeLikeConditionAdapter
import com.cobblemon.mod.common.util.adapters.BlockLikeConditionAdapter
import com.cobblemon.mod.common.util.adapters.BoxAdapter
import com.cobblemon.mod.common.util.adapters.DropEntryAdapter
import com.cobblemon.mod.common.util.adapters.EggGroupAdapter
import com.cobblemon.mod.common.util.adapters.IdentifierAdapter
import com.cobblemon.mod.common.util.adapters.IntRangeAdapter
import com.cobblemon.mod.common.util.adapters.ItemLikeConditionAdapter
import com.cobblemon.mod.common.util.adapters.LazySetAdapter
import com.cobblemon.mod.common.util.adapters.LearnsetAdapter
import com.cobblemon.mod.common.util.adapters.NbtCompoundAdapter
import com.cobblemon.mod.common.util.adapters.TimeRangeAdapter
import com.cobblemon.mod.common.util.adapters.pokemonPropertiesShortAdapter
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.common.collect.HashBasedTable
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.reflect.KProperty
import net.minecraft.block.Block
import net.minecraft.entity.EntityDimensions
import net.minecraft.item.Item
import net.minecraft.nbt.NbtCompound
import net.minecraft.resource.ResourceType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.math.Box
import net.minecraft.world.biome.Biome

object PokemonSpecies : JsonDataRegistry<Species> {

    override val id = cobblemonResource("species")
    override val type = ResourceType.SERVER_DATA

    override val gson: Gson = GsonBuilder()
        .registerTypeAdapter(Stat::class.java, Cobblemon.statProvider.typeAdapter)
        .registerTypeAdapter(ElementalType::class.java, ElementalTypeAdapter)
        .registerTypeAdapter(AbilityTemplate::class.java, AbilityTemplateAdapter)
        .registerTypeAdapter(ShoulderEffect::class.java, ShoulderEffectAdapter)
        .registerTypeAdapter(MoveTemplate::class.java, MoveTemplateAdapter)
        .registerTypeAdapter(ExperienceGroup::class.java, ExperienceGroupAdapter)
        .registerTypeAdapter(EntityDimensions::class.java, EntityDimensionsAdapter)
        .registerTypeAdapter(Learnset::class.java, LearnsetAdapter)
        .registerTypeAdapter(Evolution::class.java, CobblemonEvolutionAdapter)
        .registerTypeAdapter(Box::class.java, BoxAdapter)
        .registerTypeAdapter(AbilityPool::class.java, AbilityPoolAdapter)
        .registerTypeAdapter(EvolutionRequirement::class.java, CobblemonRequirementAdapter)
        .registerTypeAdapter(PreEvolution::class.java, CobblemonPreEvolutionAdapter)
        .registerTypeAdapter(TypeToken.getParameterized(Set::class.java, Evolution::class.java).type, LazySetAdapter(Evolution::class))
        .registerTypeAdapter(IntRange::class.java, IntRangeAdapter)
        .registerTypeAdapter(PokemonProperties::class.java, pokemonPropertiesShortAdapter)
        .registerTypeAdapter(Identifier::class.java, IdentifierAdapter)
        .registerTypeAdapter(TimeRange::class.java, TimeRangeAdapter)
        .registerTypeAdapter(ItemDropMethod::class.java, ItemDropMethod.adapter)
        .registerTypeAdapter(SleepDepth::class.java, SleepDepth.adapter)
        .registerTypeAdapter(DropEntry::class.java, DropEntryAdapter)
        .registerTypeAdapter(NbtCompound::class.java, NbtCompoundAdapter)
        .registerTypeAdapter(TypeToken.getParameterized(RegistryLikeCondition::class.java, Biome::class.java).type, BiomeLikeConditionAdapter)
        .registerTypeAdapter(TypeToken.getParameterized(RegistryLikeCondition::class.java, Block::class.java).type, BlockLikeConditionAdapter)
        .registerTypeAdapter(TypeToken.getParameterized(RegistryLikeCondition::class.java, Item::class.java).type, ItemLikeConditionAdapter)
        .registerTypeAdapter(EggGroup::class.java, EggGroupAdapter)
        .disableHtmlEscaping()
        .enableComplexMapKeySerialization()
        .create()

    override val typeToken: TypeToken<Species> = TypeToken.get(Species::class.java)
    override val resourcePath = "species"

    override val observable = SimpleObservable<PokemonSpecies>()

    private val speciesByIdentifier = hashMapOf<Identifier, Species>()
    private val speciesByDex = HashBasedTable.create<String, Int, Species>()
    private const val DUMMY_SPECIES_KEY = "${Cobblemon.MODID}dummy"
    private const val DUMMY_SPECIES_NAME = "${Cobblemon.MODID}:Dummy"
    private const val DUMMY_ABILITY_DATA = "abilities: { 0: \"No Ability\", 1: \"No Ability\", H: \"No Ability\", S: \"No Ability\" }"
    private const val DUMMY_SINGLES_TIER = "AG"
    private const val DUMMY_DOUBLES_TIER = "DUber"

    val species: Collection<Species>
        get() = this.speciesByIdentifier.values
    val implemented = mutableListOf<Species>()

    object SpeciesByNameDelegate {
        operator fun getValue(_species: PokemonSpecies, property: KProperty<*>) = getByIdentifier(cobblemonResource(property.name.lowercase()))
    }

    /**
     * Finds a species by the pathname of their [Identifier].
     * This method exists for the convenience of finding Cobble default Pokémon.
     * This uses [getByIdentifier] using the [Cobblemon.MODID] as the namespace and the [name] as the path.
     *
     * @param name The path of the species asset.
     * @return The [Species] if existing.
     */
    fun getByName(name: String) = this.getByIdentifier(cobblemonResource(name))

    /**
     * Finds a [Species] by its national Pokédex entry number.
     *
     * @param ndex The [Species.nationalPokedexNumber].
     * @return The [Species] if existing.
     */
    fun getByPokedexNumber(ndex: Int, namespace: String = Cobblemon.MODID) = this.speciesByDex.get(namespace, ndex)

    /**
     * Finds a [Species] by its unique [Identifier].
     *
     * @param identifier The unique [Species.resourceIdentifier] of the [Species].
     * @return The [Species] if existing.
     */
    fun getByIdentifier(identifier: Identifier) = this.speciesByIdentifier[identifier]

    /**
     * Counts the currently loaded species.
     *
     * @return The loaded species amount.
     */
    fun count() = this.speciesByIdentifier.size

    /**
     * Picks a random [Species].
     *
     * @throws [NoSuchElementException] if there are no Pokémon species loaded.
     *
     * @return A randomly selected [Species].
     */
    fun random(): Species = this.implemented.random()

    override fun reload(data: Map<Identifier, Species>) {
        this.speciesByIdentifier.clear()
        this.implemented.clear()
        this.speciesByDex.clear()
        data.forEach { (identifier, species) ->
            species.resourceIdentifier = identifier
            this.speciesByIdentifier.put(identifier, species)?.let { old ->
                this.speciesByDex.remove(old.resourceIdentifier.namespace, old.nationalPokedexNumber)
            }
            this.speciesByDex.put(species.resourceIdentifier.namespace, species.nationalPokedexNumber, species)
            if (species.implemented) {
                this.implemented.add(species)
            }
            species.initialize()
        }
        this.species.forEach(Species::initializePostLoads)
        createShowdownData()
        Cobblemon.LOGGER.info("Loaded {} Pokémon species", this.speciesByIdentifier.size)
        this.observable.emit(this)
    }

    override fun sync(player: ServerPlayerEntity) {
        SpeciesRegistrySyncPacket(species.toList()).sendToPlayer(player)
    }

    private fun createShowdownData() {
        Cobblemon.LOGGER.info("Creating showdown data for species")
        val pokedexDataHolder = StringBuilder()
        val formatsDataHolder = StringBuilder()
        this.createDummySpecies(pokedexDataHolder, formatsDataHolder)
        this.species.toList().forEach { species ->
            try {
                this.createShowdownRepresentation(pokedexDataHolder, species)
                this.createTierRepresentation(formatsDataHolder, species)
            } catch (e: Exception) {
                Cobblemon.LOGGER.error(
                    "Failed to create showdown representation for ${species.resourceIdentifier}, this Pokémon will not be loaded",
                    e
                )
                this.speciesByIdentifier.remove(species.resourceIdentifier)
                this.speciesByDex.remove(species.resourceIdentifier.namespace, species.nationalPokedexNumber)
            }
        }
        V8Host.getNodeInstance().createV8Runtime<V8Runtime>().use { runtime ->
            // Showdown loads mods by reading existing files as such we cannot dynamically add to the Pokédex, instead, we will overwrite the existing file and force a mod reload.
            val pokedexFile = File("node_modules/pokemon-showdown/.data-dist/mods/Cobblemon/pokedex.js")
            Files.createDirectories(pokedexFile.toPath().parent)
            pokedexFile.bufferedWriter().use { writer ->
                writer.write(
                    """
                        "use strict";
                        Object.defineProperty(exports, "__esModule", {value: true});
                        const Pokedex = {
                            $pokedexDataHolder
                        };
                        exports.Pokedex = Pokedex;
                    """.trimIndent()
                )
            }
            val formatsDataFile = File("node_modules/pokemon-showdown/.data-dist/mods/Cobblemon/formats-data.js")
            formatsDataFile.bufferedWriter().use { writer ->
                writer.write(
                    """
                        "use strict";
                        Object.defineProperty(exports, "__esModule", {value: true});
                        const FormatsData = {
                            $formatsDataHolder
                        };
                        exports.FormatsData = FormatsData;
                    """.trimIndent()
                )
            }
            val executor = runtime.getExecutor(
                """
                        const PokemonShowdown = require('pokemon-showdown');
                        PokemonShowdown.Dex.modsLoaded = false;
                        PokemonShowdown.Dex.includeMods();
                    """.trimIndent()
            )
            executor.resourceName = "./node_modules"
            executor.executeVoid()
        }
        Cobblemon.LOGGER.info("Finished creating showdown data for species")
    }

    private fun createDummySpecies(pokedexDataHolder: StringBuilder, formatsDataHolder: StringBuilder) {
        pokedexDataHolder.append("""
            $DUMMY_SPECIES_KEY: {
                num: -1,
                name: "$DUMMY_SPECIES_NAME",
                types: ["${ElementalTypes.NORMAL.name}"],
                gender: "N",
                baseStats: { hp: 1, atk: 1, def: 1, spa: 1, spd: 1, spe: 1 },
                $DUMMY_ABILITY_DATA,
                heightm: 1,
                weightkg: 1,
                color: "White",
                eggGroups: ["${EggGroup.UNDISCOVERED.showdownID}"],
            },
        """.trimIndent())
        formatsDataHolder.append("""
            $DUMMY_SPECIES_KEY: {
		        tier: "$DUMMY_SINGLES_TIER",
                doublesTier: "$DUMMY_DOUBLES_TIER",
            },
        """.trimIndent())
    }

    private fun createShowdownRepresentation(dataHolder: StringBuilder, species: Species) {
        val showdownName = this.createShowdownName(species)
        // ToDo types will need a refresh when we introduce custom typing support
        /**
         * THINGS TO NOTE:
         *
         * Abilities will be sent alongside other Pokémon level battle data, this is done due to showdown being limited to 4 ability slots
         * Evolutions will affect battle mechanics however we do not to specifically say what the result will be, to adapt to the potential result being unknown from a property until it is created we will just assign a fake result.
         *
         */
        dataHolder.append("""
            ${this.createShowdownKey(species)}: {
                num: ${species.nationalPokedexNumber},
                name: "$showdownName",
                ${this.generateTypeDetails(species)},
                ${this.generateGenderDetails(species)},
                ${this.generateBaseStatsDetails(species)},
                $DUMMY_ABILITY_DATA,
                heightm: ${species.height},
                weightkg: ${species.weight},
                ${this.generateEggGroupDetails(species)},
        """.trimIndent())
        species.preEvolution?.let { dataHolder.append("prevo: \"${createShowdownName(it.species, it.form)}\",") }
        if (species.evolutions.isNotEmpty()) {
            dataHolder.append("evos: [${species.evolutions.joinToString(", ") { "\"$DUMMY_SPECIES_NAME\"" }}],")
        }
        if (species.forms.isNotEmpty()) {
            val otherForms = species.forms.joinToString(", ") { "\"${this.createShowdownName(species, it)}\"" }
            dataHolder.append("""
                otherFormes: [$otherForms],
                formeOrder: ["$showdownName", $otherForms],
            """.trimIndent())
        }
        if (species.dynamaxBlocked) {
            dataHolder.append("cannotDynamax: true,")
        }
        dataHolder.append("},")
        species.forms.forEach { form ->
            this.createFormShowdownRepresentation(dataHolder, species, form)
        }
    }

    private fun createTierRepresentation(dataHolder: StringBuilder, species: Species) {
        dataHolder.append("""
            ${this.createShowdownKey(species)}: {
		        tier: "$DUMMY_SINGLES_TIER",
                doublesTier: "$DUMMY_DOUBLES_TIER",
            },
        """.trimIndent())
        species.forms.forEach { form ->
            dataHolder.append("""
                ${this.createShowdownKey(species, form)}: {
                    tier: "$DUMMY_SINGLES_TIER",
                    doublesTier: "$DUMMY_DOUBLES_TIER",
            """.trimIndent())
            if (form.gigantamaxMove != null) {
                dataHolder.append("isNonstandard: \"Gigantamax\",")
            }
            dataHolder.append("},")
        }
    }

    private fun createFormShowdownRepresentation(dataHolder: StringBuilder, species: Species, form: FormData) {
        val showdownName = this.createShowdownName(species, form)
        dataHolder.append("""
            ${this.createShowdownKey(species, form)}: {
                num: ${species.nationalPokedexNumber},
                name: "$showdownName",
                baseSpecies: "${this.createShowdownName(species)}",
                forme: "${form.name}",
                ${this.generateTypeDetails(species, form)},
                ${this.generateGenderDetails(species, form)},
                ${this.generateBaseStatsDetails(species, form)},
                $DUMMY_ABILITY_DATA,
                heightm: ${form.height * .1},
                weightkg: ${form.weight * .1},
                ${this.generateEggGroupDetails(species, form)},
        """.trimIndent())
        form.preEvolution?.let { dataHolder.append("prevo: \"${createShowdownName(it.species, it.form)}\",") }
        if (form.evolutions.isNotEmpty()) {
            dataHolder.append("evos: [${form.evolutions.joinToString(", ") { "\"$DUMMY_SPECIES_NAME\"" }}],")
        }
        if (form.dynamaxBlocked) {
            dataHolder.append("cannotDynamax: true,")
        }
        if (form.gigantamaxMove != null) {
            dataHolder.append("canGigantamax: \"${form.gigantamaxMove.name.replace("_", " ")}\",")
        }
        dataHolder.append("},")
    }

    // Converts a species into a showdown key resulting in '<namespace><path><form-name(if not base)>'
    private fun createShowdownKey(species: Species, form: FormData? = null): String {
        val baseSpeciesKey = species.resourceIdentifier.toString().lowercase().replaceFirst(":", "")
        return "$baseSpeciesKey${if (form == null || form.name.equals(species.standardForm.name, true)) "" else form.name.replace("-", "").lowercase()}"
    }

    // Converts a species into a showdown name resulting in '<namespace>:<species_name>-<form-name(if not base)>'
    private fun createShowdownName(species: Species, form: FormData? = null): String {
        return "${species.resourceIdentifier.namespace}:${species.name}${if (form == null || form.name.equals(species.standardForm.name, true)) "" else "-${form.name}"}"
    }

    private fun generateTypeDetails(species: Species, form: FormData? = null): String {
        val primaryType = form?.primaryType ?: species.primaryType
        val secondaryType = form?.secondaryType ?: species.secondaryType
        return "types: [\"${primaryType.name}\"${if (secondaryType != null) ", \"${secondaryType.name}\"" else ""}]"
    }

    private fun generateGenderDetails(species: Species, form: FormData? = null): String = when (val maleRatio = form?.maleRatio ?: species.maleRatio) {
        0F -> "gender: \"F\""
        1F -> "gender: \"M\""
        -1F, 1.125F -> "gender: \"N\""
        else -> "genderRatio: { M: ${maleRatio}, F: ${1F - maleRatio} }"
    }

    private fun generateBaseStatsDetails(species: Species, form: FormData? = null) = Cobblemon.statProvider.toShowdown(species, form)

    private fun generateEggGroupDetails(species: Species, form: FormData? = null): String {
        val eggGroups = form?.eggGroups ?: species.eggGroups
        return "eggGroups: [${eggGroups.joinToString(", ") { "\"${it.showdownID}\"" }}]"
    }

}