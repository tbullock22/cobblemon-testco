/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.drop

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.Cobblemon.LOGGER
import com.cobblemon.mod.common.api.text.green
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.util.lang
import com.cobblemon.mod.common.util.toBlockPos
import net.minecraft.block.Blocks
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d
import net.minecraft.util.registry.Registry

/**
 * A drop that is an actual item.
 *
 * @author Hiroku
 * @since July 24th, 2022
 */
class ItemDropEntry : DropEntry {
    override val percentage = 100F
    override val quantity = 1
    val quantityRange: IntRange? = null
    override val maxSelectableTimes = 1
    val dropMethod: ItemDropMethod? = null
    val item = Identifier("minecraft:fish")
    val nbt: NbtCompound? = null

    override fun drop(entity: LivingEntity?, world: ServerWorld, pos: Vec3d, player: ServerPlayerEntity?) {
        val item = world.registryManager.get(Registry.ITEM_KEY).get(item) ?: return LOGGER.error("Unable to load drop item: $item")
        val stack = ItemStack(item, quantityRange?.random() ?: quantity)
        val inLava = world.getBlockState(pos.toBlockPos()).block == Blocks.LAVA
        val dropMethod = (dropMethod ?: Cobblemon.config.defaultDropItemMethod).let {
            if (inLava) {
                ItemDropMethod.TO_INVENTORY
            } else {
                it
            }
        }
        nbt?.let { stack.nbt = it }

        if (dropMethod == ItemDropMethod.ON_PLAYER && player != null) {
            world.spawnEntity(ItemEntity(player.world, player.x, player.y, player.z, stack))
        } else if (dropMethod == ItemDropMethod.TO_INVENTORY && player != null) {
            val name = stack.name
            val count = stack.count
            val succeeded = player.giveItemStack(stack)
            if (Cobblemon.config.announceDropItems) {
                player.sendMessage(
                    if (succeeded) lang("drop.item.inventory", count, name.copy().green())
                    else lang("drop.item.full", name).red()
                )
            }
        } else if (dropMethod == ItemDropMethod.ON_ENTITY && entity != null) {
            world.spawnEntity(ItemEntity(entity.world, entity.x, entity.y, entity.z, stack))
        } else {
            world.spawnEntity(ItemEntity(world, pos.x, pos.y, pos.z, stack))
        }
    }
}