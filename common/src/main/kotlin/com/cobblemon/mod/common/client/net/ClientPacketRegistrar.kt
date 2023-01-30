/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net

import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.api.abilities.AbilityTemplate
import com.cobblemon.mod.common.api.moves.MoveTemplate
import com.cobblemon.mod.common.client.net.battle.BattleApplyCaptureResponseHandler
import com.cobblemon.mod.common.client.net.battle.BattleCaptureEndHandler
import com.cobblemon.mod.common.client.net.battle.BattleCaptureShakeHandler
import com.cobblemon.mod.common.client.net.battle.BattleCaptureStartHandler
import com.cobblemon.mod.common.client.net.battle.BattleEndHandler
import com.cobblemon.mod.common.client.net.battle.BattleFaintHandler
import com.cobblemon.mod.common.client.net.battle.BattleHealthChangeHandler
import com.cobblemon.mod.common.client.net.battle.BattleInitializeHandler
import com.cobblemon.mod.common.client.net.battle.BattleMakeChoiceHandler
import com.cobblemon.mod.common.client.net.battle.BattleMessageHandler
import com.cobblemon.mod.common.client.net.battle.BattlePersistentStatusHandler
import com.cobblemon.mod.common.client.net.battle.BattleQueueRequestHandler
import com.cobblemon.mod.common.client.net.battle.BattleSetTeamPokemonHandler
import com.cobblemon.mod.common.client.net.battle.BattleSwitchPokemonHandler
import com.cobblemon.mod.common.client.net.battle.BattleUpdateTeamPokemonHandler
import com.cobblemon.mod.common.client.net.battle.ChallengeNotificationHandler
import com.cobblemon.mod.common.client.net.data.DataRegistrySyncPacketHandler
import com.cobblemon.mod.common.client.net.gui.InteractPokemonUIPacketHandler
import com.cobblemon.mod.common.client.net.gui.SummaryUIPacketHandler
import com.cobblemon.mod.common.client.net.pokemon.update.EvolutionUpdatePacketHandler
import com.cobblemon.mod.common.client.net.pokemon.update.SingleUpdatePacketHandler
import com.cobblemon.mod.common.client.net.settings.ServerSettingsPacketHandler
import com.cobblemon.mod.common.client.net.sound.UnvalidatedPlaySoundS2CPacketHandler
import com.cobblemon.mod.common.client.net.starter.StarterUIPacketHandler
import com.cobblemon.mod.common.client.net.storage.RemoveClientPokemonHandler
import com.cobblemon.mod.common.client.net.storage.SwapClientPokemonHandler
import com.cobblemon.mod.common.client.net.storage.party.InitializePartyHandler
import com.cobblemon.mod.common.client.net.storage.party.MoveClientPartyPokemonHandler
import com.cobblemon.mod.common.client.net.storage.party.SetPartyPokemonHandler
import com.cobblemon.mod.common.client.net.storage.party.SetPartyReferenceHandler
import com.cobblemon.mod.common.client.net.storage.pc.ClosePCHandler
import com.cobblemon.mod.common.client.net.storage.pc.InitializePCHandler
import com.cobblemon.mod.common.client.net.storage.pc.MoveClientPCPokemonHandler
import com.cobblemon.mod.common.client.net.storage.pc.OpenPCHandler
import com.cobblemon.mod.common.client.net.storage.pc.SetPCBoxPokemonHandler
import com.cobblemon.mod.common.client.net.storage.pc.SetPCPokemonHandler
import com.cobblemon.mod.common.net.SidedPacketRegistrar
import com.cobblemon.mod.common.net.messages.client.data.AbilityRegistrySyncPacket
import com.cobblemon.mod.common.net.messages.client.data.MovesRegistrySyncPacket
import com.cobblemon.mod.common.net.messages.client.data.PropertiesCompletionRegistrySyncPacket
import com.cobblemon.mod.common.net.messages.client.data.SpeciesRegistrySyncPacket
import com.cobblemon.mod.common.net.messages.client.pokemon.update.*
import com.cobblemon.mod.common.net.messages.client.pokemon.update.evolution.AddEvolutionPacket
import com.cobblemon.mod.common.net.messages.client.pokemon.update.evolution.ClearEvolutionsPacket
import com.cobblemon.mod.common.net.messages.client.pokemon.update.evolution.RemoveEvolutionPacket
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.pokemon.properties.PropertiesCompletionProvider

/**
 * Registers packet handlers that the client will need. This is separated from the server ones
 * not because they have to be, but because it helps us guarantee client access safety in a CI
 * job.
 *
 * @author Hiroku
 * @since November 27th, 2021
 */
object ClientPacketRegistrar : SidedPacketRegistrar() {
    override fun registerHandlers() {
        // Don't forget to register packets in CobblemonNetwork!

        registerHandler<ExperienceUpdatePacket>(SingleUpdatePacketHandler())
        registerHandler<SpeciesUpdatePacket>(SingleUpdatePacketHandler())
        registerHandler<FriendshipUpdatePacket>(SingleUpdatePacketHandler())
        registerHandler<PokemonStateUpdatePacket>(SingleUpdatePacketHandler())
        registerHandler<ShinyUpdatePacket>(SingleUpdatePacketHandler())
        registerHandler<NatureUpdatePacket>(SingleUpdatePacketHandler())
        registerHandler<MoveSetUpdatePacket>(SingleUpdatePacketHandler())
        registerHandler<HealthUpdatePacket>(SingleUpdatePacketHandler())
        registerHandler<StatusUpdatePacket>(SingleUpdatePacketHandler())
        registerHandler<CaughtBallUpdatePacket>(SingleUpdatePacketHandler())
        registerHandler<BenchedMovesUpdatePacket>(SingleUpdatePacketHandler())
        registerHandler<GenderUpdatePacket>(SingleUpdatePacketHandler())
        registerHandler<AspectsUpdatePacket>(SingleUpdatePacketHandler())
        registerHandler<AbilityUpdatePacket>(SingleUpdatePacketHandler())
        registerHandler<EVsUpdatePacket>(SingleUpdatePacketHandler())
        registerHandler<IVsUpdatePacket>(SingleUpdatePacketHandler())
        registerHandler<HeldItemUpdatePacket>(SingleUpdatePacketHandler())

        // Party storage
        registerHandler(InitializePartyHandler)
        registerHandler(SetPartyPokemonHandler)
        registerHandler(MoveClientPartyPokemonHandler)
        registerHandler(SetPartyReferenceHandler)

        // PC storage
        registerHandler(InitializePCHandler)
        registerHandler(MoveClientPCPokemonHandler)
        registerHandler(SetPCBoxPokemonHandler)
        registerHandler(SetPCPokemonHandler)
        registerHandler(OpenPCHandler)
        registerHandler(ClosePCHandler)

        // General storage
        registerHandler(RemoveClientPokemonHandler)
        registerHandler(SwapClientPokemonHandler)

        registerHandler(SetClientPlayerDataHandler)

        registerHandler(InteractPokemonUIPacketHandler)

        registerHandler(SummaryUIPacketHandler)
        registerHandler(StarterUIPacketHandler)
        registerHandler<AddEvolutionPacket>(EvolutionUpdatePacketHandler())
        registerHandler<RemoveEvolutionPacket>(EvolutionUpdatePacketHandler())
        registerHandler<ClearEvolutionsPacket>(EvolutionUpdatePacketHandler())

        // Battle handlers
        registerHandler(BattleEndHandler)
        registerHandler(BattleInitializeHandler)
        registerHandler(BattleFaintHandler)
        registerHandler(BattleQueueRequestHandler)
        registerHandler(BattleMakeChoiceHandler)
        registerHandler(BattleHealthChangeHandler)
        registerHandler(BattleSetTeamPokemonHandler)
        registerHandler(BattleSwitchPokemonHandler)
        registerHandler(BattleMessageHandler)
        registerHandler(BattleApplyCaptureResponseHandler)
        registerHandler(BattleCaptureStartHandler)
        registerHandler(BattleCaptureShakeHandler)
        registerHandler(BattleCaptureEndHandler)
        registerHandler(ChallengeNotificationHandler)
        registerHandler(BattleUpdateTeamPokemonHandler)
        registerHandler(BattlePersistentStatusHandler)

        // Settings
        registerHandler(ServerSettingsPacketHandler)


        // Data registries
        registerHandler(DataRegistrySyncPacketHandler<AbilityTemplate, AbilityRegistrySyncPacket>())
        registerHandler(DataRegistrySyncPacketHandler<MoveTemplate, MovesRegistrySyncPacket>())
        registerHandler(DataRegistrySyncPacketHandler<Species, SpeciesRegistrySyncPacket>())
        registerHandler(DataRegistrySyncPacketHandler<PropertiesCompletionProvider.SuggestionHolder, PropertiesCompletionRegistrySyncPacket>())

        // Hax
        registerHandler(UnvalidatedPlaySoundS2CPacketHandler)

        CobblemonNetwork.clientHandlersRegistered.complete(Unit)
    }
}

