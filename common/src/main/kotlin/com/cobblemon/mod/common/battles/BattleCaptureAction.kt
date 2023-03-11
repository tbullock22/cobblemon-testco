/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.battles

import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.reactive.Observable.Companion.emitWhile
import com.cobblemon.mod.common.api.text.green
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.entity.pokeball.EmptyPokeBallEntity
import com.cobblemon.mod.common.net.messages.client.battle.BattleCaptureEndPacket
import com.cobblemon.mod.common.net.messages.client.battle.BattleCaptureShakePacket
import com.cobblemon.mod.common.net.messages.client.battle.BattleCaptureStartPacket
import com.cobblemon.mod.common.util.lang

/**
 * Wrapper object for an attempt at capturing a wild Pokémon during a battle.
 *
 * @author Hiroku
 * @since July 2nd, 2022
 */
class BattleCaptureAction(
    val battle: PokemonBattle,
    val targetPokemon: ActiveBattlePokemon,
    val pokeBallEntity: EmptyPokeBallEntity
) {
    val pokemonName = targetPokemon.battlePokemon?.getName() ?: "error".red()
    fun attach() {
        battle.sendUpdate(BattleCaptureStartPacket(pokeBallEntity.pokeBall.name, pokeBallEntity.aspects.get(), targetPokemon.getPNX()))

        pokeBallEntity.shakeEmitter
            .pipe(emitWhile { pokeBallEntity.isAlive && this in battle.captureActions })
            .subscribe { battle.sendUpdate(BattleCaptureShakePacket(targetPokemon.getPNX(), it)) }

        pokeBallEntity.captureFuture.thenAccept { successful ->
            if (successful) {
                targetPokemon.battlePokemon?.gone = true
                battle.writeShowdownAction(">capture ${targetPokemon.getPNX()}")
                battle.broadcastChatMessage(lang("capture.succeeded", pokemonName).green())
            } else {
                battle.broadcastChatMessage(lang("capture.broke_free", pokemonName).red())
            }
            battle.sendUpdate(BattleCaptureEndPacket(targetPokemon.getPNX(), successful))
            battle.finishCaptureAction(this)
        }
    }
}
