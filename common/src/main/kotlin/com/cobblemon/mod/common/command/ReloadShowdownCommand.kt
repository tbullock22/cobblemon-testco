/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.battles.runner.ShowdownService
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text

object ReloadShowdownCommand {

    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        val command = CommandManager.literal("reloadshowdown")
            .requires { it.hasPermissionLevel(4) }
            .executes { execute(it) }
        dispatcher.register(command)
    }

    private fun execute(context: CommandContext<ServerCommandSource>): Int {
        try {
            ShowdownService.get().closeConnection()
            ShowdownService.get().openConnection()
            context.source.sendMessage(Text.of("Reloaded showdown"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Command.SINGLE_SUCCESS
    }

}