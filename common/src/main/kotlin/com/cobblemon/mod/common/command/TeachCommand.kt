/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.api.moves.BenchedMove
import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.command.argument.MoveArgumentType
import com.cobblemon.mod.common.command.argument.PartySlotArgumentType
import com.cobblemon.mod.common.util.commandLang
import com.cobblemon.mod.common.util.permission
import com.cobblemon.mod.common.util.player
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity

object TeachCommand {

    private const val NAME = "teach"
    private const val PLAYER = "player"
    private const val SLOT = "slot"
    private const val MOVE = "move"
    private val ALREAD_KNOWS_EXCEPTION = Dynamic2CommandExceptionType { a, b -> commandLang("$NAME.already_knows", a, b).red() }

    fun register(dispatcher : CommandDispatcher<ServerCommandSource>) {
        val command = CommandManager.literal(NAME)
            .permission(CobblemonPermissions.TEACH)
            .then(CommandManager.argument(PLAYER, EntityArgumentType.player())
                .then(CommandManager.argument(SLOT, PartySlotArgumentType.partySlot())
                    .then(CommandManager.argument(MOVE, MoveArgumentType.move())
                        .executes { execute(it, it.player()) }
                    ))
            )
        dispatcher.register(command)
    }

    private fun execute(context: CommandContext<ServerCommandSource>, player: ServerPlayerEntity) : Int {
        val pokemon = PartySlotArgumentType.getPokemon(context, SLOT)
        val move = MoveArgumentType.getMove(context, MOVE)
        if (pokemon.moveSet.getMoves().any { it.template == move } || pokemon.benchedMoves.any { it.moveTemplate == move }) {
            throw ALREAD_KNOWS_EXCEPTION.create(pokemon.displayName, move.displayName)
        }
        if (pokemon.moveSet.hasSpace()) {
            pokemon.moveSet.add(move.create())
        }
        else {
            pokemon.benchedMoves.add(BenchedMove(move, 0))
        }
        context.source.sendFeedback(commandLang(NAME, pokemon.species.translatedName, player.name, move.displayName), true)
        return Command.SINGLE_SUCCESS
    }

}