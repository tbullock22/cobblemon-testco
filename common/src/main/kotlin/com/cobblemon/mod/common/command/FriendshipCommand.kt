/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.command.argument.PartySlotArgumentType
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.commandLang
import com.cobblemon.mod.common.util.permission
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity

object FriendshipCommand {

    fun register(dispatcher : CommandDispatcher<ServerCommandSource>) {
        dispatcher.register(CommandManager.literal("friendship")
            .permission(CobblemonPermissions.FRIENDSHIP)
            .then(
                CommandManager.argument("slot", PartySlotArgumentType.partySlot())
                    .executes { execute(it.source, it.source.playerOrThrow, PartySlotArgumentType.getPokemon(it, "slot")) }
            ))
    }

    private fun execute(source: ServerCommandSource, target: ServerPlayerEntity, pokemon: Pokemon) : Int {
        if (!target.world.isClient) {
            val feedback = commandLang("friendship", pokemon.displayName, pokemon.friendship)
            source.sendFeedback(feedback, true)
        }
        return Command.SINGLE_SUCCESS
    }

}