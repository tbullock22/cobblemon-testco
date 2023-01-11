/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common

import com.cobblemon.mod.common.command.ChangeScaleAndSize
import com.cobblemon.mod.common.command.CheckSpawnsCommand
import com.cobblemon.mod.common.command.ClickTextCommand
import com.cobblemon.mod.common.command.FriendshipCommand
import com.cobblemon.mod.common.command.GetNBT
import com.cobblemon.mod.common.command.GiveAllPokemon
import com.cobblemon.mod.common.command.GivePokemon
import com.cobblemon.mod.common.command.HealPokemonCommand
import com.cobblemon.mod.common.command.LevelUp
import com.cobblemon.mod.common.command.OpenStarterScreenCommand
import com.cobblemon.mod.common.command.PokemonEditCommand
import com.cobblemon.mod.common.command.SpawnAllPokemon
import com.cobblemon.mod.common.command.SpawnPokemon
import com.cobblemon.mod.common.command.StopBattleCommand
import com.cobblemon.mod.common.command.TakePokemon
import com.cobblemon.mod.common.command.TeachCommand
import com.cobblemon.mod.common.command.TestCommand
import com.mojang.brigadier.CommandDispatcher
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource

object CobblemonCommands {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>, registry: CommandRegistryAccess, selection: CommandManager.RegistrationEnvironment) {
        SpawnPokemon.register(dispatcher)
        GivePokemon.register(dispatcher)
        TakePokemon.register(dispatcher)
        ChangeScaleAndSize.register(dispatcher)
        TestCommand.register(dispatcher)
        ClickTextCommand.register(dispatcher)
        PokemonEditCommand.register(dispatcher)
        TeachCommand.register(dispatcher)
        LevelUp.register(dispatcher)
        HealPokemonCommand.register(dispatcher)
        StopBattleCommand.register(dispatcher)
        CheckSpawnsCommand.register(dispatcher)
        GetNBT.register(dispatcher)
        OpenStarterScreenCommand.register(dispatcher)
        SpawnAllPokemon.register(dispatcher)
        FriendshipCommand.register(dispatcher)
        GiveAllPokemon.register(dispatcher)
    }
}