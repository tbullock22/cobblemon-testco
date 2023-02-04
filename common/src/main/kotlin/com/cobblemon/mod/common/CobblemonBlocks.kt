/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common

import com.cobblemon.mod.common.api.apricorn.Apricorn
import com.cobblemon.mod.common.registry.CompletableRegistry
import com.cobblemon.mod.common.block.ApricornBlock
import com.cobblemon.mod.common.block.ApricornSaplingBlock
import com.cobblemon.mod.common.block.HealingMachineBlock
import com.cobblemon.mod.common.block.PCBlock
import dev.architectury.registry.registries.RegistrySupplier
import net.minecraft.block.*
import net.minecraft.entity.EntityType
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.util.math.Direction
import net.minecraft.util.math.intprovider.UniformIntProvider
import net.minecraft.util.registry.Registry
import java.util.function.ToIntFunction

object CobblemonBlocks : CompletableRegistry<Block>(Registry.BLOCK_KEY) {
    /**
     * Evolution Ores
     */

    val DAWN_STONE_ORE = this.evolutionStoneOre("dawn_stone_ore")
    val DUSK_STONE_ORE = this.evolutionStoneOre("dusk_stone_ore")
    val FIRE_STONE_ORE = this.evolutionStoneOre("fire_stone_ore")
    val ICE_STONE_ORE = this.evolutionStoneOre("ice_stone_ore")
    val LEAF_STONE_ORE = this.evolutionStoneOre("leaf_stone_ore")
    val MOON_STONE_ORE = this.evolutionStoneOre("moon_stone_ore")
    val DRIPSTONE_MOON_STONE_ORE = this.evolutionStoneOre("dripstone_moon_stone_ore")
    val SHINY_STONE_ORE = this.evolutionStoneOre("shiny_stone_ore")
    val SUN_STONE_ORE = this.evolutionStoneOre("sun_stone_ore")
    val THUNDER_STONE_ORE = this.evolutionStoneOre("thunder_stone_ore")
    val WATER_STONE_ORE = this.evolutionStoneOre("water_stone_ore")

    /**
     * Deepslate separator
     */

    val DEEPSLATE_DAWN_STONE_ORE = this.deepslateEvolutionStoneOre("deepslate_dawn_stone_ore")
    val DEEPSLATE_DUSK_STONE_ORE = this.deepslateEvolutionStoneOre("deepslate_dusk_stone_ore")
    val DEEPSLATE_FIRE_STONE_ORE = this.deepslateEvolutionStoneOre("deepslate_fire_stone_ore")
    val DEEPSLATE_ICE_STONE_ORE = this.deepslateEvolutionStoneOre("deepslate_ice_stone_ore")
    val DEEPSLATE_LEAF_STONE_ORE = this.deepslateEvolutionStoneOre("deepslate_leaf_stone_ore")
    val DEEPSLATE_MOON_STONE_ORE = this.deepslateEvolutionStoneOre("deepslate_moon_stone_ore")
    val DEEPSLATE_SHINY_STONE_ORE = this.deepslateEvolutionStoneOre("deepslate_shiny_stone_ore")
    val DEEPSLATE_SUN_STONE_ORE = this.deepslateEvolutionStoneOre("deepslate_sun_stone_ore")
    val DEEPSLATE_THUNDER_STONE_ORE = this.deepslateEvolutionStoneOre("deepslate_thunder_stone_ore")
    val DEEPSLATE_WATER_STONE_ORE = this.deepslateEvolutionStoneOre("deepslate_water_stone_ore")

    /**
     * Apricorns
     */

    val APRICORN_LOG = queue("apricorn_log") {
        log(
            MapColor.DIRT_BROWN,
            MapColor.BROWN
        )
    }
    val STRIPPED_APRICORN_LOG = queue("stripped_apricorn_log") {
        log(
            MapColor.DIRT_BROWN,
            MapColor.DIRT_BROWN
        )
    }
    val APRICORN_WOOD = queue("apricorn_wood") {
        log(
            MapColor.DIRT_BROWN,
            MapColor.DIRT_BROWN
        )
    }
    val STRIPPED_APRICORN_WOOD = queue("stripped_apricorn_wood") {
        log(
            MapColor.DIRT_BROWN,
            MapColor.DIRT_BROWN
        )
    }
    val APRICORN_PLANKS = queue("apricorn_planks") { Block(AbstractBlock.Settings.of(Material.WOOD, MapColor.DIRT_BROWN).strength(2.0f, 3.0f).sounds(BlockSoundGroup.WOOD)) }
    val APRICORN_LEAVES = queue("apricorn_leaves") { com.cobblemon.mod.common.CobblemonBlocks.leaves(BlockSoundGroup.GRASS) }
    val APRICORN_FENCE = queue("apricorn_fence") { FenceBlock(AbstractBlock.Settings.of(Material.WOOD, APRICORN_PLANKS.get().defaultMapColor).strength(2.0f, 3.0f).sounds(BlockSoundGroup.WOOD)) }
    val APRICORN_FENCE_GATE = queue("apricorn_fence_gate") { FenceGateBlock(AbstractBlock.Settings.of(Material.WOOD, APRICORN_PLANKS.get().defaultMapColor).strength(2.0f, 3.0f).sounds(BlockSoundGroup.WOOD)) }
    val APRICORN_BUTTON = queue("apricorn_button") { WoodenButtonBlock(AbstractBlock.Settings.of(Material.DECORATION).noCollision().strength(0.5f).sounds(BlockSoundGroup.WOOD)) }
    val APRICORN_PRESSURE_PLATE = queue("apricorn_pressure_plate") { PressurePlateBlock(PressurePlateBlock.ActivationRule.EVERYTHING, AbstractBlock.Settings.of(Material.WOOD, APRICORN_PLANKS.get().defaultMapColor).noCollision().strength(0.5f).sounds(BlockSoundGroup.WOOD)) }
    // Tag was removed be sure to add it back when implemented
    //val APRICORN_SIGN = queue("apricorn_sign") { StandingSignBlock(AbstractBlock.Settings.of(Material.WOOD).noCollission().strength(1.0f).sounds(BlockSoundGroup.WOOD), APRICORN_WOOD_TYPE) }
    //val APRICORN_WALL_SIGN = queue("apricorn_wall_sign") { WallSignBlock(AbstractBlock.Settings.of(Material.WOOD).noCollission().strength(1.0f).sounds(BlockSoundGroup.WOOD).dropsLike(APRICORN_SIGN), APRICORN_WOOD_TYPE) }
    val APRICORN_SLAB = queue("apricorn_slab") { SlabBlock(AbstractBlock.Settings.of(Material.WOOD, MapColor.OAK_TAN).strength(2.0f, 3.0f).sounds(BlockSoundGroup.WOOD)) }
    val APRICORN_STAIRS = queue("apricorn_stairs") { StairsBlock(
        APRICORN_PLANKS.get().defaultState, AbstractBlock.Settings.copy(
            APRICORN_PLANKS.get())) }
    val APRICORN_DOOR = queue("apricorn_door") { DoorBlock(AbstractBlock.Settings.of(Material.WOOD, APRICORN_PLANKS.get().defaultMapColor).strength(3.0F).sounds(BlockSoundGroup.WOOD).nonOpaque()) }
    val APRICORN_TRAPDOOR = queue("apricorn_trapdoor") { TrapdoorBlock(AbstractBlock.Settings.of(Material.WOOD, MapColor.OAK_TAN).strength(3.0F).sounds(BlockSoundGroup.WOOD).nonOpaque().allowsSpawning { _, _, _, _ -> false }) }

    private val PLANT_PROPERTIES = AbstractBlock.Settings.of(Material.PLANT).noCollision().ticksRandomly().breakInstantly().sounds(BlockSoundGroup.GRASS)
    val BLACK_APRICORN_SAPLING = queue("black_apricorn_sapling") { ApricornSaplingBlock(PLANT_PROPERTIES, Apricorn.BLACK) }
    val BLUE_APRICORN_SAPLING = queue("blue_apricorn_sapling") { ApricornSaplingBlock(PLANT_PROPERTIES, Apricorn.BLUE) }
    val GREEN_APRICORN_SAPLING = queue("green_apricorn_sapling") { ApricornSaplingBlock(PLANT_PROPERTIES, Apricorn.GREEN) }
    val PINK_APRICORN_SAPLING = queue("pink_apricorn_sapling") { ApricornSaplingBlock(PLANT_PROPERTIES, Apricorn.PINK) }
    val RED_APRICORN_SAPLING = queue("red_apricorn_sapling") { ApricornSaplingBlock(PLANT_PROPERTIES, Apricorn.RED) }
    val WHITE_APRICORN_SAPLING = queue("white_apricorn_sapling") { ApricornSaplingBlock(PLANT_PROPERTIES, Apricorn.WHITE) }
    val YELLOW_APRICORN_SAPLING = queue("yellow_apricorn_sapling") { ApricornSaplingBlock(PLANT_PROPERTIES, Apricorn.YELLOW) }

    val BLACK_APRICORN = registerApricornBlock("black_apricorn", Apricorn.BLACK)
    val BLUE_APRICORN = registerApricornBlock("blue_apricorn", Apricorn.BLUE)
    val GREEN_APRICORN = registerApricornBlock("green_apricorn", Apricorn.GREEN)
    val PINK_APRICORN = registerApricornBlock("pink_apricorn", Apricorn.PINK)
    val RED_APRICORN = registerApricornBlock("red_apricorn", Apricorn.RED)
    val WHITE_APRICORN = registerApricornBlock("white_apricorn", Apricorn.WHITE)
    val YELLOW_APRICORN = registerApricornBlock("yellow_apricorn", Apricorn.YELLOW)

    val HEALING_MACHINE = queue("healing_machine") { HealingMachineBlock(AbstractBlock.Settings.of(Material.METAL, MapColor.IRON_GRAY).sounds(BlockSoundGroup.METAL).strength(2f).nonOpaque().luminance(ToIntFunction { state: BlockState -> if (state.get(HealingMachineBlock.CHARGE_LEVEL) >= HealingMachineBlock.MAX_CHARGE_LEVEL) 7 else 2 })) }
    val PC = queue("pc") { PCBlock(AbstractBlock.Settings.of(Material.METAL, MapColor.IRON_GRAY).sounds(BlockSoundGroup.METAL).strength(2F).nonOpaque().luminance(ToIntFunction { state: BlockState -> if ((state.get(
            PCBlock.ON) as Boolean) && (state.get(PCBlock.PART) == PCBlock.PCPart.TOP)) 10 else 0 })) }

    private fun registerApricornBlock(id: String, apricorn: Apricorn): RegistrySupplier<ApricornBlock> {
        return queue(id) { ApricornBlock(AbstractBlock.Settings.of(Material.PLANT).ticksRandomly().strength(0.2f, 3.0f).sounds(BlockSoundGroup.WOOD).nonOpaque(), apricorn) }
    }

    /**
     * Helper method for creating logs
     * copied over from Vanilla
     */
    private fun log(arg: MapColor, arg2: MapColor): PillarBlock {
        return PillarBlock(AbstractBlock.Settings.of(Material.WOOD) { arg3: BlockState ->
            if (arg3.get(PillarBlock.AXIS) === Direction.Axis.Y) arg else arg2
        }.strength(2.0f).sounds(BlockSoundGroup.WOOD))
    }

    private fun evolutionStoneOre(name: String) = this.queue(name) { OreBlock(AbstractBlock.Settings.copy(Blocks.IRON_ORE), UniformIntProvider.create(1, 2)) }

    private fun deepslateEvolutionStoneOre(name: String) = this.queue(name) { OreBlock(AbstractBlock.Settings.copy(Blocks.DEEPSLATE_IRON_ORE), UniformIntProvider.create(1, 2)) }

    /**
     * Helper method for creating leaves
     * copied over from Vanilla
     */
    private fun leaves(sound: BlockSoundGroup): LeavesBlock {
        return LeavesBlock(
            AbstractBlock.Settings.of(Material.LEAVES).strength(0.2f).ticksRandomly().sounds(sound).nonOpaque()
                .allowsSpawning { _, _, _, type -> type === EntityType.OCELOT || type === EntityType.PARROT }
                .suffocates { _, _, _ -> false }
                .blockVision { _, _, _ -> false })
    }
}