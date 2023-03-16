/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common

import com.cobblemon.mod.common.api.apricorn.Apricorn
import com.cobblemon.mod.common.block.*
import com.cobblemon.mod.common.mint.MintType
import com.cobblemon.mod.common.registry.CompletableRegistry
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
    @JvmField
    val DAWN_STONE_ORE = this.evolutionStoneOre("dawn_stone_ore")
    @JvmField
    val DUSK_STONE_ORE = this.evolutionStoneOre("dusk_stone_ore")
    @JvmField
    val FIRE_STONE_ORE = this.evolutionStoneOre("fire_stone_ore")
    @JvmField
    val ICE_STONE_ORE = this.evolutionStoneOre("ice_stone_ore")
    @JvmField
    val LEAF_STONE_ORE = this.evolutionStoneOre("leaf_stone_ore")
    @JvmField
    val MOON_STONE_ORE = this.evolutionStoneOre("moon_stone_ore")
    @JvmField
    val DRIPSTONE_MOON_STONE_ORE = this.evolutionStoneOre("dripstone_moon_stone_ore")
    @JvmField
    val SHINY_STONE_ORE = this.evolutionStoneOre("shiny_stone_ore")
    @JvmField
    val SUN_STONE_ORE = this.evolutionStoneOre("sun_stone_ore")
    @JvmField
    val THUNDER_STONE_ORE = this.evolutionStoneOre("thunder_stone_ore")
    @JvmField
    val WATER_STONE_ORE = this.evolutionStoneOre("water_stone_ore")

    /**
     * Deepslate separator
     */

    @JvmField
    val DEEPSLATE_DAWN_STONE_ORE = this.deepslateEvolutionStoneOre("deepslate_dawn_stone_ore")
    @JvmField
    val DEEPSLATE_DUSK_STONE_ORE = this.deepslateEvolutionStoneOre("deepslate_dusk_stone_ore")
    @JvmField
    val DEEPSLATE_FIRE_STONE_ORE = this.deepslateEvolutionStoneOre("deepslate_fire_stone_ore")
    @JvmField
    val DEEPSLATE_ICE_STONE_ORE = this.deepslateEvolutionStoneOre("deepslate_ice_stone_ore")
    @JvmField
    val DEEPSLATE_LEAF_STONE_ORE = this.deepslateEvolutionStoneOre("deepslate_leaf_stone_ore")
    @JvmField
    val DEEPSLATE_MOON_STONE_ORE = this.deepslateEvolutionStoneOre("deepslate_moon_stone_ore")
    @JvmField
    val DEEPSLATE_SHINY_STONE_ORE = this.deepslateEvolutionStoneOre("deepslate_shiny_stone_ore")
    @JvmField
    val DEEPSLATE_SUN_STONE_ORE = this.deepslateEvolutionStoneOre("deepslate_sun_stone_ore")
    @JvmField
    val DEEPSLATE_THUNDER_STONE_ORE = this.deepslateEvolutionStoneOre("deepslate_thunder_stone_ore")
    @JvmField
    val DEEPSLATE_WATER_STONE_ORE = this.deepslateEvolutionStoneOre("deepslate_water_stone_ore")

    /**
     * Apricorns
     */

    @JvmField
    val APRICORN_LOG = queue("apricorn_log") {
        log(
            MapColor.DIRT_BROWN,
            MapColor.BROWN
        )
    }
    @JvmField
    val STRIPPED_APRICORN_LOG = queue("stripped_apricorn_log") {
        log(
            MapColor.DIRT_BROWN,
            MapColor.DIRT_BROWN
        )
    }
    @JvmField
    val APRICORN_WOOD = queue("apricorn_wood") {
        log(
            MapColor.DIRT_BROWN,
            MapColor.DIRT_BROWN
        )
    }
    @JvmField
    val STRIPPED_APRICORN_WOOD = queue("stripped_apricorn_wood") {
        log(
            MapColor.DIRT_BROWN,
            MapColor.DIRT_BROWN
        )
    }
    @JvmField
    val APRICORN_PLANKS = queue("apricorn_planks") { Block(AbstractBlock.Settings.of(Material.WOOD, MapColor.DIRT_BROWN).strength(2.0f, 3.0f).sounds(BlockSoundGroup.WOOD)) }
    @JvmField
    val APRICORN_LEAVES = queue("apricorn_leaves") { leaves(BlockSoundGroup.GRASS) }
    @JvmField
    val APRICORN_FENCE = queue("apricorn_fence") { FenceBlock(AbstractBlock.Settings.of(Material.WOOD, APRICORN_PLANKS.get().defaultMapColor).strength(2.0f, 3.0f).sounds(BlockSoundGroup.WOOD)) }
    @JvmField
    val APRICORN_FENCE_GATE = queue("apricorn_fence_gate") { FenceGateBlock(AbstractBlock.Settings.of(Material.WOOD, APRICORN_PLANKS.get().defaultMapColor).strength(2.0f, 3.0f).sounds(BlockSoundGroup.WOOD)) }
    @JvmField
    val APRICORN_BUTTON = queue("apricorn_button") { WoodenButtonBlock(AbstractBlock.Settings.of(Material.DECORATION).noCollision().strength(0.5f).sounds(BlockSoundGroup.WOOD)) }
    @JvmField
    val APRICORN_PRESSURE_PLATE = queue("apricorn_pressure_plate") { PressurePlateBlock(PressurePlateBlock.ActivationRule.EVERYTHING, AbstractBlock.Settings.of(Material.WOOD, APRICORN_PLANKS.get().defaultMapColor).noCollision().strength(0.5f).sounds(BlockSoundGroup.WOOD)) }
    // Tag was removed be sure to add it back when implemented
//    @JvmField
//    val APRICORN_SIGN = queue("apricorn_sign") { StandingSignBlock(AbstractBlock.Settings.of(Material.WOOD).noCollission().strength(1.0f).sounds(BlockSoundGroup.WOOD), APRICORN_WOOD_TYPE) }
    //@JvmField
//    val APRICORN_WALL_SIGN = queue("apricorn_wall_sign") { WallSignBlock(AbstractBlock.Settings.of(Material.WOOD).noCollission().strength(1.0f).sounds(BlockSoundGroup.WOOD).dropsLike(APRICORN_SIGN), APRICORN_WOOD_TYPE) }
    @JvmField
    val APRICORN_SLAB = queue("apricorn_slab") { SlabBlock(AbstractBlock.Settings.of(Material.WOOD, MapColor.OAK_TAN).strength(2.0f, 3.0f).sounds(BlockSoundGroup.WOOD)) }
    @JvmField
    val APRICORN_STAIRS = queue("apricorn_stairs") { StairsBlock(
        APRICORN_PLANKS.get().defaultState, AbstractBlock.Settings.copy(
            APRICORN_PLANKS.get())) }
    @JvmField
    val APRICORN_DOOR = queue("apricorn_door") { DoorBlock(AbstractBlock.Settings.of(Material.WOOD, APRICORN_PLANKS.get().defaultMapColor).strength(3.0F).sounds(BlockSoundGroup.WOOD).nonOpaque()) }
    @JvmField
    val APRICORN_TRAPDOOR = queue("apricorn_trapdoor") { TrapdoorBlock(AbstractBlock.Settings.of(Material.WOOD, MapColor.OAK_TAN).strength(3.0F).sounds(BlockSoundGroup.WOOD).nonOpaque().allowsSpawning { _, _, _, _ -> false }) }

    private val PLANT_PROPERTIES = AbstractBlock.Settings.of(Material.PLANT).noCollision().ticksRandomly().breakInstantly().sounds(BlockSoundGroup.GRASS)
    @JvmField
    val BLACK_APRICORN_SAPLING = queue("black_apricorn_sapling") { ApricornSaplingBlock(PLANT_PROPERTIES, Apricorn.BLACK) }
    @JvmField
    val BLUE_APRICORN_SAPLING = queue("blue_apricorn_sapling") { ApricornSaplingBlock(PLANT_PROPERTIES, Apricorn.BLUE) }
    @JvmField
    val GREEN_APRICORN_SAPLING = queue("green_apricorn_sapling") { ApricornSaplingBlock(PLANT_PROPERTIES, Apricorn.GREEN) }
    @JvmField
    val PINK_APRICORN_SAPLING = queue("pink_apricorn_sapling") { ApricornSaplingBlock(PLANT_PROPERTIES, Apricorn.PINK) }
    @JvmField
    val RED_APRICORN_SAPLING = queue("red_apricorn_sapling") { ApricornSaplingBlock(PLANT_PROPERTIES, Apricorn.RED) }
    @JvmField
    val WHITE_APRICORN_SAPLING = queue("white_apricorn_sapling") { ApricornSaplingBlock(PLANT_PROPERTIES, Apricorn.WHITE) }
    @JvmField
    val YELLOW_APRICORN_SAPLING = queue("yellow_apricorn_sapling") { ApricornSaplingBlock(PLANT_PROPERTIES, Apricorn.YELLOW) }

    @JvmField
    val BLACK_APRICORN = registerApricornBlock("black_apricorn", Apricorn.BLACK)
    @JvmField
    val BLUE_APRICORN = registerApricornBlock("blue_apricorn", Apricorn.BLUE)
    @JvmField
    val GREEN_APRICORN = registerApricornBlock("green_apricorn", Apricorn.GREEN)
    @JvmField
    val PINK_APRICORN = registerApricornBlock("pink_apricorn", Apricorn.PINK)
    @JvmField
    val RED_APRICORN = registerApricornBlock("red_apricorn", Apricorn.RED)
    @JvmField
    val WHITE_APRICORN = registerApricornBlock("white_apricorn", Apricorn.WHITE)
    @JvmField
    val YELLOW_APRICORN = registerApricornBlock("yellow_apricorn", Apricorn.YELLOW)

    @JvmField
    val HEALING_MACHINE = queue("healing_machine") { HealingMachineBlock(AbstractBlock.Settings.of(Material.METAL, MapColor.IRON_GRAY).sounds(BlockSoundGroup.METAL).strength(2f).nonOpaque().luminance(ToIntFunction { state: BlockState -> if (state.get(HealingMachineBlock.CHARGE_LEVEL) >= HealingMachineBlock.MAX_CHARGE_LEVEL) 7 else 2 })) }
    @JvmField
    val PC = queue("pc") { PCBlock(AbstractBlock.Settings.of(Material.METAL, MapColor.IRON_GRAY).sounds(BlockSoundGroup.METAL).strength(2F).nonOpaque().luminance(ToIntFunction { state: BlockState -> if ((state.get(
            PCBlock.ON) as Boolean) && (state.get(PCBlock.PART) == PCBlock.PCPart.TOP)) 10 else 0 })) }

    val RED_MINT = queue("red_mint") { MintBlock(MintType.RED, AbstractBlock.Settings.of(Material.PLANT).noCollision().ticksRandomly().breakInstantly().sounds(BlockSoundGroup.CROP)) }
    val BLUE_MINT = queue("blue_mint") { MintBlock(MintType.BLUE, AbstractBlock.Settings.of(Material.PLANT).noCollision().ticksRandomly().breakInstantly().sounds(BlockSoundGroup.CROP)) }
    val CYAN_MINT = queue("cyan_mint") { MintBlock(MintType.CYAN, AbstractBlock.Settings.of(Material.PLANT).noCollision().ticksRandomly().breakInstantly().sounds(BlockSoundGroup.CROP)) }
    val PINK_MINT = queue("pink_mint") { MintBlock(MintType.PINK, AbstractBlock.Settings.of(Material.PLANT).noCollision().ticksRandomly().breakInstantly().sounds(BlockSoundGroup.CROP)) }
    val GREEN_MINT = queue("green_mint") { MintBlock(MintType.GREEN, AbstractBlock.Settings.of(Material.PLANT).noCollision().ticksRandomly().breakInstantly().sounds(BlockSoundGroup.CROP)) }
    val WHITE_MINT = queue("white_mint") { MintBlock(MintType.WHITE, AbstractBlock.Settings.of(Material.PLANT).noCollision().ticksRandomly().breakInstantly().sounds(BlockSoundGroup.CROP)) }

    private fun registerApricornBlock(id: String, apricorn: Apricorn): RegistrySupplier<ApricornBlock> {
        return queue(id) { ApricornBlock(AbstractBlock.Settings.of(Material.PLANT, apricorn.mapColor()).ticksRandomly().strength(Blocks.OAK_LOG.hardness, Blocks.OAK_LOG.blastResistance).sounds(BlockSoundGroup.WOOD).nonOpaque(), apricorn) }
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