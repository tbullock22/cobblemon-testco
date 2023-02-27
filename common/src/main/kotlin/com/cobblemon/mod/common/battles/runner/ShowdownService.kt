package com.cobblemon.mod.common.battles.runner

import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.battles.runner.graal.GraalShowdownService
import com.google.gson.JsonArray
import java.util.*

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