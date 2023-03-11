/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling

import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.net.SidedPacketRegistrar
import com.cobblemon.mod.common.net.messages.server.pokemon.update.evolution.AcceptEvolutionPacket
import com.cobblemon.mod.common.net.serverhandling.battle.BattleSelectActionsHandler
import com.cobblemon.mod.common.net.serverhandling.evolution.EvolutionDisplayUpdatePacketHandler
import com.cobblemon.mod.common.net.serverhandling.pokemon.interact.InteractPokemonHandler
import com.cobblemon.mod.common.net.serverhandling.starter.RequestStarterScreenHandler
import com.cobblemon.mod.common.net.serverhandling.starter.SelectStarterPacketHandler
import com.cobblemon.mod.common.net.serverhandling.storage.BenchMoveHandler
import com.cobblemon.mod.common.net.serverhandling.storage.RequestMoveSwapHandler
import com.cobblemon.mod.common.net.serverhandling.storage.SendOutPokemonHandler
import com.cobblemon.mod.common.net.serverhandling.storage.SwapPCPartyPokemonHandler
import com.cobblemon.mod.common.net.serverhandling.storage.party.MovePartyPokemonHandler
import com.cobblemon.mod.common.net.serverhandling.storage.party.ReleasePCPokemonHandler
import com.cobblemon.mod.common.net.serverhandling.storage.party.SwapPartyPokemonHandler
import com.cobblemon.mod.common.net.serverhandling.storage.pc.*

/**
 * Registers packet handlers that the server will need. This is separated from the client ones
 * not because they have to be, but because it helps us guarantee client access safety in a CI
 * job.
 *
 * @author Hiroku
 * @since November 27th, 2021
 */
object ServerPacketRegistrar : SidedPacketRegistrar() {
    override fun registerHandlers() {
        // Don't forget to register packets in CobblemonNetwork!

        registerHandler(SendOutPokemonHandler)
        registerHandler(RequestMoveSwapHandler)
        registerHandler(BenchMoveHandler)
        registerHandler(ChallengeHandler)
        registerHandler<AcceptEvolutionPacket>(EvolutionDisplayUpdatePacketHandler())
        registerHandler(BattleSelectActionsHandler)
        registerHandler(SelectStarterPacketHandler)

        registerHandler(InteractPokemonHandler)

        // PC actions
        registerHandler(MovePCPokemonHandler)
        registerHandler(SwapPCPokemonHandler)
        registerHandler(MovePCPokemonToPartyHandler)
        registerHandler(MovePartyPokemonToPCHandler)
        registerHandler(ReleasePCPokemonHandler)
        registerHandler(UnlinkPlayerFromPCHandler)

        // Party actions
        registerHandler(MovePartyPokemonHandler)
        registerHandler(SwapPartyPokemonHandler)
        registerHandler(ReleasePartyPokemonHandler)

        // PC and Party actions :)
        registerHandler(SwapPCPartyPokemonHandler)

        registerHandler(RequestStarterScreenHandler)

        CobblemonNetwork.serverHandlersRegistered.complete(Unit)
    }
}