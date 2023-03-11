/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.util.party
import com.cobblemon.mod.common.util.permission
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity

object TakePokemon {
    fun register(dispatcher : CommandDispatcher<ServerCommandSource>) {
        val command = CommandManager.literal("takepokemon")
            .permission(CobblemonPermissions.TAKE_POKEMON)
            .then(
                CommandManager.argument("player", EntityArgumentType.player())
                    .then(
                        CommandManager.argument("slot", IntegerArgumentType.integer(1, 99))
                            .executes { execute(it) }
                    )
            )

        dispatcher.register(command)
    }

    private fun execute(context: CommandContext<ServerCommandSource>) : Int {
        try {
            val target = EntityArgumentType.getPlayer(context, "player")
            val slot = IntegerArgumentType.getInteger(context, "slot")
            val party = target.party()

            if (slot > party.size()) {
                // todo translate
                context.source.sendError("Your party only has ${party.size()} slots.".text())
                return 0
            }

            val pokemon = party.get(slot - 1)
            if (pokemon == null) {
                context.source.sendError("There is no Pokémon in slot $slot".text())
                return 0
            }

            party.remove(pokemon)
            if (context.source.entity != target) {
                if (context.source.entity is ServerPlayerEntity) {
                    val player = context.source.player ?: return Command.SINGLE_SUCCESS
                    val toParty = player.party()
                    toParty.add(pokemon)
                    context.source.sendFeedback("You took ${pokemon.species.name}".text(), true)
                    return Command.SINGLE_SUCCESS
                }
            }

            context.source.sendFeedback("${pokemon.species.name} was removed.".text(), true)
            return Command.SINGLE_SUCCESS
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Command.SINGLE_SUCCESS
    }
}