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
import com.cobblemon.mod.common.client.net.data.UnlockReloadPacketHandler
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
import net.minecraft.network.packet.Packet
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

    override fun registerClientBound() {
        // Pokemon Update Packets
        this.createClientBound(FriendshipUpdatePacket.ID, FriendshipUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.createClientBound(MoveSetUpdatePacket.ID, MoveSetUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.createClientBound(NatureUpdatePacket.ID, NatureUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.createClientBound(ShinyUpdatePacket.ID, ShinyUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.createClientBound(SpeciesUpdatePacket.ID, SpeciesUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.createClientBound(HealthUpdatePacket.ID, HealthUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.createClientBound(ExperienceUpdatePacket.ID, ExperienceUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.createClientBound(StatusUpdatePacket.ID, StatusUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.createClientBound(CaughtBallUpdatePacket.ID, CaughtBallUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.createClientBound(BenchedMovesUpdatePacket.ID, BenchedMovesUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.createClientBound(GenderUpdatePacket.ID, GenderUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.createClientBound(AspectsUpdatePacket.ID, AspectsUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.createClientBound(AbilityUpdatePacket.ID, AbilityUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.createClientBound(EVsUpdatePacket.ID, EVsUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.createClientBound(IVsUpdatePacket.ID, IVsUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.createClientBound(HeldItemUpdatePacket.ID, HeldItemUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.createClientBound(PokemonStateUpdatePacket.ID, PokemonStateUpdatePacket::decode, PokemonUpdatePacketHandler())

        // Evolution start
        this.createClientBound(AddEvolutionPacket.ID, AddEvolutionPacket::decode, PokemonUpdatePacketHandler())
        this.createClientBound(ClearEvolutionsPacket.ID, ClearEvolutionsPacket::decode, PokemonUpdatePacketHandler())
        this.createClientBound(RemoveEvolutionPacket.ID, RemoveEvolutionPacket::decode, PokemonUpdatePacketHandler())
        // Evolution End

        // Storage Packets
        this.createClientBound(InitializePartyPacket.ID, InitializePartyPacket::decode, InitializePartyHandler)
        this.createClientBound(SetPartyPokemonPacket.ID, SetPartyPokemonPacket::decode, SetPartyPokemonHandler)
        this.createClientBound(MoveClientPartyPokemonPacket.ID, MoveClientPartyPokemonPacket::decode, MoveClientPartyPokemonHandler)
        this.createClientBound(SetPartyReferencePacket.ID, SetPartyReferencePacket::decode, SetPartyReferenceHandler)

        this.createClientBound(InitializePCPacket.ID, InitializePCPacket::decode, InitializePCHandler)
        this.createClientBound(MoveClientPCPokemonPacket.ID, MoveClientPCPokemonPacket::decode, MoveClientPCPokemonHandler)
        this.createClientBound(SetPCBoxPokemonPacket.ID, SetPCBoxPokemonPacket::decode, SetPCBoxPokemonHandler)
        this.createClientBound(SetPCPokemonPacket.ID, SetPCPokemonPacket::decode, SetPCPokemonHandler)
        this.createClientBound(OpenPCPacket.ID, OpenPCPacket::decode, OpenPCHandler)
        this.createClientBound(ClosePCPacket.ID, ClosePCPacket::decode, ClosePCHandler)

        this.createClientBound(SwapClientPokemonPacket.ID, SwapClientPokemonPacket::decode, SwapClientPokemonHandler)
        this.createClientBound(RemoveClientPokemonPacket.ID, RemoveClientPokemonPacket::decode, RemoveClientPokemonHandler)

        // UI Packets
        this.createClientBound(SummaryUIPacket.ID, SummaryUIPacket::decode, SummaryUIPacketHandler)
        this.createClientBound(InteractPokemonUIPacket.ID, InteractPokemonUIPacket::decode, InteractPokemonUIPacketHandler)

        // Starter packets
        this.createClientBound(OpenStarterUIPacket.ID, OpenStarterUIPacket::decode, StarterUIPacketHandler)
        this.createClientBound(SetClientPlayerDataPacket.ID, SetClientPlayerDataPacket::decode, SetClientPlayerDataHandler)

        // Battle packets
        this.createClientBound(BattleEndPacket.ID, BattleEndPacket::decode, BattleEndHandler)
        this.createClientBound(BattleInitializePacket.ID, BattleInitializePacket::decode, BattleInitializeHandler)
        this.createClientBound(BattleQueueRequestPacket.ID, BattleQueueRequestPacket::decode, BattleQueueRequestHandler)
        this.createClientBound(BattleFaintPacket.ID, BattleFaintPacket::decode, BattleFaintHandler)
        this.createClientBound(BattleMakeChoicePacket.ID, BattleMakeChoicePacket::decode, BattleMakeChoiceHandler)
        this.createClientBound(BattleHealthChangePacket.ID, BattleHealthChangePacket::decode, BattleHealthChangeHandler)
        this.createClientBound(BattleSetTeamPokemonPacket.ID, BattleSetTeamPokemonPacket::decode, BattleSetTeamPokemonHandler)
        this.createClientBound(BattleSwitchPokemonPacket.ID, BattleSwitchPokemonPacket::decode, BattleSwitchPokemonHandler)
        this.createClientBound(BattleMessagePacket.ID, BattleMessagePacket::decode, BattleMessageHandler)
        this.createClientBound(BattleCaptureStartPacket.ID, BattleCaptureStartPacket::decode, BattleCaptureStartHandler)
        this.createClientBound(BattleCaptureEndPacket.ID, BattleCaptureEndPacket::decode, BattleCaptureEndHandler)
        this.createClientBound(BattleCaptureShakePacket.ID, BattleCaptureShakePacket::decode, BattleCaptureShakeHandler)
        this.createClientBound(BattleApplyCaptureResponsePacket.ID, BattleApplyCaptureResponsePacket::decode, BattleApplyCaptureResponseHandler)
        this.createClientBound(ChallengeNotificationPacket.ID, ChallengeNotificationPacket::decode, ChallengeNotificationHandler)
        this.createClientBound(BattleUpdateTeamPokemonPacket.ID, BattleUpdateTeamPokemonPacket::decode, BattleUpdateTeamPokemonHandler)
        this.createClientBound(BattlePersistentStatusPacket.ID, BattlePersistentStatusPacket::decode, BattlePersistentStatusHandler)

        // Settings packets
        this.createClientBound(ServerSettingsPacket.ID, ServerSettingsPacket::decode, ServerSettingsPacketHandler)

        // Data registries
        this.createClientBound(AbilityRegistrySyncPacket.ID, AbilityRegistrySyncPacket::decode, DataRegistrySyncPacketHandler())
        this.createClientBound(MovesRegistrySyncPacket.ID, MovesRegistrySyncPacket::decode, DataRegistrySyncPacketHandler())
        this.createClientBound(SpeciesRegistrySyncPacket.ID, SpeciesRegistrySyncPacket::decode, DataRegistrySyncPacketHandler())
        this.createClientBound(PropertiesCompletionRegistrySyncPacket.ID, PropertiesCompletionRegistrySyncPacket::decode, DataRegistrySyncPacketHandler())
        this.createClientBound(UnlockReloadPacket.ID, UnlockReloadPacket::decode, UnlockReloadPacketHandler)

        // Effects
        this.createClientBound(SpawnSnowstormParticlePacket.ID, SpawnSnowstormParticlePacket::decode, SpawnSnowstormParticleHandler)

        // Hax
        this.createClientBound(UnvalidatedPlaySoundS2CPacket.ID, UnvalidatedPlaySoundS2CPacket::decode, UnvalidatedPlaySoundS2CPacketHandler)
        this.createClientBound(SpawnPokemonPacket.ID, SpawnPokemonPacket::decode, SpawnExtraDataEntityHandler())
        this.createClientBound(SpawnPokeballPacket.ID, SpawnPokeballPacket::decode, SpawnExtraDataEntityHandler())
    }

    override fun registerServerBound() {
        // Evolution Packets
        this.createServerBound(AcceptEvolutionPacket.ID, AcceptEvolutionPacket::decode, AcceptEvolutionHandler)

        // Interaction Packets
        this.createServerBound(InteractPokemonPacket.ID, InteractPokemonPacket::decode, InteractPokemonHandler)

        // Storage Packets
        this.createServerBound(SendOutPokemonPacket.ID, SendOutPokemonPacket::decode, SendOutPokemonHandler)
        this.createServerBound(RequestMoveSwapPacket.ID, RequestMoveSwapPacket::decode, RequestMoveSwapHandler)
        this.createServerBound(BenchMovePacket.ID, BenchMovePacket::decode, BenchMoveHandler)
        this.createServerBound(BattleChallengePacket.ID, BattleChallengePacket::decode, ChallengeHandler)

        this.createServerBound(MovePCPokemonToPartyPacket.ID, MovePCPokemonToPartyPacket::decode, MovePCPokemonToPartyHandler)
        this.createServerBound(MovePartyPokemonToPCPacket.ID, MovePartyPokemonToPCPacket::decode, MovePartyPokemonToPCHandler)
        this.createServerBound(ReleasePartyPokemonPacket.ID, ReleasePartyPokemonPacket::decode, ReleasePartyPokemonHandler)
        this.createServerBound(ReleasePCPokemonPacket.ID, ReleasePCPokemonPacket::decode, ReleasePCPokemonHandler)
        this.createServerBound(UnlinkPlayerFromPCPacket.ID, UnlinkPlayerFromPCPacket::decode, UnlinkPlayerFromPCHandler)

        // Starter packets
        this.createServerBound(SelectStarterPacket.ID, SelectStarterPacket::decode, SelectStarterPacketHandler)
        this.createServerBound(RequestStarterScreenPacket.ID, RequestStarterScreenPacket::decode, RequestStarterScreenHandler)

        this.createServerBound(SwapPCPokemonPacket.ID, SwapPCPokemonPacket::decode, SwapPCPokemonHandler)
        this.createServerBound(SwapPartyPokemonPacket.ID, SwapPartyPokemonPacket::decode, SwapPartyPokemonHandler)

        this.createServerBound(MovePCPokemonPacket.ID, MovePCPokemonPacket::decode, MovePCPokemonHandler)
        this.createServerBound(MovePartyPokemonPacket.ID, MovePartyPokemonPacket::decode, MovePartyPokemonHandler)

        this.createServerBound(SwapPCPartyPokemonPacket.ID, SwapPCPartyPokemonPacket::decode, SwapPCPartyPokemonHandler)

        // Battle packets
        this.createServerBound(BattleSelectActionsPacket.ID, BattleSelectActionsPacket::decode, BattleSelectActionsHandler)
    }

    private inline fun <reified T : NetworkPacket<T>> createClientBound(identifier: Identifier, noinline decoder: (PacketByteBuf) -> T, handler: ClientNetworkPacketHandler<T>) {
        Cobblemon.implementation.networkManager.createClientBound(identifier, T::class, { message, buffer -> message.encode(buffer) }, decoder, handler)
    }

    private inline fun <reified T : NetworkPacket<T>> createServerBound(identifier: Identifier, noinline decoder: (PacketByteBuf) -> T, handler: ServerNetworkPacketHandler<T>) {
        Cobblemon.implementation.networkManager.createServerBound(identifier, T::class, { message, buffer -> message.encode(buffer) }, decoder, handler)
    }

    override fun <T : NetworkPacket<T>> createClientBound(
        identifier: Identifier,
        kClass: KClass<T>,
        encoder: (T, PacketByteBuf) -> Unit,
        decoder: (PacketByteBuf) -> T,
        handler: ClientNetworkPacketHandler<T>
    ) {
        Cobblemon.implementation.networkManager.createClientBound(identifier, kClass, encoder, decoder, handler)
    }

    override fun <T : NetworkPacket<T>> createServerBound(
        identifier: Identifier,
        kClass: KClass<T>,
        encoder: (T, PacketByteBuf) -> Unit,
        decoder: (PacketByteBuf) -> T,
        handler: ServerNetworkPacketHandler<T>
    ) {
        Cobblemon.implementation.networkManager.createServerBound(identifier, kClass, encoder, decoder, handler)
    }

    override fun sendPacketToPlayer(player: ServerPlayerEntity, packet: NetworkPacket<*>) = Cobblemon.implementation.networkManager.sendPacketToPlayer(player, packet)

    override fun sendPacketToServer(packet: NetworkPacket<*>) = Cobblemon.implementation.networkManager.sendPacketToServer(packet)

    override fun <T : NetworkPacket<*>> asVanillaClientBound(packet: T): Packet<ClientPlayPacketListener> = Cobblemon.implementation.networkManager.asVanillaClientBound(packet)
}