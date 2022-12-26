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
import com.cobblemon.mod.common.battles.runner.GraalShowdown
import com.cobblemon.mod.common.util.FileUtils
import com.cobblemon.mod.common.util.extractTo
import com.cobblemon.mod.common.util.fromJson
import com.google.gson.GsonBuilder
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.concurrent.CompletableFuture
import net.minecraft.util.Identifier

class ShowdownThread : Thread("Cobblemon Showdown") {

    var showdownStarted = CompletableFuture<Unit>()
    val gson = GsonBuilder()
        .disableHtmlEscaping()
        .create()

    override fun run() {
        var showdownDir = File("showdown")
        showdownDir.mkdir()

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
        GraalShowdown.createContext()
        GraalShowdown.boot()

        LOGGER.info("Showdown has been started!")
        showdownStarted.complete(Unit)
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