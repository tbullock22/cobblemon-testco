/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.client.net.SetClientPlayerDataHandler
import com.cobblemon.mod.common.client.net.battle.*
import com.cobblemon.mod.common.client.net.data.DataRegistrySyncPacketHandler
import com.cobblemon.mod.common.client.net.effect.SpawnSnowstormParticleHandler
import com.cobblemon.mod.common.client.net.gui.InteractPokemonUIPacketHandler
import com.cobblemon.mod.common.client.net.gui.SummaryUIPacketHandler
import com.cobblemon.mod.common.client.net.pokemon.update.PokemonUpdatePacketHandler
import com.cobblemon.mod.common.client.net.settings.ServerSettingsPacketHandler
import com.cobblemon.mod.common.client.net.sound.UnvalidatedPlaySoundS2CPacketHandler
import com.cobblemon.mod.common.client.net.spawn.SpawnExtraDataEntityHandler
import com.cobblemon.mod.common.client.net.starter.StarterUIPacketHandler
import com.cobblemon.mod.common.client.net.storage.RemoveClientPokemonHandler
import com.cobblemon.mod.common.client.net.storage.SwapClientPokemonHandler
import com.cobblemon.mod.common.client.net.storage.party.InitializePartyHandler
import com.cobblemon.mod.common.client.net.storage.party.MoveClientPartyPokemonHandler
import com.cobblemon.mod.common.client.net.storage.party.SetPartyPokemonHandler
import com.cobblemon.mod.common.client.net.storage.party.SetPartyReferenceHandler
import com.cobblemon.mod.common.client.net.storage.pc.*
import com.cobblemon.mod.common.net.messages.client.battle.*
import com.cobblemon.mod.common.net.messages.client.data.*
import com.cobblemon.mod.common.net.messages.client.data.PropertiesCompletionRegistrySyncPacket
import com.cobblemon.mod.common.net.messages.client.data.SpeciesRegistrySyncPacket
import com.cobblemon.mod.common.net.messages.client.effect.SpawnSnowstormParticlePacket
import com.cobblemon.mod.common.net.messages.client.pokemon.update.*
import com.cobblemon.mod.common.net.messages.client.pokemon.update.evolution.AddEvolutionPacket
import com.cobblemon.mod.common.net.messages.client.pokemon.update.evolution.ClearEvolutionsPacket
import com.cobblemon.mod.common.net.messages.client.pokemon.update.evolution.RemoveEvolutionPacket
import com.cobblemon.mod.common.net.messages.client.settings.ServerSettingsPacket
import com.cobblemon.mod.common.net.messages.client.sound.UnvalidatedPlaySoundS2CPacket
import com.cobblemon.mod.common.net.messages.client.spawn.SpawnPokeballPacket
import com.cobblemon.mod.common.net.messages.client.spawn.SpawnPokemonPacket
import com.cobblemon.mod.common.net.messages.client.starter.OpenStarterUIPacket
import com.cobblemon.mod.common.net.messages.client.starter.SetClientPlayerDataPacket
import com.cobblemon.mod.common.net.messages.client.storage.RemoveClientPokemonPacket
import com.cobblemon.mod.common.net.messages.client.storage.SwapClientPokemonPacket
import com.cobblemon.mod.common.net.messages.client.storage.party.InitializePartyPacket
import com.cobblemon.mod.common.net.messages.client.storage.party.MoveClientPartyPokemonPacket
import com.cobblemon.mod.common.net.messages.client.storage.party.SetPartyPokemonPacket
import com.cobblemon.mod.common.net.messages.client.storage.party.SetPartyReferencePacket
import com.cobblemon.mod.common.net.messages.client.storage.pc.*
import com.cobblemon.mod.common.net.messages.client.ui.InteractPokemonUIPacket
import com.cobblemon.mod.common.net.messages.client.ui.SummaryUIPacket
import com.cobblemon.mod.common.net.messages.server.*
import com.cobblemon.mod.common.net.messages.server.battle.BattleSelectActionsPacket
import com.cobblemon.mod.common.net.messages.server.pokemon.interact.InteractPokemonPacket
import com.cobblemon.mod.common.net.messages.server.pokemon.update.evolution.AcceptEvolutionPacket
import com.cobblemon.mod.common.net.messages.server.starter.RequestStarterScreenPacket
import com.cobblemon.mod.common.net.messages.server.storage.SwapPCPartyPokemonPacket
import com.cobblemon.mod.common.net.messages.server.storage.party.MovePartyPokemonPacket
import com.cobblemon.mod.common.net.messages.server.storage.party.ReleasePartyPokemonPacket
import com.cobblemon.mod.common.net.messages.server.storage.party.SwapPartyPokemonPacket
import com.cobblemon.mod.common.net.messages.server.storage.pc.*
import com.cobblemon.mod.common.net.serverhandling.ChallengeHandler
import com.cobblemon.mod.common.net.serverhandling.battle.BattleSelectActionsHandler
import com.cobblemon.mod.common.net.serverhandling.evolution.AcceptEvolutionHandler
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
import com.cobblemon.mod.common.util.server
import kotlin.reflect.KClass
import net.minecraft.network.Packet
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier

/**
 * Registers Cobblemon packets. Packet handlers are set up on handling the [MessageBuiltEvent] dispatched from here.
 *
 * This class also contains short functions for dispatching our packets to a player, all players, or to the entire server.
 *
 * @author Hiroku
 * @since November 27th, 2021
 */
object CobblemonNetwork : NetworkManager {

    fun ServerPlayerEntity.sendPacket(packet: NetworkPacket<*>) = sendPacketToPlayer(this, packet)
    fun sendToServer(packet: NetworkPacket<*>) = this.sendPacketToServer(packet)
    fun sendToAllPlayers(packet: NetworkPacket<*>) = sendPacketToPlayers(server()!!.playerManager.playerList, packet)
    fun sendPacketToPlayers(players: Iterable<ServerPlayerEntity>, packet: NetworkPacket<*>) = players.forEach { sendPacketToPlayer(it, packet) }

    override fun initClient() {
        // Evolution Packets
        this.registerServerBound(AcceptEvolutionPacket.ID, AcceptEvolutionPacket::decode, AcceptEvolutionHandler)

        // Interaction Packets
        this.registerServerBound(InteractPokemonPacket.ID, InteractPokemonPacket::decode, InteractPokemonHandler)

        // Storage Packets
        this.registerServerBound(SendOutPokemonPacket.ID, SendOutPokemonPacket::decode, SendOutPokemonHandler)
        this.registerServerBound(RequestMoveSwapPacket.ID, RequestMoveSwapPacket::decode, RequestMoveSwapHandler)
        this.registerServerBound(BenchMovePacket.ID, BenchMovePacket::decode, BenchMoveHandler)
        this.registerServerBound(BattleChallengePacket.ID, BattleChallengePacket::decode, ChallengeHandler)

        this.registerServerBound(MovePCPokemonToPartyPacket.ID, MovePCPokemonToPartyPacket::decode, MovePCPokemonToPartyHandler)
        this.registerServerBound(MovePartyPokemonToPCPacket.ID, MovePartyPokemonToPCPacket::decode, MovePartyPokemonToPCHandler)
        this.registerServerBound(ReleasePartyPokemonPacket.ID, ReleasePartyPokemonPacket::decode, ReleasePartyPokemonHandler)
        this.registerServerBound(ReleasePCPokemonPacket.ID, ReleasePCPokemonPacket::decode, ReleasePCPokemonHandler)
        this.registerServerBound(UnlinkPlayerFromPCPacket.ID, UnlinkPlayerFromPCPacket::decode, UnlinkPlayerFromPCHandler)

        // Starter packets
        this.registerServerBound(SelectStarterPacket.ID, SelectStarterPacket::decode, SelectStarterPacketHandler)
        this.registerServerBound(RequestStarterScreenPacket.ID, RequestStarterScreenPacket::decode, RequestStarterScreenHandler)

        this.registerServerBound(SwapPCPokemonPacket.ID, SwapPCPokemonPacket::decode, SwapPCPokemonHandler)
        this.registerServerBound(SwapPartyPokemonPacket.ID, SwapPartyPokemonPacket::decode, SwapPartyPokemonHandler)

        this.registerServerBound(MovePCPokemonPacket.ID, MovePCPokemonPacket::decode, MovePCPokemonHandler)
        this.registerServerBound(MovePartyPokemonPacket.ID, MovePartyPokemonPacket::decode, MovePartyPokemonHandler)

        this.registerServerBound(SwapPCPartyPokemonPacket.ID, SwapPCPartyPokemonPacket::decode, SwapPCPartyPokemonHandler)

        // Battle packets
        this.registerServerBound(BattleSelectActionsPacket.ID, BattleSelectActionsPacket::decode, BattleSelectActionsHandler)
    }

    override fun initServer() {
        // Pokemon Update Packets
        this.registerClientBound(FriendshipUpdatePacket.ID, FriendshipUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.registerClientBound(MoveSetUpdatePacket.ID, MoveSetUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.registerClientBound(NatureUpdatePacket.ID, NatureUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.registerClientBound(ShinyUpdatePacket.ID, ShinyUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.registerClientBound(SpeciesUpdatePacket.ID, SpeciesUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.registerClientBound(HealthUpdatePacket.ID, HealthUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.registerClientBound(ExperienceUpdatePacket.ID, ExperienceUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.registerClientBound(StatusUpdatePacket.ID, StatusUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.registerClientBound(CaughtBallUpdatePacket.ID, CaughtBallUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.registerClientBound(BenchedMovesUpdatePacket.ID, BenchedMovesUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.registerClientBound(GenderUpdatePacket.ID, GenderUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.registerClientBound(AspectsUpdatePacket.ID, AspectsUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.registerClientBound(AbilityUpdatePacket.ID, AbilityUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.registerClientBound(EVsUpdatePacket.ID, EVsUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.registerClientBound(IVsUpdatePacket.ID, IVsUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.registerClientBound(HeldItemUpdatePacket.ID, HeldItemUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.registerClientBound(PokemonStateUpdatePacket.ID, PokemonStateUpdatePacket::decode, PokemonUpdatePacketHandler())

        // Evolution start
        this.registerClientBound(AddEvolutionPacket.ID, AddEvolutionPacket::decode, PokemonUpdatePacketHandler())
        this.registerClientBound(ClearEvolutionsPacket.ID, ClearEvolutionsPacket::decode, PokemonUpdatePacketHandler())
        this.registerClientBound(RemoveEvolutionPacket.ID, RemoveEvolutionPacket::decode, PokemonUpdatePacketHandler())
        // Evolution End

        // Storage Packets
        this.registerClientBound(InitializePartyPacket.ID, InitializePartyPacket::decode, InitializePartyHandler)
        this.registerClientBound(SetPartyPokemonPacket.ID, SetPartyPokemonPacket::decode, SetPartyPokemonHandler)
        this.registerClientBound(MoveClientPartyPokemonPacket.ID, MoveClientPartyPokemonPacket::decode, MoveClientPartyPokemonHandler)
        this.registerClientBound(SetPartyReferencePacket.ID, SetPartyReferencePacket::decode, SetPartyReferenceHandler)

        this.registerClientBound(InitializePCPacket.ID, InitializePCPacket::decode, InitializePCHandler)
        this.registerClientBound(MoveClientPCPokemonPacket.ID, MoveClientPCPokemonPacket::decode, MoveClientPCPokemonHandler)
        this.registerClientBound(SetPCBoxPokemonPacket.ID, SetPCBoxPokemonPacket::decode, SetPCBoxPokemonHandler)
        this.registerClientBound(SetPCPokemonPacket.ID, SetPCPokemonPacket::decode, SetPCPokemonHandler)
        this.registerClientBound(OpenPCPacket.ID, OpenPCPacket::decode, OpenPCHandler)
        this.registerClientBound(ClosePCPacket.ID, ClosePCPacket::decode, ClosePCHandler)

        this.registerClientBound(SwapClientPokemonPacket.ID, SwapClientPokemonPacket::decode, SwapClientPokemonHandler)
        this.registerClientBound(RemoveClientPokemonPacket.ID, RemoveClientPokemonPacket::decode, RemoveClientPokemonHandler)

        // UI Packets
        this.registerClientBound(SummaryUIPacket.ID, SummaryUIPacket::decode, SummaryUIPacketHandler)
        this.registerClientBound(InteractPokemonUIPacket.ID, InteractPokemonUIPacket::decode, InteractPokemonUIPacketHandler)

        // Starter packets
        this.registerClientBound(OpenStarterUIPacket.ID, OpenStarterUIPacket::decode, StarterUIPacketHandler)
        this.registerClientBound(SetClientPlayerDataPacket.ID, SetClientPlayerDataPacket::decode, SetClientPlayerDataHandler)

        // Battle packets
        this.registerClientBound(BattleEndPacket.ID, BattleEndPacket::decode, BattleEndHandler)
        this.registerClientBound(BattleInitializePacket.ID, BattleInitializePacket::decode, BattleInitializeHandler)
        this.registerClientBound(BattleQueueRequestPacket.ID, BattleQueueRequestPacket::decode, BattleQueueRequestHandler)
        this.registerClientBound(BattleFaintPacket.ID, BattleFaintPacket::decode, BattleFaintHandler)
        this.registerClientBound(BattleMakeChoicePacket.ID, BattleMakeChoicePacket::decode, BattleMakeChoiceHandler)
        this.registerClientBound(BattleHealthChangePacket.ID, BattleHealthChangePacket::decode, BattleHealthChangeHandler)
        this.registerClientBound(BattleSetTeamPokemonPacket.ID, BattleSetTeamPokemonPacket::decode, BattleSetTeamPokemonHandler)
        this.registerClientBound(BattleSwitchPokemonPacket.ID, BattleSwitchPokemonPacket::decode, BattleSwitchPokemonHandler)
        this.registerClientBound(BattleMessagePacket.ID, BattleMessagePacket::decode, BattleMessageHandler)
        this.registerClientBound(BattleCaptureStartPacket.ID, BattleCaptureStartPacket::decode, BattleCaptureStartHandler)
        this.registerClientBound(BattleCaptureEndPacket.ID, BattleCaptureEndPacket::decode, BattleCaptureEndHandler)
        this.registerClientBound(BattleCaptureShakePacket.ID, BattleCaptureShakePacket::decode, BattleCaptureShakeHandler)
        this.registerClientBound(BattleApplyCaptureResponsePacket.ID, BattleApplyCaptureResponsePacket::decode, BattleApplyCaptureResponseHandler)
        this.registerClientBound(ChallengeNotificationPacket.ID, ChallengeNotificationPacket::decode, ChallengeNotificationHandler)
        this.registerClientBound(BattleUpdateTeamPokemonPacket.ID, BattleUpdateTeamPokemonPacket::decode, BattleUpdateTeamPokemonHandler)
        this.registerClientBound(BattlePersistentStatusPacket.ID, BattlePersistentStatusPacket::decode, BattlePersistentStatusHandler)
        this.registerClientBound(BattleTransformPacket.ID, BattleTransformPacket::decode, BattleTransformPacketHandler)

        // Settings packets
        this.registerClientBound(ServerSettingsPacket.ID, ServerSettingsPacket::decode, ServerSettingsPacketHandler)

        // Data registries
        this.registerClientBound(AbilityRegistrySyncPacket.ID, AbilityRegistrySyncPacket::decode, DataRegistrySyncPacketHandler())
        this.registerClientBound(MovesRegistrySyncPacket.ID, MovesRegistrySyncPacket::decode, DataRegistrySyncPacketHandler())
        this.registerClientBound(SpeciesRegistrySyncPacket.ID, SpeciesRegistrySyncPacket::decode, DataRegistrySyncPacketHandler())
        this.registerClientBound(PropertiesCompletionRegistrySyncPacket.ID, PropertiesCompletionRegistrySyncPacket::decode, DataRegistrySyncPacketHandler())

        // Effects
        this.registerClientBound(SpawnSnowstormParticlePacket.ID, SpawnSnowstormParticlePacket::decode, SpawnSnowstormParticleHandler)

        // Hax
        this.registerClientBound(UnvalidatedPlaySoundS2CPacket.ID, UnvalidatedPlaySoundS2CPacket::decode, UnvalidatedPlaySoundS2CPacketHandler)
        this.registerClientBound(SpawnPokemonPacket.ID, SpawnPokemonPacket::decode, SpawnExtraDataEntityHandler())
        this.registerClientBound(SpawnPokeballPacket.ID, SpawnPokeballPacket::decode, SpawnExtraDataEntityHandler())
    }

    private inline fun <reified T : NetworkPacket<T>> registerClientBound(identifier: Identifier, noinline decoder: (PacketByteBuf) -> T, handler: ClientNetworkPacketHandler<T>) {
        Cobblemon.implementation.networkManager.registerClientBound(identifier, T::class, { message, buffer -> message.encode(buffer) }, decoder, handler)
    }

    private inline fun <reified T : NetworkPacket<T>> registerServerBound(identifier: Identifier, noinline decoder: (PacketByteBuf) -> T, handler: ServerNetworkPacketHandler<T>) {
        Cobblemon.implementation.networkManager.registerServerBound(identifier, T::class, { message, buffer -> message.encode(buffer) }, decoder, handler)
    }

    override fun <T : NetworkPacket<T>> registerClientBound(
        identifier: Identifier,
        kClass: KClass<T>,
        encoder: (T, PacketByteBuf) -> Unit,
        decoder: (PacketByteBuf) -> T,
        handler: ClientNetworkPacketHandler<T>
    ) {
        Cobblemon.implementation.networkManager.registerClientBound(identifier, kClass, encoder, decoder, handler)
    }

    override fun <T : NetworkPacket<T>> registerServerBound(
        identifier: Identifier,
        kClass: KClass<T>,
        encoder: (T, PacketByteBuf) -> Unit,
        decoder: (PacketByteBuf) -> T,
        handler: ServerNetworkPacketHandler<T>
    ) {
        Cobblemon.implementation.networkManager.registerServerBound(identifier, kClass, encoder, decoder, handler)
    }

    override fun sendPacketToPlayer(player: ServerPlayerEntity, packet: NetworkPacket<*>) = Cobblemon.implementation.networkManager.sendPacketToPlayer(player, packet)

    override fun sendPacketToServer(packet: NetworkPacket<*>) = Cobblemon.implementation.networkManager.sendPacketToServer(packet)

    override fun <T : NetworkPacket<*>> asVanillaClientBound(packet: T): Packet<ClientPlayPacketListener> = Cobblemon.implementation.networkManager.asVanillaClientBound(packet)
}