package com.cablemc.pokemod.common.command.argument

import com.cablemc.pokemod.common.client.visual.VisualPattern
import com.cablemc.pokemod.common.client.visual.VisualPatterns
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.command.CommandSource
import net.minecraft.text.Text
import java.util.concurrent.CompletableFuture

class PatternArgumentType: ArgumentType<VisualPattern> {

    override fun parse(reader: StringReader): VisualPattern = VisualPatterns.getByName(reader.readString())
        ?: throw SimpleCommandExceptionType(INVALID_PATTERN).createWithContext(reader)

    override fun <S : Any> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        return CommandSource.suggestMatching(VisualPatterns.getPatterns(), builder)
    }

    override fun getExamples() = EXAMPLES

    companion object {

        val EXAMPLES: List<String> = listOf("orbit")
        val INVALID_PATTERN = Text.translatable("pokemod.command.playvisual.invalid-pattern")

        fun pattern() = PatternArgumentType()

        fun <S> getPattern(context: CommandContext<S>, name: String): VisualPattern {
            return context.getArgument(name, VisualPattern::class.java)
        }

    }

}