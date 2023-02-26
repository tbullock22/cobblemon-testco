package com.cobblemon.mod.common.item.interactive

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.item.CobblemonItemGroups
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import java.lang.Integer.min

class PotionItem(val type: PotionType) : PokemonInteractiveItem(Settings().group(CobblemonItemGroups.MEDICINE_ITEM_GROUP), Ownership.OWNER) {

    override fun processInteraction(player: ServerPlayerEntity, entity: PokemonEntity, stack: ItemStack): Boolean {
        val pokemon = entity.pokemon
        if (pokemon.isFullHealth()) return false

        val healthToRestore = getHealthToRestore(pokemon)
        pokemon.currentHealth = min(pokemon.currentHealth + healthToRestore, pokemon.hp)
        if (type == PotionType.FULL_RESTORE) {
            pokemon.status = null
        }
        return true
    }

    private fun getHealthToRestore(pokemon: Pokemon): Int {
        return when (type) {
            PotionType.POTION -> 20
            PotionType.SUPER_POTION -> 60
            PotionType.HYPER_POTION -> 120
            PotionType.MAX_POTION -> pokemon.hp
            PotionType.FULL_RESTORE -> pokemon.hp
        }
    }

}

enum class PotionType {
    POTION, SUPER_POTION, HYPER_POTION, MAX_POTION, FULL_RESTORE
}