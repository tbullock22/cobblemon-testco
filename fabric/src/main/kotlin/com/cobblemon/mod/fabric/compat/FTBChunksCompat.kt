package com.cobblemon.mod.fabric.compat

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.text.gold
import com.cobblemon.mod.common.api.text.white
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import dev.ftb.mods.ftbchunks.client.EntityIcons
import dev.ftb.mods.ftbchunks.client.EntityMapIcon
import dev.ftb.mods.ftbchunks.client.FTBChunksClientConfig
import dev.ftb.mods.ftbchunks.client.MapType
import dev.ftb.mods.ftbchunks.integration.MapIconEvent
import dev.ftb.mods.ftblibrary.util.TooltipList
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text

object FTBChunksCompat {

    fun hook() {
        MapIconEvent.MINIMAP.register(this::mapPokemon)
        MapIconEvent.LARGE_MAP.register(this::mapPokemon)
        Cobblemon.LOGGER.info("Pokémon entity compatibility for FTB Chunks enabled for more information see https://www.feed-the-beast.com/mods")
    }

    private fun mapPokemon(event: MapIconEvent) {
        val minecraft = event.mc
        if (!FTBChunksClientConfig.MINIMAP_ENTITIES.get()) {
            return
        }
        for (entity in minecraft.world?.entities ?: return) {
            // ToDo respect config settings for surface only entities see default implementation of entity icons for the way to go about it
            val pokemonEntity = entity as? PokemonEntity ?: continue
            event.add(PokemonIcon(pokemonEntity))
        }
    }

    // ToDo when implemented replace EntityIcons.NORMAL with Icon.EMPTY this is only here so we can draw a reference point to make sure we're positioned correctly
    class PokemonIcon(private val pokemonEntity: PokemonEntity) : EntityMapIcon(pokemonEntity, EntityIcons.NORMAL) {

        override fun draw(
            mapType: MapType,
            matrixStack: MatrixStack,
            x: Int,
            y: Int,
            width: Int,
            height: Int,
            outsideVisibleArea: Boolean
        ) {
            // ToDo when implemented remove this as it is unnecessary
            this.icon.draw(matrixStack, x, y, width, height)
            val species = PokemonSpecies.getByIdentifier(this.pokemonEntity.species.get().asIdentifierDefaultingNamespace()) ?: return
            val aspects = this.pokemonEntity.aspects.get()
        }

        override fun addTooltip(list: TooltipList) {
            val species = PokemonSpecies.getByIdentifier(this.pokemonEntity.species.get().asIdentifierDefaultingNamespace()) ?: return
            if (this.pokemonEntity.aspects.get().contains("shiny")) {
                list.add(species.translatedName.append(Text.literal(" ✫")).gold())
            }
            else {
                list.add(species.translatedName.white())
            }
        }

    }

}