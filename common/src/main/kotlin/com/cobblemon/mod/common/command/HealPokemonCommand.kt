/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.util.*
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity

object HealPokemonCommand {

    private val IN_BATTLE_EXCEPTION = SimpleCommandExceptionType(commandLang("pokeheal.in_battle").red())

    fun register(dispatcher : CommandDispatcher<ServerCommandSource>) {
        val command = dispatcher.register(literal("healpokemon")
            .permission(CobblemonPermissions.HEAL_POKEMON_SELF)
            .executes { execute(it.source, it.source.playerOrThrow) }
            .then(
                CommandManager.argument("player", EntityArgumentType.player())
                    .permission(CobblemonPermissions.HEAL_POKEMON_OTHER)
                    .executes { execute(it.source, it.player("player")) }
            ))
        dispatcher.register(command.alias("pokeheal"))
    }

    private fun execute(source: ServerCommandSource, target: ServerPlayerEntity) : Int {
        if (target.isInBattle()) {
            throw IN_BATTLE_EXCEPTION.create()
        }
        if (!target.world.isClient) {
            val party = target.party()
            party.heal()
            source.sendFeedback(commandLang("healpokemon.heal", target.name), true)
        }
        return Command.SINGLE_SUCCESS
    }

}