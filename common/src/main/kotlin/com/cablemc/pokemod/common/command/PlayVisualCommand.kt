package com.cablemc.pokemod.common.command

import com.cablemc.pokemod.common.client.visual.Visual
import com.cablemc.pokemod.common.client.visual.Visuals.registerVisual
import com.cablemc.pokemod.common.command.argument.PatternArgumentType
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.command.argument.EntitySummonArgumentType
import net.minecraft.command.argument.ParticleEffectArgumentType
import net.minecraft.command.argument.Vec3ArgumentType
import net.minecraft.particle.DefaultParticleType
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.world.ServerWorld

object PlayVisualCommand {
    private const val NAME = "playvisual"
    private const val PRESET = "preset"
    private const val VISUALPRESET = "visualpreset"
    private const val PATTERN = "pattern"
    private const val POS = "pos"
    private const val ENTITYSUMMON = "entity_summon"
    private const val TARGETENTITY = "target_entity"
    private const val PARTICLE = "particle"
    private const val DURATION = "duration"
    private const val PATTERNFLAGS = "pattern_flags"

    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        val command = literal(NAME).requires { it.hasPermissionLevel(2) }
            .then(literal(PRESET)
                //TODO: Create a Visual Preset Argument Type
                .then(argument(VISUALPRESET, StringArgumentType.string())
                    .then(argument(POS, Vec3ArgumentType.vec3())
                        //TODO: Add Visual Flags Argument Type
                        .executes { execute(it) }
                    )
                    .then(argument(TARGETENTITY, EntityArgumentType.entity())
                        //TODO: Add Visual Flags Argument Type
                        .executes { execute(it) }
                    )
                )
            )
            .then(argument(PATTERN, PatternArgumentType.pattern())
                .then(argument(PARTICLE, ParticleEffectArgumentType.particleEffect())
                    .then(argument(POS, Vec3ArgumentType.vec3())
                        .then(argument(DURATION, IntegerArgumentType.integer())
                            .then(argument(PATTERNFLAGS, StringArgumentType.greedyString())
                                .executes { execute(it) }
                            )
                        )
                    )
                    .then(argument(TARGETENTITY, EntityArgumentType.entity())
                        .then(argument(DURATION, IntegerArgumentType.integer())
                            .then(argument(PATTERNFLAGS, StringArgumentType.greedyString())
                                .executes { execute(it) }
                            )
                        )
                    )
                )
                .then(argument(ENTITYSUMMON, EntitySummonArgumentType.entitySummon())
                    .then(argument(POS, Vec3ArgumentType.vec3())
                        .then(argument(DURATION, IntegerArgumentType.integer())
                            .then(argument(PATTERNFLAGS, StringArgumentType.greedyString())
                                .executes { execute(it) }
                            )
                        )
                    )
                    .then(argument(TARGETENTITY, EntityArgumentType.entity())
                        .then(argument(DURATION, IntegerArgumentType.integer())
                            .then(argument(PATTERNFLAGS, StringArgumentType.greedyString())
                                .executes { execute(it) }
                            )
                        )
                    )
                )
            )
        dispatcher.register(command)
    }


    private fun execute(
        context: CommandContext<ServerCommandSource>
    ): Int {
        try {
            //TODO: Add functionality for pattern "presets", entities, and entity-centered visuals
            val world = context.source.world as ServerWorld
            val pattern = PatternArgumentType.getPattern(context, PATTERN)
            val particle = ParticleEffectArgumentType.getParticle(context, PARTICLE)
            val pos = Vec3ArgumentType.getVec3(context, POS)
            val duration = IntegerArgumentType.getInteger(context, DURATION)
            val patternFlags = StringArgumentType.getString(context, PATTERNFLAGS)
            pattern.parse(patternFlags)

            val visual = Visual(particle as DefaultParticleType, world, pos, 0, duration, pattern)
            registerVisual(visual)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Command.SINGLE_SUCCESS
    }
}