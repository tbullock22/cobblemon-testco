/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.battles

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.Cobblemon.LOGGER
import com.cobblemon.mod.common.Cobblemon.config
import com.cobblemon.mod.common.Cobblemon.showdown
import com.cobblemon.mod.common.battles.runner.JavetShowdownConnection
import com.cobblemon.mod.common.util.FileUtils
import com.cobblemon.mod.common.util.extractTo
import com.cobblemon.mod.common.util.fromJson
import com.google.gson.GsonBuilder
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.util.concurrent.CompletableFuture
import net.minecraft.client.MinecraftClient
import net.minecraft.util.Identifier
class ShowdownThread : Thread("Cobblemon Showdown") {

    var showdownStarted = CompletableFuture<Unit>()
    val gson = GsonBuilder()
        .disableHtmlEscaping()
        .create()

    override fun run() {
        var showdownDir = File(".")

        val showdownMetadata = loadShowdownMetadata()

        // Check if showdown needs to be installed
        if (!showdownDir.exists() || config.autoUpdateShowdown) {
            val showdownZip = File(showdownDir, "showdown.zip")
            val showdownMetadataFile = File(showdownDir, "showdown.json")

            var extract = true

            if (showdownMetadataFile.exists()) {
                val metaDataStream = InputStreamReader(FileInputStream(showdownMetadataFile))
                val localShowdownMetadata = gson.fromJson<ShowdownMetadata>(metaDataStream)
                metaDataStream.close()
                if (showdownMetadata!!.showdownVersion == localShowdownMetadata.showdownVersion) {
                    extract = false
                } else {
                    showdownDir.renameTo(File("showdown-backup"))
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
        showdown = JavetShowdownConnection()
        (showdown as JavetShowdownConnection).initializeServer()

        // Sleep for two seconds before attempting connection
        sleep(2000)

        val maxTries = 15
        var tries = 0

        // If connection fails, wait another two seconds
        while (!attemptConnection() && tries < maxTries) {
            tries++
            sleep(3000)
        }

        // Max attempts
        if (tries == maxTries) {
            LOGGER.error("Failed to connect to showdown after 5 tries.")
            MinecraftClient.getInstance().close()
        }

        LOGGER.info("Showdown has been connected!")
        showdownStarted.complete(Unit)

        // Reset tries as this will be used by reconnection attempts
        tries = 0

        // While showdown is not closed, continue to check connection and read messages
        while (!showdown.isClosed()) {

            // Attempt reconnection if not connected
            if (!showdown.isConnected()) {
                while (!attemptConnection() && tries < maxTries) {
                    tries++
                    sleep(3000)
                }

                // Max attempts
                if (tries == maxTries) {
                    LOGGER.error("Failed to connect to showdown after 5 tries.")
                    MinecraftClient.getInstance().close()
                }

                tries = 0
                LOGGER.info("Showdown has been reconnected!")
            }

            // Reads messages and don't destroy the connection if there is an error
            try {
                showdown.read(ShowdownInterpreter::interpretMessage)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Read messages every half a second
            sleep(500)
        }
    }

    private fun attemptConnection() : Boolean {
        try {
            showdown.open()
            return true
        } catch (exception: IOException) {
            return false
        }
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

    private data class ShowdownMetadata(
        val showdownVersion: Double
    )
}