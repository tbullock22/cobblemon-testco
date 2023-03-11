/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.battles.runner.graal

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.battles.ShowdownInterpreter
import com.cobblemon.mod.common.battles.runner.ShowdownService
import com.google.gson.Gson
import com.google.gson.JsonArray
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.HostAccess
import org.graalvm.polyglot.PolyglotAccess
import org.graalvm.polyglot.Value
import java.io.File
import java.util.*

/**
 * Mediator service for communicating between the Cobblemon Minecraft mod and Cobblemon showdown service via
 * GraalVM. This directly invokes JavaScript functions provided within the showdown service.
 *
 * @see {@code cobbled-exports/cobbled-index.js} within cobbledmon-showdown repository
 * @see <a href="https://www.graalvm.org/">
 * @since February 27, 2023
 * @author Hiroku, landonjw
 */
class GraalShowdownService : ShowdownService {

    @Transient
    lateinit var context: Context
    @Transient
    lateinit var sendBattleMessageFunction: Value
    @Transient
    val unbundler = GraalShowdownUnbundler()
    @Transient
    val gson = Gson()

    override fun openConnection() {
        unbundler.attemptUnbundle()
        createContext()
        boot()
    }

    private fun createContext() {
        val access = HostAccess.newBuilder(HostAccess.ALL).build()
        context = Context.newBuilder("js")
            .allowIO(true)
            .allowExperimentalOptions(true)
            .allowPolyglotAccess(PolyglotAccess.ALL)
            .allowHostAccess(access)
            .allowAllAccess(true)
            .allowCreateThread(true)
            .logHandler(GraalLogger)
            .option("engine.WarnInterpreterOnly", "false")
            .option("js.commonjs-require", "true")
            .option("js.commonjs-require-cwd", "showdown")
            .option(
                "js.commonjs-core-modules-replacements",
                "buffer:buffer/,crypto:crypto-browserify,path:path-browserify"
            )
            .allowHostClassLoading(true)
            .allowNativeAccess(true)
            .allowCreateProcess(true)
            .build()

        context.eval("js", """
            globalThis.process = {
                cwd: function() {
                    return '';
                }
            }
        """.trimIndent())
    }

    override fun closeConnection() {
        context.close()
    }

    private fun boot() {
        context.eval("js", File("showdown/index.js").readText())
        sendBattleMessageFunction = context.getBindings("js").getMember("sendBattleMessage")
    }

    override fun startBattle(battle: PokemonBattle, messages: Array<String>) {
        val startBattleFunction = context.getBindings("js").getMember("startBattle")
        startBattleFunction.execute(this, battle.battleId.toString(), messages)
    }

    override fun send(battleId: UUID, messages: Array<String>) {
        sendToShowdown(battleId, messages)
    }

    override fun getAbilityIds(): JsonArray {
        val getCobbledAbilityIdsFn = context.getBindings("js").getMember("getCobbledAbilityIds")
        val arrayResult = getCobbledAbilityIdsFn.execute().asString()
        return gson.fromJson(arrayResult, JsonArray::class.java)
    }

    override fun getMoves(): JsonArray {
        val getCobbledMovesFn = context.getBindings("js").getMember("getCobbledMoves")
        val arrayResult = getCobbledMovesFn.execute().asString()
        return gson.fromJson(arrayResult, JsonArray::class.java)
    }

    override fun getItemIds(): JsonArray {
        val getCobbledItemIdsFn = context.getBindings("js").getMember("getCobbledItemIds")
        val arrayResult = getCobbledItemIdsFn.execute().asString()
        return gson.fromJson(arrayResult, JsonArray::class.java)
    }

    override fun indicateSpeciesInitialized() {
        val speciesInitFn = context.getBindings("js").getMember("afterCobbledSpeciesInit")
        speciesInitFn.execute()
    }

    private fun sendToShowdown(battleId: UUID, messages: Array<String>) {
        sendBattleMessageFunction.execute(battleId.toString(), messages)
    }

    fun sendFromShowdown(battleId: String, message: String) {
        ShowdownInterpreter.interpretMessage(UUID.fromString(battleId), message)
    }

    fun log(message: String) {
        Cobblemon.LOGGER.info(message)
    }
}