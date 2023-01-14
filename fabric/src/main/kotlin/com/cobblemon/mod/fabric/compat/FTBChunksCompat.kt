package com.cobblemon.mod.fabric.compat

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.gui.drawPortraitPokemon
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.text.gold
import com.cobblemon.mod.common.api.text.white
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import dev.ftb.mods.ftbchunks.client.EntityMapIcon
import dev.ftb.mods.ftbchunks.client.FTBChunksClientConfig
import dev.ftb.mods.ftbchunks.client.MapType
import dev.ftb.mods.ftbchunks.integration.MapIconEvent
import dev.ftb.mods.ftblibrary.icon.Icon
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
            // ToDo we need to somehow disable the default entity icon from showing up, see EntityIcons#get, this isn't a graphical only issue as sometimes it will overlap ours and render the incorrect Icon#addTooltip
            val pokemonEntity = entity as? PokemonEntity ?: continue
            event.add(PokemonIcon(pokemonEntity))
        }
    }

    class PokemonIcon(private val pokemonEntity: PokemonEntity) : EntityMapIcon(pokemonEntity, Icon.EMPTY) {

        override fun draw(
            mapType: MapType,
            matrixStack: MatrixStack,
            x: Int,
            y: Int,
            width: Int,
            height: Int,
            outsideVisibleArea: Boolean
        ) {
            val species = PokemonSpecies.getByIdentifier(this.pokemonEntity.species.get().asIdentifierDefaultingNamespace()) ?: return
            matrixStack.push()
            matrixStack.translate(x + width / 2.0, y + height / 2.0, 0.0)
            val min = width.coerceAtMost(height)
            matrixStack.scale(min / 16F, min / 16F, min / 16F)
            drawPortraitPokemon(
                species,
                this.pokemonEntity.aspects.get(),
                matrixStack
            )
            matrixStack.pop()
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

        override fun isVisible(mapType: MapType, distanceToPlayer: Double, outsideVisibleArea: Boolean): Boolean = !mapType.isWorldIcon && !outsideVisibleArea

    }

}