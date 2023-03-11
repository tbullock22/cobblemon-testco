/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage.player.adapter

import com.cobblemon.mod.common.api.storage.player.PlayerAdvancementData
import com.cobblemon.mod.common.api.storage.player.PlayerData
import com.cobblemon.mod.common.api.storage.player.PlayerDataExtension
import com.cobblemon.mod.common.util.fromJson
import com.google.gson.GsonBuilder
import java.io.BufferedReader
import java.io.FileReader
import java.io.PrintWriter
import java.nio.file.Path
import java.util.UUID
import net.minecraft.server.MinecraftServer
import net.minecraft.util.WorldSavePath
class JsonPlayerData: PlayerDataFileStoreAdapter {

    companion object {
        val gson = GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .registerTypeAdapter(PlayerDataExtension::class.java, PlayerDataExtensionAdapter)
            .create()
    }

    private val cache = mutableMapOf<UUID, PlayerData>()
    lateinit var savePath: Path
    var useNestedStructure = true

    fun setup(server: MinecraftServer) {
        savePath = server.getSavePath(WorldSavePath.PLAYERDATA).parent
    }

    fun getSubFile(uuid: UUID): String {
        return if (useNestedStructure) {
            "${uuid.toString().substring(0, 2)}/$uuid.json"
        } else {
            "$uuid.json"
        }
    }
    private fun file(uuid: UUID) = savePath.resolve("cobblemonplayerdata/${getSubFile(uuid)}").toFile()

    override fun load(uuid: UUID): PlayerData {
        if (cache.contains(uuid)) {
            return cache[uuid]!!
        }

        val playerFile = file(uuid)
        playerFile.parentFile.mkdirs()
        return if (playerFile.exists()) {
            gson.fromJson<PlayerData>(BufferedReader(FileReader(playerFile))).also {
                cache[uuid] = it
                // Resolves old data from pre 1.0, don't even ask man. - Hiroku
                if (it.advancementData == null) {
                    it.advancementData = PlayerAdvancementData()
                }
            }
        } else {
            PlayerData.default(uuid).also(::save)
        }
    }

    fun saveCache() {
        cache.forEach { (_, pd) -> save(pd)}
        cache.clear()
    }

    override fun save(playerData: PlayerData) {
        val file = file(playerData.uuid)
        file.parentFile.mkdirs()
        val pw = PrintWriter(file(playerData.uuid))
        pw.write(gson.toJson(playerData))
        pw.flush()
        pw.close()
        cache[playerData.uuid] = playerData
    }
}