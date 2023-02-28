package com.cobblemon.mod.common.battles.runner

import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.battles.runner.graal.GraalShowdownService
import com.google.gson.JsonArray
import java.util.*

/**
 * Mediator service for communicating between the Cobblemon Minecraft mod and Cobblemon showdown service.
 *
 * All outgoing calls to showdown should be done through this layer to decouple the mod from a specific implementation.
 * To get an instance of the service, use the singleton provided via `ShowdownService.get()`
 *
 * @since February 27th, 2023
 * @author landonjw
 */
interface ShowdownService {
    fun openConnection()
    fun closeConnection()
    fun startBattle(battle: PokemonBattle, messages: Array<String>)
    fun send(battleId: UUID, messages: Array<String>)
    fun getAbilityIds(): JsonArray
    fun getMoves(): JsonArray
    fun getItemIds(): JsonArray
    fun indicateSpeciesInitialized()

    companion object {
        private var service: ShowdownService? = null

        fun get(): ShowdownService {
            if (service == null) service = GraalShowdownService()
            return service!!
        }
    }
}