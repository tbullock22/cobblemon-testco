/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.spawning

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.FileFilter
import java.io.FileReader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.isRegularFile
import kotlin.io.path.name

internal class SpawnValidator {

    @Test
    fun `Validate Spawn Files`() {
        val root = Paths.get("src")
            .resolve("main")
            .resolve("resources")
            .resolve("data")
            .resolve("cobblemon")

        val generations = root.resolve("species")
            .toFile()
            .listFiles(FileFilter {
                it.isDirectory
            })

        val species = mutableListOf<String>()
        for (generation in generations) {
            Files.walk(generation.toPath()).filter { it.isRegularFile() }.use {
                it.forEach { species.add(it.name.removeSuffix(".json")) }
            }
        }

        val spawns = root.resolve("spawn_pool_world")
            .toFile()
            .listFiles(FileFilter { it.isFile })

        val invalid = mutableListOf<Path>()
        val gson = GsonBuilder().create()
        for (spawner in spawns) {
            val json = gson.fromJson(FileReader(spawner), JsonObject::class.java)
            val options = json.getAsJsonArray("spawns")
            for (option in options) {
                val obj = option as JsonObject
                var target = obj.get("pokemon").asString
                if (target.contains(" ")) {
                    target = target.substring(0, target.indexOf(' '))
                }

                if (!species.contains(target)) {
                    invalid.add(spawner.toPath())
                }
            }
        }

        if (invalid.isNotEmpty()) {
            println("Detected invalid spawn parameters (${invalid.size})")
            for (marked in invalid) {
                println("- $marked")
            }
        }

        assertTrue(invalid.isEmpty())
    }

}