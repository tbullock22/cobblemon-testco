/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common

import com.cobblemon.mod.common.net.messages.client.battle.BattlePersistentStatusPacket
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.net.MessageBuiltEvent
import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.net.PacketHandler
import com.cobblemon.mod.common.net.messages.client.battle.BattleApplyCaptureResponsePacket
import com.cobblemon.mod.common.net.messages.client.battle.BattleCaptureEndPacket
import com.cobblemon.mod.common.net.messages.client.battle.BattleCaptureShakePacket
import com.cobblemon.mod.common.net.messages.client.battle.BattleCaptureStartPacket
import com.cobblemon.mod.common.net.messages.client.battle.BattleEndPacket
import com.cobblemon.mod.common.net.messages.client.battle.BattleFaintPacket
import com.cobblemon.mod.common.net.messages.client.battle.BattleHealthChangePacket
import com.cobblemon.mod.common.net.messages.client.battle.BattleInitializePacket
import com.cobblemon.mod.common.net.messages.client.battle.BattleMakeChoicePacket
import com.cobblemon.mod.common.net.messages.client.battle.BattleMessagePacket
import com.cobblemon.mod.common.net.messages.client.battle.BattleQueueRequestPacket
import com.cobblemon.mod.common.net.messages.client.battle.BattleSetTeamPokemonPacket
import com.cobblemon.mod.common.net.messages.client.battle.BattleSwitchPokemonPacket
import com.cobblemon.mod.common.net.messages.client.battle.BattleUpdateTeamPokemonPacket
import com.cobblemon.mod.common.net.messages.client.battle.ChallengeNotificationPacket
import com.cobblemon.mod.common.net.messages.client.data.AbilityRegistrySyncPacket
import com.cobblemon.mod.common.net.messages.client.data.MovesRegistrySyncPacket
import com.cobblemon.mod.common.net.messages.client.data.PropertiesCompletionRegistrySyncPacket
import com.cobblemon.mod.common.net.messages.client.data.SpeciesRegistrySyncPacket
import com.cobblemon.mod.common.net.messages.client.pokemon.update.AbilityUpdatePacket
import com.cobblemon.mod.common.net.messages.client.pokemon.update.AspectsUpdatePacket
import com.cobblemon.mod.common.net.messages.client.pokemon.update.BenchedMovesUpdatePacket
import com.cobblemon.mod.common.net.messages.client.pokemon.update.CaughtBallUpdatePacket
import com.cobblemon.mod.common.net.messages.client.pokemon.update.EVsUpdatePacket
import com.cobblemon.mod.common.net.messages.client.pokemon.update.ExperienceUpdatePacket
import com.cobblemon.mod.common.net.messages.client.pokemon.update.FriendshipUpdatePacket
import com.cobblemon.mod.common.net.messages.client.pokemon.update.GenderUpdatePacket
import com.cobblemon.mod.common.net.messages.client.pokemon.update.HealthUpdatePacket
import com.cobblemon.mod.common.net.messages.client.pokemon.update.IVsUpdatePacket
import com.cobblemon.mod.common.net.messages.client.pokemon.update.MoveSetUpdatePacket
import com.cobblemon.mod.common.net.messages.client.pokemon.update.NatureUpdatePacket
import com.cobblemon.mod.common.net.messages.client.pokemon.update.PokemonStateUpdatePacket
import com.cobblemon.mod.common.net.messages.client.pokemon.update.ShinyUpdatePacket
import com.cobblemon.mod.common.net.messages.client.pokemon.update.SpeciesUpdatePacket
import com.cobblemon.mod.common.net.messages.client.pokemon.update.StatusUpdatePacket
import com.cobblemon.mod.common.net.messages.client.pokemon.update.evolution.AddEvolutionPacket
import com.cobblemon.mod.common.net.messages.client.pokemon.update.evolution.ClearEvolutionsPacket
import com.cobblemon.mod.common.net.messages.client.pokemon.update.evolution.RemoveEvolutionPacket
import com.cobblemon.mod.common.net.messages.client.settings.ServerSettingsPacket
import com.cobblemon.mod.common.net.messages.client.starter.OpenStarterUIPacket
import com.cobblemon.mod.common.net.messages.client.starter.SetClientPlayerDataPacket
import com.cobblemon.mod.common.net.messages.client.storage.RemoveClientPokemonPacket
import com.cobblemon.mod.common.net.messages.client.storage.SwapClientPokemonPacket
import com.cobblemon.mod.common.net.messages.client.storage.party.InitializePartyPacket
import com.cobblemon.mod.common.net.messages.client.storage.party.MoveClientPartyPokemonPacket
import com.cobblemon.mod.common.net.messages.client.storage.party.SetPartyPokemonPacket
import com.cobblemon.mod.common.net.messages.client.storage.party.SetPartyReferencePacket
import com.cobblemon.mod.common.net.messages.client.storage.pc.ClosePCPacket
import com.cobblemon.mod.common.net.messages.client.storage.pc.InitializePCPacket
import com.cobblemon.mod.common.net.messages.client.storage.pc.MoveClientPCPokemonPacket
import com.cobblemon.mod.common.net.messages.client.storage.pc.OpenPCPacket
import com.cobblemon.mod.common.net.messages.client.storage.pc.SetPCBoxPokemonPacket
import com.cobblemon.mod.common.net.messages.client.storage.pc.SetPCPokemonPacket
import com.cobblemon.mod.common.net.messages.client.ui.SummaryUIPacket
import com.cobblemon.mod.common.net.messages.server.BattleChallengePacket
import com.cobblemon.mod.common.net.messages.server.BenchMovePacket
import com.cobblemon.mod.common.net.messages.server.RequestMoveSwapPacket
import com.cobblemon.mod.common.net.messages.server.SelectStarterPacket
import com.cobblemon.mod.common.net.messages.server.SendOutPokemonPacket
import com.cobblemon.mod.common.net.messages.server.battle.BattleSelectActionsPacket
import com.cobblemon.mod.common.net.messages.server.pokemon.update.evolution.AcceptEvolutionPacket
import com.cobblemon.mod.common.net.messages.server.starter.RequestStarterScreenPacket
import com.cobblemon.mod.common.net.messages.server.storage.SwapPCPartyPokemonPacket
import com.cobblemon.mod.common.net.messages.server.storage.party.MovePartyPokemonPacket
import com.cobblemon.mod.common.net.messages.server.storage.party.ReleasePartyPokemonPacket
import com.cobblemon.mod.common.net.messages.server.storage.party.SwapPartyPokemonPacket
import com.cobblemon.mod.common.net.messages.server.storage.pc.MovePCPokemonPacket
import com.cobblemon.mod.common.net.messages.server.storage.pc.MovePCPokemonToPartyPacket
import com.cobblemon.mod.common.net.messages.server.storage.pc.MovePartyPokemonToPCPacket
import com.cobblemon.mod.common.net.messages.server.storage.pc.ReleasePCPokemonPacket
import com.cobblemon.mod.common.net.messages.server.storage.pc.SwapPCPokemonPacket
import com.cobblemon.mod.common.util.getServer
import java.util.concurrent.CompletableFuture
import net.minecraft.server.network.ServerPlayerEntity

/**
 * Registers Cobblemon packets. Packet handlers are set up on handling the [MessageBuiltEvent] dispatched from here.
 *
 * This class also contains short functions for dispatching our packets to a player, all players, or to the entire server.
 *
 * @author Hiroku
 * @since November 27th, 2021
 */
object CobblemonNetwork {
    const val PROTOCOL_VERSION = "1"

    var clientHandlersRegistered = CompletableFuture<Unit>()
    var serverHandlersRegistered = CompletableFuture<Unit>()

    init {
        clientHandlersRegistered.runAfterBoth(serverHandlersRegistered) { register() }
    }

    lateinit var networkDelegate: NetworkDelegate


    fun ServerPlayerEntity.sendPacket(packet: NetworkPacket) = sendToPlayer(this, packet)
    fun sendToPlayer(player: ServerPlayerEntity, packet: NetworkPacket) = networkDelegate.sendPacketToPlayer(player, packet)
    fun sendToServer(packet: NetworkPacket) = networkDelegate.sendPacketToServer(packet)
    fun sendToAllPlayers(packet: NetworkPacket) = sendToPlayers(getServer()!!.playerManager.playerList, packet)
    fun sendToPlayers(players: Iterable<ServerPlayerEntity>, packet: NetworkPacket) = players.forEach { sendToPlayer(it, packet) }

    interface PreparedMessage<T : NetworkPacket> {
        fun registerMessage()
        fun registerHandler(handler: PacketHandler<T>)
    }

    interface NetworkContext {
        val player: ServerPlayerEntity?
    }


    fun register() {
        // Don't forget to register handlers in either ClientPacketRegistrar or ServerPacketRegistrar!

        /**
         * Client Packets
         */

        // Pokemon Update Packets
        buildClientMessage<FriendshipUpdatePacket>()
        buildClientMessage<MoveSetUpdatePacket>()
        buildClientMessage<NatureUpdatePacket>()
        buildClientMessage<ShinyUpdatePacket>()
        buildClientMessage<SpeciesUpdatePacket>()
        buildClientMessage<HealthUpdatePacket>()
        buildClientMessage<ExperienceUpdatePacket>()
        buildClientMessage<StatusUpdatePacket>()
        buildClientMessage<CaughtBallUpdatePacket>()
        buildClientMessage<BenchedMovesUpdatePacket>()
        buildClientMessage<GenderUpdatePacket>()
        buildClientMessage<AspectsUpdatePacket>()
        buildClientMessage<AbilityUpdatePacket>()
        buildClientMessage<EVsUpdatePacket>()
        buildClientMessage<IVsUpdatePacket>()

        // Evolution start
        buildClientMessage<AddEvolutionPacket>()
        buildClientMessage<ClearEvolutionsPacket>()
        buildClientMessage<RemoveEvolutionPacket>()
        // Evolution End

        buildClientMessage<PokemonStateUpdatePacket>()

        // Storage Packets
        buildClientMessage<InitializePartyPacket>()
        buildClientMessage<SetPartyPokemonPacket>()
        buildClientMessage<MoveClientPartyPokemonPacket>()
        buildClientMessage<SetPartyReferencePacket>()

        buildClientMessage<InitializePCPacket>()
        buildClientMessage<MoveClientPCPokemonPacket>()
        buildClientMessage<SetPCBoxPokemonPacket>()
        buildClientMessage<SetPCPokemonPacket>()
        buildClientMessage<OpenPCPacket>()
        buildClientMessage<ClosePCPacket>()

        buildClientMessage<SwapClientPokemonPacket>()
        buildClientMessage<RemoveClientPokemonPacket>()

        // UI Packets
        buildClientMessage<SummaryUIPacket>()

        // Starter packets
        buildClientMessage<OpenStarterUIPacket>()
        buildClientMessage<SetClientPlayerDataPacket>()

        // Battle packets
        buildClientMessage<BattleEndPacket>()
        buildClientMessage<BattleInitializePacket>()
        buildClientMessage<BattleQueueRequestPacket>()
        buildClientMessage<BattleFaintPacket>()
        buildClientMessage<BattleMakeChoicePacket>()
        buildClientMessage<BattleHealthChangePacket>()
        buildClientMessage<BattleSetTeamPokemonPacket>()
        buildClientMessage<BattleSwitchPokemonPacket>()
        buildClientMessage<BattleMessagePacket>()
        buildClientMessage<BattleCaptureStartPacket>()
        buildClientMessage<BattleCaptureEndPacket>()
        buildClientMessage<BattleCaptureShakePacket>()
        buildClientMessage<BattleApplyCaptureResponsePacket>()
        buildClientMessage<ChallengeNotificationPacket>()
        buildClientMessage<BattleUpdateTeamPokemonPacket>()
        buildClientMessage<BattlePersistentStatusPacket>()

        // Settings packets
        buildClientMessage<ServerSettingsPacket>()

        // Data registries
        buildClientMessage<AbilityRegistrySyncPacket>()
        buildClientMessage<MovesRegistrySyncPacket>()
        buildClientMessage<SpeciesRegistrySyncPacket>()
        buildClientMessage<PropertiesCompletionRegistrySyncPacket>()

        /**
         * Server Packets
         */

        // Pokemon Update Packets
        // Evolution start
        buildServerMessage<AcceptEvolutionPacket>()
        // Evolution End

        // Storage Packets
        buildServerMessage<SendOutPokemonPacket>()
        buildServerMessage<RequestMoveSwapPacket>()
        buildServerMessage<BenchMovePacket>()
        buildServerMessage<BattleChallengePacket>()

        buildServerMessage<MovePCPokemonToPartyPacket>()
        buildServerMessage<MovePartyPokemonToPCPacket>()
        buildServerMessage<ReleasePartyPokemonPacket>()
        buildServerMessage<ReleasePCPokemonPacket>()

        // Starter packets
        buildServerMessage<SelectStarterPacket>()
        buildServerMessage<RequestStarterScreenPacket>()

        buildServerMessage<SwapPCPokemonPacket>()
        buildServerMessage<SwapPartyPokemonPacket>()

        buildServerMessage<MovePCPokemonPacket>()
        buildServerMessage<MovePartyPokemonPacket>()

        buildServerMessage<SwapPCPartyPokemonPacket>()

        // Battle packets
        buildServerMessage<BattleSelectActionsPacket>()
    }

    private inline fun <reified P : NetworkPacket> buildClientMessage() =
        buildMessage<P>(toServer = false)
    private inline fun <reified P : NetworkPacket> buildServerMessage() =
        buildMessage<P>(toServer = true)

    private inline fun <reified P : NetworkPacket> buildMessage(toServer: Boolean) {
        val message = networkDelegate.buildMessage(P::class.java, toServer)
        // Gives client or server the chance to attach its packet handler
        CobblemonEvents.MESSAGE_BUILT.post(MessageBuiltEvent(P::class.java, toServer, message))
        message.registerMessage()
    }
}