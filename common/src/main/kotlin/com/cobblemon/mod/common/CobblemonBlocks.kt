/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common

import com.cobblemon.mod.common.api.apricorn.Apricorn
import com.cobblemon.mod.common.block.ApricornBlock
import com.cobblemon.mod.common.block.ApricornSaplingBlock
import com.cobblemon.mod.common.block.HealingMachineBlock
import com.cobblemon.mod.common.block.PCBlock
import com.cobblemon.mod.common.mixin.invoker.ButtonBlockInvoker
import com.cobblemon.mod.common.mixin.invoker.DoorBlockInvoker
import com.cobblemon.mod.common.mixin.invoker.PressurePlateBlockInvoker
import com.cobblemon.mod.common.mixin.invoker.StairsBlockInvoker
import com.cobblemon.mod.common.mixin.invoker.TrapdoorBlockInvoker
import com.cobblemon.mod.common.platform.PlatformRegistry
import net.minecraft.block.*
import net.minecraft.entity.EntityType
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.Direction
import net.minecraft.util.math.intprovider.UniformIntProvider

object CobblemonBlocks : PlatformRegistry<Registry<Block>, RegistryKey<Registry<Block>>, Block>() {

    override val registry: Registry<Block> = Registries.BLOCK
    override val registryKey: RegistryKey<Registry<Block>> = RegistryKeys.BLOCK

    val APRICORN_BLOCK_SET_TYPE = BlockSetType("apricorn")
    val APRICORN_WOOD_TYPE = WoodType.register(WoodType("apricorn", APRICORN_BLOCK_SET_TYPE))

    // Evolution Ores
    @JvmField
    val DAWN_STONE_ORE = evolutionStoneOre("dawn_stone_ore")
    @JvmField
    val DUSK_STONE_ORE = evolutionStoneOre("dusk_stone_ore")
    @JvmField
    val FIRE_STONE_ORE = evolutionStoneOre("fire_stone_ore")
    @JvmField
    val ICE_STONE_ORE = evolutionStoneOre("ice_stone_ore")
    @JvmField
    val LEAF_STONE_ORE = evolutionStoneOre("leaf_stone_ore")
    @JvmField
    val MOON_STONE_ORE = evolutionStoneOre("moon_stone_ore")
    @JvmField
    val DRIPSTONE_MOON_STONE_ORE = evolutionStoneOre("dripstone_moon_stone_ore")
    @JvmField
    val SHINY_STONE_ORE = evolutionStoneOre("shiny_stone_ore")
    @JvmField
    val SUN_STONE_ORE = evolutionStoneOre("sun_stone_ore")
    @JvmField
    val THUNDER_STONE_ORE = evolutionStoneOre("thunder_stone_ore")
    @JvmField
    val WATER_STONE_ORE = evolutionStoneOre("water_stone_ore")

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

    // Apricorns
    @JvmField
    val APRICORN_LOG = log("apricorn_log", arg2 = MapColor.BROWN)
    @JvmField
    val STRIPPED_APRICORN_LOG = log("stripped_apricorn_log")
    @JvmField
    val APRICORN_WOOD = log("apricorn_wood")
    @JvmField
    val STRIPPED_APRICORN_WOOD = log("stripped_apricorn_wood")
    @JvmField
    val APRICORN_PLANKS = this.create("apricorn_planks", Block(AbstractBlock.Settings.of(Material.WOOD, MapColor.DIRT_BROWN).strength(2.0f, 3.0f).sounds(BlockSoundGroup.WOOD)))
    @JvmField
    val APRICORN_LEAVES = leaves("apricorn_leaves")
    @JvmField
    val APRICORN_FENCE = this.create("apricorn_fence", FenceBlock(AbstractBlock.Settings.of(Material.WOOD, APRICORN_PLANKS.defaultMapColor).strength(2.0f, 3.0f).sounds(BlockSoundGroup.WOOD)))
    @JvmField
    val APRICORN_FENCE_GATE = this.create("apricorn_fence_gate", FenceGateBlock(AbstractBlock.Settings.of(Material.WOOD, APRICORN_PLANKS.defaultMapColor).strength(2.0f, 3.0f).sounds(BlockSoundGroup.WOOD), APRICORN_WOOD_TYPE))
    @JvmField
    val APRICORN_BUTTON = this.create("apricorn_button", ButtonBlockInvoker.`cobblemon$create`(AbstractBlock.Settings.of(Material.DECORATION).noCollision().strength(0.5f).sounds(BlockSoundGroup.WOOD), APRICORN_BLOCK_SET_TYPE, 30, true))
    @JvmField
    val APRICORN_PRESSURE_PLATE = this.create("apricorn_pressure_plate", PressurePlateBlockInvoker.`cobblemon$create`(PressurePlateBlock.ActivationRule.EVERYTHING, AbstractBlock.Settings.of(Material.WOOD, APRICORN_PLANKS.defaultMapColor).noCollision().strength(0.5f).sounds(BlockSoundGroup.WOOD), APRICORN_BLOCK_SET_TYPE))
    // Tag was removed be sure to add it back when implemented
    //val APRICORN_SIGN = queue("apricorn_sign") { StandingSignBlock(AbstractBlock.Settings.of(Material.WOOD).noCollission().strength(1.0f).sounds(BlockSoundGroup.WOOD), APRICORN_WOOD_TYPE) }
    //val APRICORN_WALL_SIGN = queue("apricorn_wall_sign") { WallSignBlock(AbstractBlock.Settings.of(Material.WOOD).noCollission().strength(1.0f).sounds(BlockSoundGroup.WOOD).dropsLike(APRICORN_SIGN), APRICORN_WOOD_TYPE) }
    @JvmField
    val APRICORN_SLAB = this.create("apricorn_slab", SlabBlock(AbstractBlock.Settings.of(Material.WOOD, MapColor.OAK_TAN).strength(2.0f, 3.0f).sounds(BlockSoundGroup.WOOD)))
    @JvmField
    val APRICORN_STAIRS = this.create("apricorn_stairs", StairsBlockInvoker.`cobblemon$create`(APRICORN_PLANKS.defaultState, AbstractBlock.Settings.copy(APRICORN_PLANKS)))
    @JvmField
    val APRICORN_DOOR = this.create("apricorn_door", DoorBlockInvoker.`cobblemon$create`(AbstractBlock.Settings.of(Material.WOOD, APRICORN_PLANKS.defaultMapColor).strength(3.0F).sounds(BlockSoundGroup.WOOD).nonOpaque(), APRICORN_BLOCK_SET_TYPE))
    @JvmField
    val APRICORN_TRAPDOOR = this.create("apricorn_trapdoor", TrapdoorBlockInvoker.`cobblemon$create`(AbstractBlock.Settings.of(Material.WOOD, MapColor.OAK_TAN).strength(3.0F).sounds(BlockSoundGroup.WOOD).nonOpaque().allowsSpawning { _, _, _, _ -> false }, APRICORN_BLOCK_SET_TYPE))

    private val PLANT_PROPERTIES = AbstractBlock.Settings.of(Material.PLANT).noCollision().ticksRandomly().breakInstantly().sounds(BlockSoundGroup.GRASS)
    @JvmField
    val BLACK_APRICORN_SAPLING = this.create("black_apricorn_sapling", ApricornSaplingBlock(PLANT_PROPERTIES, Apricorn.BLACK))
    @JvmField
    val BLUE_APRICORN_SAPLING = this.create("blue_apricorn_sapling", ApricornSaplingBlock(PLANT_PROPERTIES, Apricorn.BLUE))
    @JvmField
    val GREEN_APRICORN_SAPLING = this.create("green_apricorn_sapling", ApricornSaplingBlock(PLANT_PROPERTIES, Apricorn.GREEN))
    @JvmField
    val PINK_APRICORN_SAPLING = this.create("pink_apricorn_sapling", ApricornSaplingBlock(PLANT_PROPERTIES, Apricorn.PINK))
    @JvmField
    val RED_APRICORN_SAPLING = this.create("red_apricorn_sapling", ApricornSaplingBlock(PLANT_PROPERTIES, Apricorn.RED))
    @JvmField
    val WHITE_APRICORN_SAPLING = this.create("white_apricorn_sapling", ApricornSaplingBlock(PLANT_PROPERTIES, Apricorn.WHITE))
    @JvmField
    val YELLOW_APRICORN_SAPLING = this.create("yellow_apricorn_sapling", ApricornSaplingBlock(PLANT_PROPERTIES, Apricorn.YELLOW))

    @JvmField
    val BLACK_APRICORN = apricornBlock("black_apricorn", Apricorn.BLACK)
    @JvmField
    val BLUE_APRICORN = apricornBlock("blue_apricorn", Apricorn.BLUE)
    @JvmField
    val GREEN_APRICORN = apricornBlock("green_apricorn", Apricorn.GREEN)
    @JvmField
    val PINK_APRICORN = apricornBlock("pink_apricorn", Apricorn.PINK)
    @JvmField
    val RED_APRICORN = apricornBlock("red_apricorn", Apricorn.RED)
    @JvmField
    val WHITE_APRICORN = apricornBlock("white_apricorn", Apricorn.WHITE)
    @JvmField
    val YELLOW_APRICORN = apricornBlock("yellow_apricorn", Apricorn.YELLOW)

//    val HEALING_MACHINE = this.create("healing_machine", HealingMachineBlock(AbstractBlock.Settings.of(Material.METAL, MapColor.IRON_GRAY).sounds(BlockSoundGroup.METAL).strength(2f).nonOpaque().luminance { state: BlockState ->
//        if (state.get(HealingMachineBlock.CHARGE_LEVEL) >= HealingMachineBlock.MAX_CHARGE_LEVEL) 7 else 2
//    }))
//    val PC = this.create("pc", PCBlock(AbstractBlock.Settings.of(Material.METAL, MapColor.IRON_GRAY).sounds(BlockSoundGroup.METAL).strength(2F).nonOpaque().luminance { state: BlockState ->
//        if ((state.get(PCBlock.ON) as Boolean) && (state.get(PCBlock.PART) == PCBlock.PCPart.TOP)) 10 else 0
//    }))
    @JvmField
    val HEALING_MACHINE = create(
        "healing_machine",
        HealingMachineBlock(
            AbstractBlock.Settings
                .of(Material.METAL, MapColor.IRON_GRAY)
                .sounds(BlockSoundGroup.METAL)
                .strength(2f)
                .nonOpaque()
                .luminance { if (it.get(HealingMachineBlock.CHARGE_LEVEL) >= HealingMachineBlock.MAX_CHARGE_LEVEL) 7 else 2 }
        )
    )
    @JvmField
    val PC = create(
        "pc",
        PCBlock(
            AbstractBlock.Settings
                .of(Material.METAL, MapColor.IRON_GRAY)
                .sounds(BlockSoundGroup.METAL)
                .strength(2F)
                .nonOpaque()
                .luminance { if ((it.get(PCBlock.ON) as Boolean) && (it.get(PCBlock.PART) == PCBlock.PCPart.TOP)) 10 else 0 }
        )
    )

    /**
     * Returns a map of all the blocks that can be stripped with an axe in the format of input - output.
     *
     * @return A map of all the blocks that can be stripped with an axe in the format of input - output.
     */
    fun strippedBlocks(): Map<Block, Block> = mapOf(
        APRICORN_WOOD to STRIPPED_APRICORN_WOOD,
        APRICORN_LOG to STRIPPED_APRICORN_LOG
    )

    private fun apricornBlock(name: String, apricorn: Apricorn): ApricornBlock = this.create(name, ApricornBlock(AbstractBlock.Settings.of(Material.PLANT, apricorn.mapColor()).ticksRandomly().strength(Blocks.OAK_LOG.hardness, Blocks.OAK_LOG.blastResistance).sounds(BlockSoundGroup.WOOD).nonOpaque(), apricorn))

    /**
     * Helper method for creating logs
     * copied over from Vanilla
     */
    private fun log(name: String, arg: MapColor = MapColor.DIRT_BROWN, arg2: MapColor = MapColor.DIRT_BROWN): PillarBlock {
        val block = PillarBlock(AbstractBlock.Settings.of(Material.WOOD) { arg3: BlockState ->
            if (arg3.get(PillarBlock.AXIS) === Direction.Axis.Y) arg else arg2
        }.strength(2.0f).sounds(BlockSoundGroup.WOOD))
        return this.create(name, block)
    }

    private fun evolutionStoneOre(name: String) = this.create(name, ExperienceDroppingBlock(AbstractBlock.Settings.copy(Blocks.IRON_ORE), UniformIntProvider.create(1, 2)))

    private fun deepslateEvolutionStoneOre(name: String) = this.create(name, ExperienceDroppingBlock(AbstractBlock.Settings.copy(Blocks.DEEPSLATE_IRON_ORE), UniformIntProvider.create(1, 2)))

    /**
     * Helper method for creating leaves
     * copied over from Vanilla
     */
    private fun leaves(name: String): LeavesBlock {
        val block = LeavesBlock(AbstractBlock.Settings.of(Material.LEAVES).strength(0.2f).ticksRandomly().sounds(BlockSoundGroup.GRASS).nonOpaque()
                .allowsSpawning { _, _, _, type -> type === EntityType.OCELOT || type === EntityType.PARROT }
                .suffocates { _, _, _ -> false }
                .blockVision { _, _, _ -> false })
        return this.create(name, block)
    }
}