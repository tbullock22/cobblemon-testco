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
import com.cobblemon.mod.common.command.argument.PokemonPropertiesArgumentType
import com.cobblemon.mod.common.util.alias
import com.cobblemon.mod.common.util.commandLang
import com.cobblemon.mod.common.util.permission
import com.cobblemon.mod.common.util.toBlockPos
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.command.argument.Vec3ArgumentType
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

object SpawnPokemon {

    private const val NAME = "spawnpokemon"
    private const val PROPERTIES = "properties"
    private const val POSITION = "pos"
    private const val ALIAS = "pokespawn"
    private const val AT_NAME = "${NAME}at"
    private const val AT_ALIAS = "${ALIAS}at"
    private val NO_SPECIES_EXCEPTION = SimpleCommandExceptionType(commandLang("${NAME}.nospecies").red())
    // ToDo maybe dedicated lang down the line but the errors shouldn't really happen unless people are really messing up
    private val INVALID_POS_EXCEPTION = SimpleCommandExceptionType(Text.literal("Invalid position").red())
    private val FAILED_SPAWN_EXCEPTION = SimpleCommandExceptionType(Text.literal("Unable to spawn at the given position").red())

    fun register(dispatcher : CommandDispatcher<ServerCommandSource>) {
        val contextPositionCommand = dispatcher.register(literal(NAME)
            .permission(CobblemonPermissions.SPAWN_POKEMON)
            .then(argument(PROPERTIES, PokemonPropertiesArgumentType.properties())
                .executes{ context -> this.execute(context, context.source.position) }
            )
        )
        dispatcher.register(contextPositionCommand.alias(ALIAS))
        val argumentPositionCommand = dispatcher.register(literal(AT_NAME)
            .permission(CobblemonPermissions.SPAWN_POKEMON)
            .then(argument(POSITION, Vec3ArgumentType.vec3())
                .then(argument(PROPERTIES, PokemonPropertiesArgumentType.properties())
                    .executes { context -> execute(context, Vec3ArgumentType.getVec3(context, POSITION)) }
                )
            )
        )
        dispatcher.register(argumentPositionCommand.alias(AT_ALIAS))
    }

    private fun execute(context: CommandContext<ServerCommandSource>, pos: Vec3d): Int {
        val world = context.source.world
        val blockPos = pos.toBlockPos()
        if (!World.isValid(blockPos)) {
            throw INVALID_POS_EXCEPTION.create()
        }
        val properties = PokemonPropertiesArgumentType.getPokemonProperties(context, PROPERTIES)
        if (properties.species == null) {
            throw NO_SPECIES_EXCEPTION.create()
        }
        val pokemonEntity = properties.createEntity(world)
        pokemonEntity.refreshPositionAndAngles(pos.x, pos.y, pos.z, pokemonEntity.yaw, pokemonEntity.pitch)
        if (world.spawnEntity(pokemonEntity)) {
            return Command.SINGLE_SUCCESS
        }
        throw FAILED_SPAWN_EXCEPTION.create()
    }

}