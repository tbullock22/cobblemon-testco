/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.command.argument.PartySlotArgumentType
import com.cobblemon.mod.common.command.argument.PokemonPropertiesArgumentType
import com.cobblemon.mod.common.util.alias
import com.cobblemon.mod.common.util.commandLang
import com.cobblemon.mod.common.util.permission
import com.cobblemon.mod.common.util.player
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity

object PokemonEditCommand {

    private const val NAME = "pokemonedit"
    private const val NAME_OTHER = "${NAME}other"
    private const val PLAYER = "player"
    private const val SLOT = "slot"
    private const val PROPERTIES = "properties"
    private const val ALIAS = "pokeedit"
    private const val ALIAS_OTHER = "${ALIAS}other"

    fun register(dispatcher : CommandDispatcher<ServerCommandSource>) {
        val selfCommand = dispatcher.register(literal(NAME)
            .permission(CobblemonPermissions.POKEMON_EDIT_SELF)
            .then(argument(SLOT, PartySlotArgumentType.partySlot())
                .then(argument(PROPERTIES, PokemonPropertiesArgumentType.properties())
                    .executes{ execute(it, it.source.playerOrThrow) }
                )
            )
        )
        dispatcher.register(selfCommand.alias(ALIAS))

        val otherCommand = dispatcher.register(literal(NAME_OTHER)
            .permission(CobblemonPermissions.POKEMON_EDIT_OTHER)
            .then(argument(PLAYER, EntityArgumentType.player())
                .then(argument(SLOT, PartySlotArgumentType.partySlot())
                    .then(argument(PROPERTIES, PokemonPropertiesArgumentType.properties())
                        .executes { execute(it, it.player()) }
                    )
                )
            )
        )
        dispatcher.register(otherCommand.alias(ALIAS_OTHER))
    }

    private fun execute(context: CommandContext<ServerCommandSource>, player: ServerPlayerEntity): Int {
        val pokemon = PartySlotArgumentType.getPokemonOf(context, SLOT, player)
        val oldName = pokemon.species.translatedName
        val properties = PokemonPropertiesArgumentType.getPokemonProperties(context, PROPERTIES)
        properties.apply(pokemon)
        context.source.sendFeedback(commandLang(NAME, oldName, player.name), true)
        return Command.SINGLE_SUCCESS
    }

}