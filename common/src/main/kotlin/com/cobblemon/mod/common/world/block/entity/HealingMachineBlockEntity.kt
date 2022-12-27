/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.world.block.entity

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonBlockEntities
import com.cobblemon.mod.common.api.pokeball.PokeBalls
import com.cobblemon.mod.common.api.text.green
import com.cobblemon.mod.common.pokeball.PokeBall
import com.cobblemon.mod.common.util.DataKeys
import com.cobblemon.mod.common.util.getPlayer
import com.cobblemon.mod.common.util.lang
import com.cobblemon.mod.common.util.party
import java.util.UUID
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
class HealingMachineBlockEntity(
    blockPos: BlockPos,
    blockState: BlockState
) : BlockEntity(CobblemonBlockEntities.HEALING_MACHINE.get(), blockPos, blockState) {
    var currentUser: UUID? = null
        private set
    var pokeBalls: MutableList<PokeBall> = mutableListOf()
        private set
    private var healTimeLeft: Int = 0
    var healingCharge: Float = 0.0f
    val isInUse: Boolean
        get() = currentUser != null
    var infinite: Boolean = false

    fun setUser(user: UUID) {
        this.clearData()

        val player = user.getPlayer() ?: return
        val party = player.party()

        pokeBalls.clear()
        pokeBalls.addAll(party.map { it.caughtBall })
        this.currentUser = user
        this.healTimeLeft = 60

        markUpdated()
    }

    fun canHeal(player: ServerPlayerEntity): Boolean {
        if (Cobblemon.config.infiniteHealerCharge || this.infinite) {
            return true
        }
        val neededHealthPercent = player.party().getHealingRemainderPercent()
        return this.healingCharge >= neededHealthPercent
    }

    fun activate(player: ServerPlayerEntity) {
        if (!Cobblemon.config.infiniteHealerCharge) {
            val neededHealthPercent = player.party().getHealingRemainderPercent()
            this.healingCharge -= neededHealthPercent
        }
        this.setUser(player.uuid)
    }

    fun completeHealing() {
        val player = this.currentUser?.getPlayer() ?: return clearData()
        val party = player.party()

        party.heal()
        player.sendMessage(lang("healingmachine.healed").green())
        this.clearData()
    }

    private fun clearData() {
        this.currentUser = null
        this.pokeBalls.clear()
        this.healTimeLeft = 0
        markUpdated()
    }

    override fun readNbt(compoundTag: NbtCompound) {
        super.readNbt(compoundTag)

        this.pokeBalls.clear()

        if (compoundTag.containsUuid(DataKeys.HEALER_MACHINE_USER)) {
            this.currentUser = compoundTag.getUuid(DataKeys.HEALER_MACHINE_USER)
        }
        if (compoundTag.contains(DataKeys.HEALER_MACHINE_POKEBALLS)) {
            val pokeBallsTag = compoundTag.getCompound(DataKeys.HEALER_MACHINE_POKEBALLS)
            for (key in pokeBallsTag.keys) {
                val pokeBallId = pokeBallsTag.getString(key)
                if (pokeBallId.isEmpty()) {
                    continue
                }

                val pokeBall = PokeBalls.getPokeBall(Identifier(pokeBallId))
                if (pokeBall != null) {
                    this.pokeBalls.add(pokeBall)
                }
            }
        }
        if (compoundTag.contains(DataKeys.HEALER_MACHINE_TIME_LEFT)) {
            this.healTimeLeft = compoundTag.getInt(DataKeys.HEALER_MACHINE_TIME_LEFT)
        }
        if (compoundTag.contains(DataKeys.HEALER_MACHINE_CHARGE)) {
            this.healingCharge = compoundTag.getFloat(DataKeys.HEALER_MACHINE_CHARGE)
        }
        if (compoundTag.contains(DataKeys.HEALER_MACHINE_INFINITE)) {
            this.infinite = compoundTag.getBoolean(DataKeys.HEALER_MACHINE_INFINITE)
        }
    }

    override fun writeNbt(compoundTag: NbtCompound) {
        super.writeNbt(compoundTag)

        if (this.currentUser != null) {
            compoundTag.putUuid(DataKeys.HEALER_MACHINE_USER, this.currentUser!!)
        } else {
            compoundTag.remove(DataKeys.HEALER_MACHINE_USER)
        }

        if (pokeBalls.isNotEmpty()) {
            val pokeBallsTag = NbtCompound()
            var ballIndex = 1

            for (pokeBall in this.pokeBalls) {
                pokeBallsTag.putString("Pokeball$ballIndex", pokeBall.name.toString())
                ballIndex++
            }
            compoundTag.put(DataKeys.HEALER_MACHINE_POKEBALLS, pokeBallsTag)
        } else {
            compoundTag.remove(DataKeys.HEALER_MACHINE_POKEBALLS)
        }

        compoundTag.putInt(DataKeys.HEALER_MACHINE_TIME_LEFT, this.healTimeLeft)
        compoundTag.putFloat(DataKeys.HEALER_MACHINE_CHARGE, this.healingCharge)
        compoundTag.putBoolean(DataKeys.HEALER_MACHINE_INFINITE, this.infinite)
    }

    override fun toUpdatePacket() =  BlockEntityUpdateS2CPacket.create(this)
    override fun toInitialChunkDataNbt(): NbtCompound {
        return super.createNbtWithIdentifyingData()
    }

    private fun markUpdated() {
        this.markDirty()
        world!!.updateListeners(pos, this.cachedState, this.cachedState, 3)
    }

    companion object : BlockEntityTicker<HealingMachineBlockEntity> {
        override fun tick(world: World, blockPos: BlockPos, blockState: BlockState, tileEntity: HealingMachineBlockEntity) {
            if (world.isClient) {
                return
            }

            // Healing progression
            if (tileEntity.isInUse) {
                if (tileEntity.healTimeLeft > 0) {
                    tileEntity.healTimeLeft--
                } else {
                    tileEntity.completeHealing()
                }
            } else {
                // Recharging
                val maxCharge = Cobblemon.config.maxHealerCharge
                if (tileEntity.healingCharge < maxCharge) {
                    tileEntity.healingCharge = (tileEntity.healingCharge + Cobblemon.config.chargeGainedPerTick).coerceAtMost(maxCharge)
                    tileEntity.markUpdated()
                }
            }
        }
    }
}