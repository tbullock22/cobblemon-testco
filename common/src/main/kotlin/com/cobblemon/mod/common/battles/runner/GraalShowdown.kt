/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.battles.runner

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.battles.BattleRegistry
import com.cobblemon.mod.common.battles.ShowdownInterpreter
import java.io.File
import java.util.UUID
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.HostAccess
import org.graalvm.polyglot.PolyglotAccess
import org.graalvm.polyglot.Value

object GraalShowdown {
    @Transient
    lateinit var context: Context
    @Transient
    lateinit var sendBattleMessageFunction: Value
    fun createContext() {
        val access = HostAccess.newBuilder(HostAccess.ALL).build()
        context = Context.newBuilder("js")
            .allowIO(true)
            .allowExperimentalOptions(true)
            .allowPolyglotAccess(PolyglotAccess.ALL)
            .allowHostAccess(access)
            .allowAllAccess(true)
            .allowCreateThread(true)
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

    fun boot() {
        context.eval("js", File("showdown/index.js").readText())
        sendBattleMessageFunction = context.getBindings("js").getMember("sendBattleMessage")
    }

    fun startBattle(battle: PokemonBattle, messages: Array<String>) {
        val startBattleFunction = context.getBindings("js").getMember("startBattle")
        startBattleFunction.execute(GraalShowdown, battle.battleId.toString(), messages)
    }

    fun sendFromShowdown(battleId: String, message: String) {
        ShowdownInterpreter.interpretMessage(UUID.fromString(battleId), message)
    }

    fun sendToShowdown(battleId: UUID, messages: Array<String>) {
        sendBattleMessageFunction.execute(battleId.toString(), messages)
    }
}