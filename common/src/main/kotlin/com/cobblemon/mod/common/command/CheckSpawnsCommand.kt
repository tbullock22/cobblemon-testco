/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.Cobblemon.config
import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.api.spawning.CobblemonWorldSpawnerManager
import com.cobblemon.mod.common.api.spawning.SpawnCause
import com.cobblemon.mod.common.api.spawning.spawner.SpawningArea
import com.cobblemon.mod.common.api.text.*
import com.cobblemon.mod.common.command.argument.SpawnBucketArgumentType
import com.cobblemon.mod.common.util.lang
import com.cobblemon.mod.common.util.permission
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.MutableText
import net.minecraft.util.math.MathHelper
import java.text.DecimalFormat

object CheckSpawnsCommand {
    const val PURPLE_THRESHOLD = 0.01F
    const val RED_THRESHOLD = 0.1F
    const val YELLOW_THRESHOLD = 5F
    val df = DecimalFormat("#.##")

    fun register(dispatcher : CommandDispatcher<ServerCommandSource>) {
        dispatcher.register(CommandManager.literal("checkspawn")
            .permission(CobblemonPermissions.CHECKSPAWNS)
            .then(
                CommandManager.argument("bucket", SpawnBucketArgumentType.spawnBucket())
                    .requires { it.player != null }
                    .executes { execute(it, it.source.playerOrThrow) }
            ))
    }

    private fun execute(context: CommandContext<ServerCommandSource>, player: ServerPlayerEntity) : Int {
        if (!config.enableSpawning) {
            return 0
        }

        val spawner = CobblemonWorldSpawnerManager.spawnersForPlayers[player.uuid] ?: return Command.SINGLE_SUCCESS
        val bucket = SpawnBucketArgumentType.getSpawnBucket(context, "bucket")
        val cause = SpawnCause(spawner, bucket, player)

        val slice = spawner.prospector.prospect(
            spawner = spawner,
            area = SpawningArea(
                cause = cause,
                world = player.world,
                baseX = MathHelper.ceil(player.x - config.worldSliceDiameter / 2F),
                baseY = MathHelper.ceil(player.y - config.worldSliceHeight / 2F),
                baseZ = MathHelper.ceil(player.z - config.worldSliceDiameter / 2F),
                length = config.worldSliceDiameter,
                height = config.worldSliceHeight,
                width = config.worldSliceDiameter
            )
        )

        val contexts = spawner.resolver.resolve(spawner, spawner.contextCalculators, slice)

        val spawnProbabilities = spawner.getSpawningSelector().getProbabilities(spawner, contexts)

        val spawnNames = mutableMapOf<String, MutableText>()
        val namedProbabilities = mutableMapOf<MutableText, Float>()

        spawnProbabilities.entries.forEach {
            val nameText = it.key.getName()
            val nameString = nameText.string
            if (!spawnNames.containsKey(nameString)) {
                spawnNames[nameString] = it.key.getName()
            }

            val standardizedNameText = spawnNames[nameString]!!
            namedProbabilities[standardizedNameText] = (namedProbabilities[standardizedNameText] ?: 0F) + it.value
        }

        val sortedEntries = namedProbabilities.entries.sortedByDescending { it.value }
        val messages = mutableListOf<MutableText>()
        sortedEntries.forEach { (name, percentage) ->
            val message = name + ": " + applyColour("${df.format(percentage)}%".text(), percentage)
//            player.sendMessage()
            messages.add(message)
        }

        if (messages.isEmpty()) {
            player.sendMessage(lang("command.checkspawns.nothing").red())
        } else {
            player.sendMessage(lang("command.checkspawns.spawns").underline())
            val msg = messages[0]
            for (nextMessage in messages.subList(1, messages.size)) {
                msg.add(", ".text() + nextMessage)
            }
            player.sendMessage(msg)
        }

        return Command.SINGLE_SUCCESS
    }

    fun applyColour(name: MutableText, percentage: Float): MutableText {
        return if (percentage < PURPLE_THRESHOLD) {
            name.lightPurple()
        } else if (percentage < RED_THRESHOLD) {
            name.red()
        } else if (percentage < YELLOW_THRESHOLD) {
            name.yellow()
        } else {
            name.green()
        }
    }
}