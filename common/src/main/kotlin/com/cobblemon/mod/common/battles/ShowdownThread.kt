/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.battles

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.Cobblemon.LOGGER
import com.cobblemon.mod.common.Cobblemon.config
import com.cobblemon.mod.common.battles.runner.GraalShowdown
import com.cobblemon.mod.common.util.FileUtils
import com.cobblemon.mod.common.util.extractTo
import com.cobblemon.mod.common.util.fromJson
import com.google.gson.GsonBuilder
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import net.minecraft.util.Identifier
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch

class ShowdownThread : Thread("Cobblemon Showdown") {

    private val latch = CountDownLatch(1)
    private val gson = GsonBuilder()
        .disableHtmlEscaping()
        .create()

    private val whenReady : Queue<Runnable> = LinkedList()

    fun launch() {
        this.start()
        this.latch.await()
        for (action in whenReady) {
            action.run()
        }
    }

    fun queue(action: Runnable) {
        if (this.latch.count == 0L) {
            action.run()
        } else {
            this.whenReady.add(action)
        }
    }

    override fun run() {
        LOGGER.info("Starting showdown service...")

        var showdownDir = File("showdown")
        val metadata = loadShowdownMetadata()

        // Check if showdown needs to be installed
        if (!showdownDir.exists() || config.autoUpdateShowdown) {
            val showdownZip = File(showdownDir, "showdown.zip")
            showdownZip.mkdirs()
            val showdownMetadataFile = File(showdownDir, "showdown.json")

            var extract = true
            if (showdownMetadataFile.exists()) {
                val current = this.readShowdownMetadata(showdownMetadataFile)
                if (metadata!!.showdownVersion == current!!.showdownVersion) {
                    extract = false
                } else {
                    // Backup current install first before continuing
                    LOGGER.info("Updating showdown service to version ${metadata.showdownVersion}, from version ${current.showdownVersion}...")

                    val backupDir = File("showdown-backup")
                    if (backupDir.exists() && backupDir.isDirectory) {
                        backupDir.deleteRecursively()
                    }

                    showdownDir.copyTo(File("showdown-backup"))
                }
            }

            if (extract) {
                showdownDir = showdownZip.parentFile
                Identifier(Cobblemon.MODID, "showdown.zip").extractTo(showdownZip)
                Identifier(Cobblemon.MODID, "showdown.json").extractTo(showdownMetadataFile)
                FileUtils.unzipFile(showdownZip.toPath(), showdownDir.toPath())
                showdownZip.delete()
            }
        }


        // Initialize showdown connection
        GraalShowdown.createContext()
        GraalShowdown.boot()

        LOGGER.info("Showdown has been started!")
        this.latch.countDown()
    }

    private fun loadShowdownMetadata() : ShowdownMetadata? {
        try {
            val inputStream = javaClass.getResourceAsStream("/assets/${Cobblemon.MODID}/showdown.json")!!
            return gson.fromJson<ShowdownMetadata>(InputStreamReader(inputStream))
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        return null
    }

    private fun readShowdownMetadata(target: File) : ShowdownMetadata? {
        try {
            InputStreamReader(FileInputStream(target)).use {
                return gson.fromJson<ShowdownMetadata>(it)
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
            return null
        }
    }

    private data class ShowdownMetadata(val showdownVersion: Double)
}