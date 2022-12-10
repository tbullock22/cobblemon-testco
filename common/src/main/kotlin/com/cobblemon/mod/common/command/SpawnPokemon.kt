/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.command.argument.PokemonPropertiesArgumentType
import com.cobblemon.mod.common.util.alias
import com.cobblemon.mod.common.util.commandLang
import com.cobblemon.mod.common.util.permission
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity

object SpawnPokemon {

    private const val NAME = "spawnpokemon"
    private const val PROPERTIES = "properties"
    private const val ALIAS = "pokespawn"

    fun register(dispatcher : CommandDispatcher<ServerCommandSource>) {
        val command = dispatcher.register(literal(NAME)
            .permission(CobblemonPermissions.SPAWN_POKEMON)
            .then(argument(PROPERTIES, PokemonPropertiesArgumentType.properties())
                .executes(this::execute)))
        dispatcher.register(command.alias(ALIAS))
    }

    private fun execute(context: CommandContext<ServerCommandSource>) : Int {
        val entity = context.source.entity
        if (entity is ServerPlayerEntity && !entity.world.isClient) {
            try {
                val pkm = PokemonPropertiesArgumentType.getPokemonProperties(context, PROPERTIES)
                if (pkm.species == null) {
                    entity.sendMessage(commandLang("${NAME}.nospecies").red())
                    return Command.SINGLE_SUCCESS
                }
                val pokemonEntity = pkm.createEntity(entity.world)
                entity.world.spawnEntity(pokemonEntity)
                pokemonEntity.setPosition(entity.pos)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return Command.SINGLE_SUCCESS
    }

}