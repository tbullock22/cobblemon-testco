/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.helditem

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.pokemon.helditem.HeldItemManager
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.battleLang
import net.minecraft.item.Item
import net.minecraft.text.Text

/**
 * The Cobblemon implementation of [HeldItemManager].
 * It directly consumes the [Pokemon.heldItem] when required.
 * The literal IDs are the path of item identifiers under the [Cobblemon.MODID] namespace.
 *
 * @author Licious
 * @since December 30th, 2022
 */
object CobblemonHeldItemManager : BaseCobblemonHeldItemManager() {

    /**
     * A collection of literal effect IDs that will trigger the Pokémon receiving the associated held item.
     */
    private val giveItemEffect = setOf("pickup", "recycle", "magician", "pickpocket", "thief", "covet", "harvest", "bestow", "switcheroo", "trick")

    /**
     * A collection of literal effect IDs that will trigger the Pokémon needing to have their item removed these are never communicated through '-enditem'.
     */
    private val takeItemEffect = setOf("magician", "pickpocket", "covet", "bestow")

    /** Remappings of [Item] to showdownId strings. */
    private val remaps = mutableMapOf<Item, String>()

    override fun load() {
        super.load()
        Cobblemon.LOGGER.info("Imported {} held item IDs from showdown", this.loadedItemCount())
    }

    override fun showdownId(pokemon: BattlePokemon): String? {
        val item = pokemon.effectedPokemon.heldItemNoCopy().item
        if (remaps.containsKey(item)) {
            return remaps[item]
        }
        val original = super.showdownId(pokemon)
        if (original == null && pokemon.effectedPokemon.heldItemNoCopy().isEmpty) {
            // This will allow interactions such as thief to occur, we want this when there is no item only instead of overwriting other stacks that aren't held items.
            return ""
        }
        return original
    }

    override fun handleStartInstruction(pokemon: BattlePokemon, battle: PokemonBattle, battleMessage: BattleMessage) {
        val itemID = battleMessage.argumentAt(1)?.lowercase()?.replace(" ", "") ?: run {
            battle.broadcastChatMessage(Text.literal("Failed to handle '-item' action: ${battleMessage.rawMessage}").red())
            Cobblemon.LOGGER.error("Failed to handle '-item' action: ${battleMessage.rawMessage}")
            return
        }
        val effect = battleMessage.effect()
        val battlerName = pokemon.getName()
        // The only item using the null effect gimmick
        if (effect == null && itemID == "airballoon") {
            battle.broadcastChatMessage(battleLang("item.air_balloon.start", battlerName))
            return
        }
        val source = battleMessage.actorAndActivePokemonFromOptional(battle)?.second?.battlePokemon
        val itemName = this.nameOf(itemID)
        val sourceName = source?.getName() ?: Text.of("UNKNOWN")
        val effectId = effect?.id?.lowercase() ?: ""
        val text = when (effectId) {
            "pickup", "recycle" -> battleLang("item.recycle_or_pickup.start", battlerName, itemName)
            "frisk" -> battleLang("item.frisk.start", sourceName, battlerName, itemName)
            // The "source" is actually the target here
            "magician", "pickpocket", "covet", "thief" -> battleLang("item.take_item.start", battlerName, sourceName, itemName)
            "harvest" -> battleLang("item.harvest.start", battlerName, itemName)
            "bestow" -> battleLang("item.bestow.start", battlerName, itemName, sourceName)
            "switcheroo", "trick" -> battleLang("item.tricked.start", battlerName)
            else -> Text.literal("Cannot interpret ${battleMessage.rawMessage}").red().also {
                Cobblemon.LOGGER.error("Failed to handle '-item' action: ${battleMessage.rawMessage}")
            }
        }
        if (this.giveItemEffect.contains(effectId)) {
            this.give(pokemon, itemID)
        }
        if (this.takeItemEffect.contains(effectId)) {
            battleMessage.actorAndActivePokemonFromOptional(battle)?.second?.battlePokemon?.let { this.take(it, itemID) }
        }
        battle.broadcastChatMessage(text)
    }

    override fun handleEndInstruction(pokemon: BattlePokemon, battle: PokemonBattle, battleMessage: BattleMessage) {
        val itemID = battleMessage.argumentAt(1)?.lowercase()?.replace(" ", "") ?: run {
            battle.broadcastChatMessage(Text.literal("Failed to handle '-enditem' action: ${battleMessage.rawMessage}").red())
            Cobblemon.LOGGER.error("Failed to handle '-enditem' action: ${battleMessage.rawMessage}")
            return
        }
        // These are sent when showdown wants the client to animate something but not produce any text
        if (battleMessage.hasOptionalArgument("silent")) {
            this.take(pokemon, itemID)
            return
        }
        val battlerName = pokemon.getName()
        val itemName = this.nameOf(itemID)
        if (battleMessage.hasOptionalArgument("eat")) {
            battle.broadcastChatMessage(battleLang("item.eat.end", battlerName, itemName))
            this.take(pokemon, itemID)
            return
        }
        val source = battleMessage.actorAndActivePokemonFromOptional(battle)?.second?.battlePokemon
        val sourceName = source?.getName() ?: Text.of("UNKNOWN")
        val effect = battleMessage.effect()
        val text = when (effect?.id?.lowercase() ?: "") {
            "fling" -> battleLang("item.fling.end", battlerName, itemName)
            "knockoff" -> battleLang("item.knock_off.end", sourceName, battlerName, itemName)
            "gem" -> battleLang("item.gem.end", itemName, battlerName)
            "incinerate" -> battleLang("item.incinerate.end", battlerName, itemName)
            "stealeat" -> battleLang("item.steal_eat.end", sourceName, battlerName, itemName)
            else -> when (itemID) {
                "airballoon" -> battleLang("item.air_balloon.end", battlerName)
                "focussash" -> battleLang("item.hung_on.end", battlerName, itemName)
                "redcard" -> battleLang("item.red_card.end", battlerName, sourceName)
                "berryjuice" -> battleLang("item.berry_juice.end", battlerName)
                "boosterenergy", "electricseed", "grassyseed", "mistyseed", "psychicseed", "roomservice" -> battleLang("item.item.used_its.end", battlerName, itemName)
                "mentalherb" -> battleLang("item.mental_herb.end", battlerName)
                "powerherb" -> battleLang("item.power_herb.end", battlerName)
                "mirrorherb" -> battleLang("item.mirror_herb.end", battlerName)
                "whiteherb" -> battleLang("item.white_herb.end", battlerName)
                else -> Text.literal("Cannot interpret ${battleMessage.rawMessage}").red().also {
                    Cobblemon.LOGGER.error("Failed to handle '-enditem' action: ${battleMessage.rawMessage}")
                }
            }
        }
        this.take(pokemon, itemID)
        battle.broadcastChatMessage(text)
    }

    /**
     * Registers a custom mapping from [Item] to showdown ID string.
     *
     * @param item The Minecraft [Item] instance that has a specific showdownId.
     * @param showdownId The showdown name of this item.
     */
    fun registerRemap(item: Item, showdownId: String) {
        this.remaps[item] = showdownId
    }
}