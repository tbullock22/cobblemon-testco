/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item

import com.cobblemon.mod.common.api.text.gray
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.entity.pokeball.EmptyPokeBallEntity
import com.cobblemon.mod.common.item.CobblemonItemGroups.POKE_BALL_GROUP
import com.cobblemon.mod.common.pokeball.PokeBall
import com.cobblemon.mod.common.util.asTranslated
import com.cobblemon.mod.common.util.isServerSide
import com.cobblemon.mod.common.util.math.geometry.toRadians
import net.minecraft.client.item.TooltipContext
import kotlin.math.cos
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World
class PokeBallItem(
    val pokeBall : PokeBall
) : CobblemonItem(Settings().group(POKE_BALL_GROUP)) {

    override fun use(world: World, player: PlayerEntity, usedHand: Hand): TypedActionResult<ItemStack> {
        val itemStack = player.getStackInHand(usedHand)
        if (world.isServerSide()) {
            throwPokeBall(world, player as ServerPlayerEntity)
        }
        if (!player.abilities.creativeMode) {
            itemStack.decrement(1)
        }
        return TypedActionResult.success(itemStack, world.isClient)
    }

    private fun throwPokeBall(world: World, player: ServerPlayerEntity) {
        val pokeBallEntity = EmptyPokeBallEntity(pokeBall, player.world).apply {
            setPos(player.x, player.y + player.standingEyeHeight - 0.2, player.z)
            setVelocity(player, player.pitch - 10 * cos(player.pitch.toRadians()), player.yaw, 0.0f, 1.25f, 1.0f)
            owner = player
        }
        world.spawnEntity(pokeBallEntity)
    }

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        tooltip.add("item.${this.pokeBall.name.namespace}.${this.pokeBall.name.path}.tooltip".asTranslated().gray())
    }

}