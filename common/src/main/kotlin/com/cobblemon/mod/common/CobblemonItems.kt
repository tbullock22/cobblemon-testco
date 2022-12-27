/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common

import com.cobblemon.mod.common.api.pokeball.PokeBalls
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.item.ApricornItem
import com.cobblemon.mod.common.item.CobblemonItem
import com.cobblemon.mod.common.item.CobblemonItemGroups
import com.cobblemon.mod.common.item.PokeBallItem
import com.cobblemon.mod.common.item.interactive.CandyItem
import com.cobblemon.mod.common.item.interactive.LinkCableItem
import com.cobblemon.mod.common.item.interactive.VitaminItem
import com.cobblemon.mod.common.pokeball.PokeBall
import com.cobblemon.mod.common.registry.CompletableRegistry
import dev.architectury.registry.registries.RegistrySupplier
import net.minecraft.block.Block
import net.minecraft.item.AliasedBlockItem
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.util.registry.Registry

object CobblemonItems : CompletableRegistry<Item>(Registry.ITEM_KEY) {

    private val pokeballs = mutableListOf<RegistrySupplier<PokeBallItem>>()

    val POKE_BALL = pokeballItem(PokeBalls.POKE_BALL)
    val CITRINE_BALL = pokeballItem(PokeBalls.CITRINE_BALL)
    val VERDANT_BALL = pokeballItem(PokeBalls.VERDANT_BALL)
    val AZURE_BALL = pokeballItem(PokeBalls.AZURE_BALL)
    val ROSEATE_BALL = pokeballItem(PokeBalls.ROSEATE_BALL)
    val SLATE_BALL = pokeballItem(PokeBalls.SLATE_BALL)
    val PREMIER_BALL = pokeballItem(PokeBalls.PREMIER_BALL)
    val GREAT_BALL = pokeballItem(PokeBalls.GREAT_BALL)
    val ULTRA_BALL = pokeballItem(PokeBalls.ULTRA_BALL)
    val SAFARI_BALL = pokeballItem(PokeBalls.SAFARI_BALL)
    val FAST_BALL = pokeballItem(PokeBalls.FAST_BALL)
    val LEVEL_BALL = pokeballItem(PokeBalls.LEVEL_BALL)
    val LURE_BALL = pokeballItem(PokeBalls.LURE_BALL)
    val HEAVY_BALL = pokeballItem(PokeBalls.HEAVY_BALL)
    val LOVE_BALL = pokeballItem(PokeBalls.LOVE_BALL)
    val FRIEND_BALL = pokeballItem(PokeBalls.FRIEND_BALL)
    val MOON_BALL = pokeballItem(PokeBalls.MOON_BALL)
    val SPORT_BALL = pokeballItem(PokeBalls.SPORT_BALL)
    val PARK_BALL = pokeballItem(PokeBalls.PARK_BALL)
    val NET_BALL = pokeballItem(PokeBalls.NET_BALL)
    val DIVE_BALL = pokeballItem(PokeBalls.DIVE_BALL)
    val NEST_BALL = pokeballItem(PokeBalls.NEST_BALL)
    val REPEAT_BALL = pokeballItem(PokeBalls.REPEAT_BALL)
    val TIMER_BALL = pokeballItem(PokeBalls.TIMER_BALL)
    val LUXURY_BALL = pokeballItem(PokeBalls.LUXURY_BALL)
    val DUSK_BALL = pokeballItem(PokeBalls.DUSK_BALL)
    val HEAL_BALL = pokeballItem(PokeBalls.HEAL_BALL)
    val QUICK_BALL = pokeballItem(PokeBalls.QUICK_BALL)
    val DREAM_BALL = pokeballItem(PokeBalls.DREAM_BALL)
    val BEAST_BALL = pokeballItem(PokeBalls.BEAST_BALL)
    val MASTER_BALL = pokeballItem(PokeBalls.MASTER_BALL)
    val CHERISH_BALL = pokeballItem(PokeBalls.CHERISH_BALL)

    val RED_APRICORN = queue("red_apricorn") { ApricornItem(CobblemonBlocks.RED_APRICORN.get()) }
    val YELLOW_APRICORN = queue("yellow_apricorn") { ApricornItem(CobblemonBlocks.YELLOW_APRICORN.get()) }
    val GREEN_APRICORN = queue("green_apricorn") { ApricornItem(CobblemonBlocks.GREEN_APRICORN.get()) }
    val BLUE_APRICORN = queue("blue_apricorn") { ApricornItem(CobblemonBlocks.BLUE_APRICORN.get()) }
    val PINK_APRICORN = queue("pink_apricorn") { ApricornItem(CobblemonBlocks.PINK_APRICORN.get()) }
    val BLACK_APRICORN = queue("black_apricorn") { ApricornItem(CobblemonBlocks.BLACK_APRICORN.get()) }
    val WHITE_APRICORN = queue("white_apricorn") { ApricornItem(CobblemonBlocks.WHITE_APRICORN.get()) }

    val RED_APRICORN_SEED = queue("red_apricorn_seed") { itemNameBlockItem(CobblemonBlocks.RED_APRICORN_SAPLING.get(), ItemGroup.MISC) }
    val YELLOW_APRICORN_SEED = queue("yellow_apricorn_seed") { itemNameBlockItem(CobblemonBlocks.YELLOW_APRICORN_SAPLING.get(), ItemGroup.MISC) }
    val GREEN_APRICORN_SEED = queue("green_apricorn_seed") { itemNameBlockItem(CobblemonBlocks.GREEN_APRICORN_SAPLING.get(), ItemGroup.MISC) }
    val BLUE_APRICORN_SEED = queue("blue_apricorn_seed") { itemNameBlockItem(CobblemonBlocks.BLUE_APRICORN_SAPLING.get(), ItemGroup.MISC) }
    val PINK_APRICORN_SEED = queue("pink_apricorn_seed") { itemNameBlockItem(CobblemonBlocks.PINK_APRICORN_SAPLING.get(), ItemGroup.MISC) }
    val BLACK_APRICORN_SEED = queue("black_apricorn_seed") { itemNameBlockItem(CobblemonBlocks.BLACK_APRICORN_SAPLING.get(), ItemGroup.MISC) }
    val WHITE_APRICORN_SEED = queue("white_apricorn_seed") { itemNameBlockItem(CobblemonBlocks.WHITE_APRICORN_SAPLING.get(), ItemGroup.MISC) }

    val APRICORN_LOG = queue("apricorn_log") { blockItem(CobblemonBlocks.APRICORN_LOG.get(), ItemGroup.BUILDING_BLOCKS) }
    val STRIPPED_APRICORN_LOG = queue("stripped_apricorn_log") { blockItem(CobblemonBlocks.STRIPPED_APRICORN_LOG.get(), ItemGroup.BUILDING_BLOCKS) }
    val APRICORN_WOOD = queue("apricorn_wood") { blockItem(CobblemonBlocks.APRICORN_WOOD.get(), ItemGroup.BUILDING_BLOCKS) }
    val STRIPPED_APRICORN_WOOD = queue("stripped_apricorn_wood") { blockItem(CobblemonBlocks.STRIPPED_APRICORN_WOOD.get(), ItemGroup.BUILDING_BLOCKS) }
    val APRICORN_PLANKS = queue("apricorn_planks") { blockItem(CobblemonBlocks.APRICORN_PLANKS.get(), ItemGroup.BUILDING_BLOCKS) }
    val APRICORN_LEAVES = queue("apricorn_leaves") { blockItem(CobblemonBlocks.APRICORN_LEAVES.get(), ItemGroup.BUILDING_BLOCKS) }

    val APRICORN_DOOR = queue("apricorn_door") { blockItem(CobblemonBlocks.APRICORN_DOOR.get(), ItemGroup.REDSTONE) }
    val APRICORN_TRAPDOOR = queue("apricorn_trapdoor") { blockItem(CobblemonBlocks.APRICORN_TRAPDOOR.get(), ItemGroup.REDSTONE) }
    val APRICORN_FENCE = queue("apricorn_fence") { blockItem(CobblemonBlocks.APRICORN_FENCE.get(), ItemGroup.DECORATIONS) }
    val APRICORN_FENCE_GATE = queue("apricorn_fence_gate") { blockItem(CobblemonBlocks.APRICORN_FENCE_GATE.get(), ItemGroup.REDSTONE) }
    val APRICORN_BUTTON = queue("apricorn_button") { blockItem(CobblemonBlocks.APRICORN_BUTTON.get(), ItemGroup.REDSTONE) }
    val APRICORN_PRESSURE_PLATE = queue("apricorn_pressure_plate") { blockItem(CobblemonBlocks.APRICORN_PRESSURE_PLATE.get(), ItemGroup.REDSTONE) }
    //val APRICORN_SIGN = queue("apricorn_sign", SignItem(Item.Properties().stacksTo(16).tab(CreativeModeTab.TAB_DECORATIONS), CobblemonBlocks.APRICORN_SIGN, CobblemonBlocks.APRICORN_WALL_SIGN))
    val APRICORN_SLAB = queue("apricorn_slab") { blockItem(CobblemonBlocks.APRICORN_SLAB.get(), ItemGroup.BUILDING_BLOCKS) }
    val APRICORN_STAIRS = queue("apricorn_stairs") { blockItem(CobblemonBlocks.APRICORN_STAIRS.get(), ItemGroup.BUILDING_BLOCKS) }

    val HEALING_MACHINE = queue("healing_machine") { blockItem(CobblemonBlocks.HEALING_MACHINE.get(), ItemGroup.REDSTONE) }
    val PC = queue("pc") { blockItem(CobblemonBlocks.PC.get(), ItemGroup.REDSTONE) }

    // Evolution items
    val LINK_CABLE = queue("link_cable") { LinkCableItem() }
    val KINGS_ROCK = queue("kings_rock") { evolutionItem() }
    val METAL_COAT = queue("metal_coat") { evolutionItem() }
    val BLACK_AUGURITE = queue("black_augurite") { evolutionItem() }
    val PROTECTOR = queue("protector") { evolutionItem() }
    val OVAL_STONE = queue("oval_stone") { evolutionItem() }
    val DRAGON_SCALE = queue("dragon_scale") { evolutionItem() }
    val ELECTIRIZER = queue("electirizer") { evolutionItem() }
    val MAGMARIZER = queue("magmarizer") { evolutionItem() }
    val UPGRADE = queue("upgrade") { evolutionItem() }
    val DUBIOUS_DISC = queue("dubious_disc") { evolutionItem() }

    // Medicine
    val RARE_CANDY = queue("rare_candy") { CandyItem { _, pokemon -> pokemon.getExperienceToNextLevel() } }
    val EXPERIENCE_CANDY_XS = queue("exp_candy_xs") { CandyItem { _, _ -> CandyItem.DEFAULT_XS_CANDY_YIELD } }
    val EXPERIENCE_CANDY_S = queue("exp_candy_s") { CandyItem { _, _ -> CandyItem.DEFAULT_S_CANDY_YIELD } }
    val EXPERIENCE_CANDY_M = queue("exp_candy_m") { CandyItem { _, _ -> CandyItem.DEFAULT_M_CANDY_YIELD } }
    val EXPERIENCE_CANDY_L = queue("exp_candy_l") { CandyItem { _, _ -> CandyItem.DEFAULT_L_CANDY_YIELD } }
    val EXPERIENCE_CANDY_XL = queue("exp_candy_xl") { CandyItem { _, _ -> CandyItem.DEFAULT_XL_CANDY_YIELD } }
    val CALCIUM = queue("calcium") { VitaminItem(Stats.SPECIAL_ATTACK) }
    val CARBOS = queue("carbos") { VitaminItem(Stats.SPEED) }
    val HP_UP = queue("hp_up") { VitaminItem(Stats.HP) }
    val IRON = queue("iron") { VitaminItem(Stats.DEFENCE) }
    val PROTEIN = queue("protein") { VitaminItem(Stats.ATTACK) }
    val ZINC = queue("zinc") { VitaminItem(Stats.SPECIAL_DEFENCE) }

    /**
     * Evolution Ores and Stones
     */
    val DAWN_STONE_ORE = queue("dawn_stone_ore") { blockItem(CobblemonBlocks.DAWN_STONE_ORE.get(), ItemGroup.BUILDING_BLOCKS) }
    val DUSK_STONE_ORE = queue("dusk_stone_ore") { blockItem(CobblemonBlocks.DUSK_STONE_ORE.get(), ItemGroup.BUILDING_BLOCKS) }
    val FIRE_STONE_ORE = queue("fire_stone_ore") { blockItem(CobblemonBlocks.FIRE_STONE_ORE.get(), ItemGroup.BUILDING_BLOCKS) }
    val ICE_STONE_ORE = queue("ice_stone_ore") { blockItem(CobblemonBlocks.ICE_STONE_ORE.get(), ItemGroup.BUILDING_BLOCKS) }
    val LEAF_STONE_ORE = queue("leaf_stone_ore") { blockItem(CobblemonBlocks.LEAF_STONE_ORE.get(), ItemGroup.BUILDING_BLOCKS) }
    val MOON_STONE_ORE = queue("moon_stone_ore") { blockItem(CobblemonBlocks.MOON_STONE_ORE.get(), ItemGroup.BUILDING_BLOCKS) }
    val SHINY_STONE_ORE = queue("shiny_stone_ore") { blockItem(CobblemonBlocks.SHINY_STONE_ORE.get(), ItemGroup.BUILDING_BLOCKS) }
    val SUN_STONE_ORE = queue("sun_stone_ore") { blockItem(CobblemonBlocks.SUN_STONE_ORE.get(), ItemGroup.BUILDING_BLOCKS) }
    val THUNDER_STONE_ORE = queue("thunder_stone_ore") { blockItem(CobblemonBlocks.THUNDER_STONE_ORE.get(), ItemGroup.BUILDING_BLOCKS) }
    val WATER_STONE_ORE = queue("water_stone_ore") { blockItem(CobblemonBlocks.WATER_STONE_ORE.get(), ItemGroup.BUILDING_BLOCKS) }
    val DEEPSLATE_DAWN_STONE_ORE = queue("deepslate_dawn_stone_ore") { blockItem(CobblemonBlocks.DEEPSLATE_DAWN_STONE_ORE.get(), ItemGroup.BUILDING_BLOCKS) }
    val DEEPSLATE_DUSK_STONE_ORE = queue("deepslate_dusk_stone_ore") { blockItem(CobblemonBlocks.DEEPSLATE_DUSK_STONE_ORE.get(), ItemGroup.BUILDING_BLOCKS) }
    val DEEPSLATE_FIRE_STONE_ORE = queue("deepslate_fire_stone_ore") { blockItem(CobblemonBlocks.DEEPSLATE_FIRE_STONE_ORE.get(), ItemGroup.BUILDING_BLOCKS) }
    val DEEPSLATE_ICE_STONE_ORE = queue("deepslate_ice_stone_ore") { blockItem(CobblemonBlocks.DEEPSLATE_ICE_STONE_ORE.get(), ItemGroup.BUILDING_BLOCKS) }
    val DEEPSLATE_LEAF_STONE_ORE = queue("deepslate_leaf_stone_ore") { blockItem(CobblemonBlocks.DEEPSLATE_LEAF_STONE_ORE.get(), ItemGroup.BUILDING_BLOCKS) }
    val DEEPSLATE_MOON_STONE_ORE = queue("deepslate_moon_stone_ore") { blockItem(CobblemonBlocks.DEEPSLATE_MOON_STONE_ORE.get(), ItemGroup.BUILDING_BLOCKS) }
    val DEEPSLATE_SHINY_STONE_ORE = queue("deepslate_shiny_stone_ore") { blockItem(CobblemonBlocks.DEEPSLATE_SHINY_STONE_ORE.get(), ItemGroup.BUILDING_BLOCKS) }
    val DEEPSLATE_SUN_STONE_ORE = queue("deepslate_sun_stone_ore") { blockItem(CobblemonBlocks.DEEPSLATE_SUN_STONE_ORE.get(), ItemGroup.BUILDING_BLOCKS) }
    val DEEPSLATE_THUNDER_STONE_ORE = queue("deepslate_thunder_stone_ore") { blockItem(CobblemonBlocks.DEEPSLATE_THUNDER_STONE_ORE.get(), ItemGroup.BUILDING_BLOCKS) }
    val DEEPSLATE_WATER_STONE_ORE = queue("deepslate_water_stone_ore") { blockItem(CobblemonBlocks.DEEPSLATE_WATER_STONE_ORE.get(), ItemGroup.BUILDING_BLOCKS) }
    val DRIPSTONE_MOON_STONE_ORE = queue("dripstone_moon_stone_ore") { blockItem(CobblemonBlocks.DRIPSTONE_MOON_STONE_ORE.get(), ItemGroup.BUILDING_BLOCKS) }
    val DAWN_STONE = queue("dawn_stone") { evolutionItem() }
    val DUSK_STONE = queue("dusk_stone") { evolutionItem() }
    val FIRE_STONE = queue("fire_stone") { evolutionItem() }
    val ICE_STONE = queue("ice_stone") { evolutionItem() }
    val LEAF_STONE = queue("leaf_stone") { evolutionItem() }
    val MOON_STONE = queue("moon_stone") { evolutionItem() }
    val SHINY_STONE = queue("shiny_stone") { evolutionItem() }
    val SUN_STONE = queue("sun_stone") { evolutionItem() }
    val THUNDER_STONE = queue("thunder_stone") { evolutionItem() }
    val WATER_STONE = queue("water_stone") { evolutionItem() }

    fun pokeballs(): List<RegistrySupplier<PokeBallItem>> = this.pokeballs

    private fun blockItem(block: Block, tab: ItemGroup) : BlockItem {
        return BlockItem(block, Item.Settings().group(tab))
    }

    private fun itemNameBlockItem(block: Block, tab: ItemGroup) : BlockItem {
        return AliasedBlockItem(block, Item.Settings().group(tab))
    }

    private fun evolutionItem(): CobblemonItem {
        return CobblemonItem(Item.Settings().group(CobblemonItemGroups.EVOLUTION_ITEM_GROUP))
    }

    private fun pokeballItem(pokeBall: PokeBall): RegistrySupplier<PokeBallItem> {
        val supplier = this.queue(pokeBall.name.path) { PokeBallItem(pokeBall) }
        pokeBall.itemSupplier = supplier
        this.pokeballs.add(supplier)
        return supplier
    }

}