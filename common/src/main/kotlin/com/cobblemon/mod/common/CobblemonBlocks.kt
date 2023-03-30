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
import net.minecraft.block.AbstractBlock
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.ExperienceDroppingBlock
import net.minecraft.block.FenceBlock
import net.minecraft.block.FenceGateBlock
import net.minecraft.block.FireBlock
import net.minecraft.block.LeavesBlock
import net.minecraft.block.MapColor
import net.minecraft.block.Material
import net.minecraft.block.PillarBlock
import net.minecraft.block.PressurePlateBlock
import net.minecraft.block.SlabBlock
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

    // Evolution Ores
    val DAWN_STONE_ORE = evolutionStoneOre("dawn_stone_ore")
    val DUSK_STONE_ORE = evolutionStoneOre("dusk_stone_ore")
    val FIRE_STONE_ORE = evolutionStoneOre("fire_stone_ore")
    val ICE_STONE_ORE = evolutionStoneOre("ice_stone_ore")
    val LEAF_STONE_ORE = evolutionStoneOre("leaf_stone_ore")
    val MOON_STONE_ORE = evolutionStoneOre("moon_stone_ore")
    val DRIPSTONE_MOON_STONE_ORE = evolutionStoneOre("dripstone_moon_stone_ore")
    val SHINY_STONE_ORE = evolutionStoneOre("shiny_stone_ore")
    val SUN_STONE_ORE = evolutionStoneOre("sun_stone_ore")
    val THUNDER_STONE_ORE = evolutionStoneOre("thunder_stone_ore")
    val WATER_STONE_ORE = evolutionStoneOre("water_stone_ore")

    // Deepslate Ores
    val DEEPSLATE_DAWN_STONE_ORE = deepslateEvolutionStoneOre("deepslate_dawn_stone_ore")
    val DEEPSLATE_DUSK_STONE_ORE = deepslateEvolutionStoneOre("deepslate_dusk_stone_ore")
    val DEEPSLATE_FIRE_STONE_ORE = deepslateEvolutionStoneOre("deepslate_fire_stone_ore")
    val DEEPSLATE_ICE_STONE_ORE = deepslateEvolutionStoneOre("deepslate_ice_stone_ore")
    val DEEPSLATE_LEAF_STONE_ORE = deepslateEvolutionStoneOre("deepslate_leaf_stone_ore")
    val DEEPSLATE_MOON_STONE_ORE = deepslateEvolutionStoneOre("deepslate_moon_stone_ore")
    val DEEPSLATE_SHINY_STONE_ORE = deepslateEvolutionStoneOre("deepslate_shiny_stone_ore")
    val DEEPSLATE_SUN_STONE_ORE = deepslateEvolutionStoneOre("deepslate_sun_stone_ore")
    val DEEPSLATE_THUNDER_STONE_ORE = deepslateEvolutionStoneOre("deepslate_thunder_stone_ore")
    val DEEPSLATE_WATER_STONE_ORE = deepslateEvolutionStoneOre("deepslate_water_stone_ore")

    // Apricorns
    val APRICORN_LOG = log("apricorn_log", arg2 = MapColor.BROWN)
    val STRIPPED_APRICORN_LOG = log("stripped_apricorn_log")
    val APRICORN_WOOD = log("apricorn_wood")
    val STRIPPED_APRICORN_WOOD = log("stripped_apricorn_wood")
    val APRICORN_PLANKS = this.create("apricorn_planks", Block(AbstractBlock.Settings.of(Material.WOOD, MapColor.DIRT_BROWN).strength(2.0f, 3.0f).sounds(BlockSoundGroup.WOOD)))
    val APRICORN_LEAVES = leaves("apricorn_leaves")
    val APRICORN_FENCE = this.create("apricorn_fence", FenceBlock(AbstractBlock.Settings.of(Material.WOOD, APRICORN_PLANKS.defaultMapColor).strength(2.0f, 3.0f).sounds(BlockSoundGroup.WOOD)))
    val APRICORN_FENCE_GATE = this.create("apricorn_fence_gate", FenceGateBlock(AbstractBlock.Settings.of(Material.WOOD, APRICORN_PLANKS.defaultMapColor).strength(2.0f, 3.0f).sounds(BlockSoundGroup.WOOD), SoundEvents.BLOCK_FENCE_GATE_CLOSE, SoundEvents.BLOCK_FENCE_GATE_OPEN))
    val APRICORN_BUTTON = this.create("apricorn_button", ButtonBlockInvoker.`cobblemon$create`(AbstractBlock.Settings.of(Material.DECORATION).noCollision().strength(0.5f).sounds(BlockSoundGroup.WOOD), 30, true, SoundEvents.BLOCK_WOODEN_BUTTON_CLICK_OFF, SoundEvents.BLOCK_WOODEN_BUTTON_CLICK_ON))
    val APRICORN_PRESSURE_PLATE = this.create("apricorn_pressure_plate", PressurePlateBlockInvoker.`cobblemon$create`(PressurePlateBlock.ActivationRule.EVERYTHING, AbstractBlock.Settings.of(Material.WOOD, APRICORN_PLANKS.defaultMapColor).noCollision().strength(0.5f).sounds(BlockSoundGroup.WOOD), SoundEvents.BLOCK_WOODEN_PRESSURE_PLATE_CLICK_OFF, SoundEvents.BLOCK_WOODEN_PRESSURE_PLATE_CLICK_ON))
    // Tag was removed be sure to add it back when implemented
    //val APRICORN_SIGN = queue("apricorn_sign") { StandingSignBlock(AbstractBlock.Settings.of(Material.WOOD).noCollission().strength(1.0f).sounds(BlockSoundGroup.WOOD), APRICORN_WOOD_TYPE) }
    //val APRICORN_WALL_SIGN = queue("apricorn_wall_sign") { WallSignBlock(AbstractBlock.Settings.of(Material.WOOD).noCollission().strength(1.0f).sounds(BlockSoundGroup.WOOD).dropsLike(APRICORN_SIGN), APRICORN_WOOD_TYPE) }
    val APRICORN_SLAB = this.create("apricorn_slab", SlabBlock(AbstractBlock.Settings.of(Material.WOOD, MapColor.OAK_TAN).strength(2.0f, 3.0f).sounds(BlockSoundGroup.WOOD)))
    val APRICORN_STAIRS = this.create("apricorn_stairs", StairsBlockInvoker.`cobblemon$create`(APRICORN_PLANKS.defaultState, AbstractBlock.Settings.copy(APRICORN_PLANKS)))
    val APRICORN_DOOR = this.create("apricorn_door", DoorBlockInvoker.`cobblemon$create`(AbstractBlock.Settings.of(Material.WOOD, APRICORN_PLANKS.defaultMapColor).strength(3.0F).sounds(BlockSoundGroup.WOOD).nonOpaque(), SoundEvents.BLOCK_WOODEN_DOOR_CLOSE, SoundEvents.BLOCK_WOODEN_DOOR_OPEN))
    val APRICORN_TRAPDOOR = this.create("apricorn_trapdoor", TrapdoorBlockInvoker.`cobblemon$create`(AbstractBlock.Settings.of(Material.WOOD, MapColor.OAK_TAN).strength(3.0F).sounds(BlockSoundGroup.WOOD).nonOpaque().allowsSpawning { _, _, _, _ -> false }, SoundEvents.BLOCK_WOODEN_TRAPDOOR_CLOSE, SoundEvents.BLOCK_WOODEN_TRAPDOOR_OPEN))

    private val PLANT_PROPERTIES = AbstractBlock.Settings.of(Material.PLANT).noCollision().ticksRandomly().breakInstantly().sounds(BlockSoundGroup.GRASS)
    val BLACK_APRICORN_SAPLING = this.create("black_apricorn_sapling", ApricornSaplingBlock(PLANT_PROPERTIES, Apricorn.BLACK))
    val BLUE_APRICORN_SAPLING = this.create("blue_apricorn_sapling", ApricornSaplingBlock(PLANT_PROPERTIES, Apricorn.BLUE))
    val GREEN_APRICORN_SAPLING = this.create("green_apricorn_sapling", ApricornSaplingBlock(PLANT_PROPERTIES, Apricorn.GREEN))
    val PINK_APRICORN_SAPLING = this.create("pink_apricorn_sapling", ApricornSaplingBlock(PLANT_PROPERTIES, Apricorn.PINK))
    val RED_APRICORN_SAPLING = this.create("red_apricorn_sapling", ApricornSaplingBlock(PLANT_PROPERTIES, Apricorn.RED))
    val WHITE_APRICORN_SAPLING = this.create("white_apricorn_sapling", ApricornSaplingBlock(PLANT_PROPERTIES, Apricorn.WHITE))
    val YELLOW_APRICORN_SAPLING = this.create("yellow_apricorn_sapling", ApricornSaplingBlock(PLANT_PROPERTIES, Apricorn.YELLOW))

    val BLACK_APRICORN = apricornBlock("black_apricorn", Apricorn.BLACK)
    val BLUE_APRICORN = apricornBlock("blue_apricorn", Apricorn.BLUE)
    val GREEN_APRICORN = apricornBlock("green_apricorn", Apricorn.GREEN)
    val PINK_APRICORN = apricornBlock("pink_apricorn", Apricorn.PINK)
    val RED_APRICORN = apricornBlock("red_apricorn", Apricorn.RED)
    val WHITE_APRICORN = apricornBlock("white_apricorn", Apricorn.WHITE)
    val YELLOW_APRICORN = apricornBlock("yellow_apricorn", Apricorn.YELLOW)

    val HEALING_MACHINE = this.create("healing_machine", HealingMachineBlock(AbstractBlock.Settings.of(Material.METAL, MapColor.IRON_GRAY).sounds(BlockSoundGroup.METAL).strength(2f).nonOpaque().luminance { state: BlockState ->
        if (state.get(HealingMachineBlock.CHARGE_LEVEL) >= HealingMachineBlock.MAX_CHARGE_LEVEL) 7 else 2
    }))
    val PC = this.create("pc", PCBlock(AbstractBlock.Settings.of(Material.METAL, MapColor.IRON_GRAY).sounds(BlockSoundGroup.METAL).strength(2F).nonOpaque().luminance { state: BlockState ->
        if ((state.get(PCBlock.ON) as Boolean) && (state.get(PCBlock.PART) == PCBlock.PCPart.TOP)) 10 else 0
    }))

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

    fun registerFlammableCobblemonBlocks(registerFlammableBlock: (Block, Int, Int) -> Unit) {
        registerFlammableBlock(APRICORN_PLANKS, 5, 20)
        registerFlammableBlock(APRICORN_SLAB, 5, 20)
        registerFlammableBlock(APRICORN_FENCE_GATE, 5, 20)
        registerFlammableBlock(APRICORN_FENCE, 5, 20)
        registerFlammableBlock(APRICORN_STAIRS, 5, 20)
        registerFlammableBlock(APRICORN_LOG, 5, 5)
        registerFlammableBlock(STRIPPED_APRICORN_LOG, 5, 5)
        registerFlammableBlock(APRICORN_WOOD, 5, 5)
        registerFlammableBlock(APRICORN_LEAVES, 30, 60)
    }
}