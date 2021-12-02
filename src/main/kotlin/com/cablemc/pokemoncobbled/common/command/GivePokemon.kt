package com.cablemc.pokemoncobbled.common.command

import com.cablemc.pokemoncobbled.common.api.storage.PokemonStoreManager
import com.cablemc.pokemoncobbled.common.command.argument.PokemonArgumentType
import com.cablemc.pokemoncobbled.common.pokemon.Pokemon
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.selector.EntitySelector
import net.minecraft.network.chat.TextComponent

object GivePokemon {

    fun register(dispatcher : CommandDispatcher<CommandSourceStack>) {
        val command = Commands.literal("givepokemon")
            .requires { it.hasPermission(4) }
            .then(
                Commands.argument("player", EntityArgument.player())
                    .then(Commands.argument("pokemon", PokemonArgumentType.pokemon()).executes { execute(it) })
            )
        dispatcher.register(command)
    }

    private fun execute(context: CommandContext<CommandSourceStack>) : Int {
        try {
            val pkm = PokemonArgumentType.getPokemon(context, "pokemon")
            val player = context.getArgument("player", EntitySelector::class.java).findSinglePlayer(context.source)
            val pokemon = Pokemon().apply { species = pkm }
            val party = PokemonStoreManager.getParty(player)
            party.add(pokemon)
            context.source.sendSuccess(TextComponent("Gave ").append(player.name).append(" a ${pkm.name}"), true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Command.SINGLE_SUCCESS
    }
}