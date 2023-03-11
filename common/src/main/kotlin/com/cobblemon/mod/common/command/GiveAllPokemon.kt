/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.util.party
import com.cobblemon.mod.common.util.requiresWithPermission
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource

object GiveAllPokemon {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register(
            CommandManager.literal("giveallpokemon")
                .requiresWithPermission(CobblemonPermissions.GIVE_ALL_POKEMON) { it.player != null }
                .then(
                    CommandManager.argument("min", IntegerArgumentType.integer(1))
                        .then(
                            CommandManager.argument("max", IntegerArgumentType.integer(1))
                                .executes {
                                    execute(it, IntegerArgumentType.getInteger(it, "min")..IntegerArgumentType.getInteger(it, "max"))
                                }
                        )
                        .executes { execute(it, IntegerArgumentType.getInteger(it, "min")..Int.MAX_VALUE) }
                )
                .executes { execute(it, 1..Int.MAX_VALUE) }
        )
    }

    private fun execute(context: CommandContext<ServerCommandSource>, range: IntRange) : Int {
        val player = context.source.playerOrThrow
        val pc = player.party().getOverflowPC() ?: return 0

        val orderedSpeces = PokemonSpecies.implemented.sortedBy { it.nationalPokedexNumber }

        for (species in orderedSpeces) {
            pc.add(species.create())//.sendOut(player.world as ServerWorld, player.pos)
        }

        return Command.SINGLE_SUCCESS
    }
}