package com.cobblemon.mod.common.battles.runner.graal

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.util.FileUtils
import com.cobblemon.mod.common.util.extractTo
import com.cobblemon.mod.common.util.fromJson
import com.google.gson.GsonBuilder
import net.minecraft.util.Identifier
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

/**
 * Unbundles a zipped Showdown file found within {@code resources/assets/showdown.zip} for use within the GraalShowdownService.
 * This will do a metadata check prior to unbundling and stop if there is already an unbundled showdown package
 * of the same version.
 *
 * @since  February 27, 2023
 * @author Deltric
 */
class GraalShowdownUnbundler {

    private val gson = GsonBuilder()
        .disableHtmlEscaping()
        .create()

    fun attemptUnbundle() {
        var showdownDir = File("showdown")
        val metadata = loadShowdownMetadata()

        // Check if showdown needs to be installed
        if (!showdownDir.exists() || Cobblemon.config.autoUpdateShowdown) {
            val showdownZip = File(showdownDir, "showdown.zip")
            showdownZip.mkdirs()
            val showdownMetadataFile = File(showdownDir, "showdown.json")

            var extract = true
            if (showdownMetadataFile.exists()) {
                val current = this.readShowdownMetadata(showdownMetadataFile)
                if (metadata!!.showdownVersion == current!!.showdownVersion && false) {
                    extract = false
                } else {
                    // Backup current install first before continuing
                    Cobblemon.LOGGER.info("Updating showdown service to version ${metadata.showdownVersion}, from version ${current.showdownVersion}...")

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