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
import com.cobblemon.mod.common.util.commandLang
import com.cobblemon.mod.common.util.permission
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.command.argument.ItemStackArgumentType
import net.minecraft.server.command.CommandManager.*
import net.minecraft.server.command.ServerCommandSource

object HeldItemCommand {

    private const val NAME = "held_item"
    private const val TARGET = "target"
    private const val SLOT = "slot"
    private const val ITEM = "item"

    fun register(dispatcher: CommandDispatcher<ServerCommandSource>, commandRegistryAccess: CommandRegistryAccess) {
        dispatcher.register(literal(NAME)
            .permission(CobblemonPermissions.HELD_ITEM)
            .then(argument(TARGET, EntityArgumentType.player())
                .then(argument(SLOT, PartySlotArgumentType.partySlot())
                    .then(argument(ITEM, ItemStackArgumentType.itemStack(commandRegistryAccess))
                        .executes(this::execute)
                    )
                )
            )
        )
    }

    private fun execute(ctx: CommandContext<ServerCommandSource>): Int {
        val player = EntityArgumentType.getPlayer(ctx, TARGET)
        val pokemon = PartySlotArgumentType.getPokemonOf(ctx, SLOT, player)
        val stackArgument = ItemStackArgumentType.getItemStackArgument(ctx, ITEM)
        val stack = stackArgument.createStack(1, false)
        pokemon.swapHeldItem(stack)
        ctx.source.sendFeedback(commandLang(NAME, player.name, pokemon.species.translatedName, stack.name), true)
        return Command.SINGLE_SUCCESS
    }

}