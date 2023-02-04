/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block.entity

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonBlockEntities
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.pokeball.PokeBalls
import com.cobblemon.mod.common.api.text.green
import com.cobblemon.mod.common.pokeball.PokeBall
import com.cobblemon.mod.common.util.*
import com.cobblemon.mod.common.block.HealingMachineBlock
import java.util.UUID
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import kotlin.math.floor

class HealingMachineBlockEntity(
    val blockPos: BlockPos,
    val blockState: BlockState
) : BlockEntity(CobblemonBlockEntities.HEALING_MACHINE.get(), blockPos, blockState) {
    var currentUser: UUID? = null
        private set
    var pokeBalls: MutableList<PokeBall> = mutableListOf()
        private set
    var healTimeLeft: Int = 0
    var healingCharge: Float = 0.0F
    val isInUse: Boolean
        get() = currentUser != null
    var infinite: Boolean = false

    var currentSignal = 0
        private set

    init {
        this.updateRedstoneSignal()
        this.updateBlockChargeLevel()
    }

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
            this.updateRedstoneSignal()
        }
        this.setUser(player.uuid)
        updateBlockChargeLevel(HealingMachineBlock.MAX_CHARGE_LEVEL + 1)
        if (world != null && !world!!.isClient) world!!.playSoundServer(position = blockPos.toVec3d(), sound = CobblemonSounds.HEALING_MACHINE_ACTIVE.get(), volume = 1F, pitch = 1F)
    }

    fun completeHealing() {
        val player = this.currentUser?.getPlayer() ?: return clearData()
        val party = player.party()

        party.heal()
        player.sendMessage(lang("healingmachine.healed").green())
        updateBlockChargeLevel()
        clearData()
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

    private fun updateRedstoneSignal() {
        if (Cobblemon.config.infiniteHealerCharge || this.infinite) {
            this.currentSignal = MAX_REDSTONE_SIGNAL
        }
        val remainder = ((this.healingCharge / Cobblemon.config.maxHealerCharge) * 100).toInt() / 10
        this.currentSignal = remainder.coerceAtMost(MAX_REDSTONE_SIGNAL)
    }

    private fun updateBlockChargeLevel(level: Int? = null) {
        if (world != null && !world!!.isClient) {
            val chargeLevel = level ?: if (Cobblemon.config.infiniteHealerCharge || this.infinite) HealingMachineBlock.MAX_CHARGE_LEVEL
                else floor((healingCharge / Cobblemon.config.maxHealerCharge) * HealingMachineBlock.MAX_CHARGE_LEVEL).toInt()

            val state = world!!.getBlockState(blockPos)
            if (state != null && state.block is HealingMachineBlock) {
                val currentCharge = state.get(HealingMachineBlock.CHARGE_LEVEL).toInt()
                if (chargeLevel != currentCharge) world!!.setBlockState(blockPos, state.with(HealingMachineBlock.CHARGE_LEVEL, chargeLevel))
            }
        }
    }

    companion object {
        const val MAX_REDSTONE_SIGNAL = 10

        internal val TICKER = BlockEntityTicker<HealingMachineBlockEntity> { world, _, _, blockEntity ->
            if (world.isClient) return@BlockEntityTicker

            // Healing progression
            if (blockEntity.isInUse) {
                if (blockEntity.healTimeLeft > 0) {
                    blockEntity.healTimeLeft--
                } else {
                    blockEntity.completeHealing()
                }
            } else {
                // Recharging
                val maxCharge = Cobblemon.config.maxHealerCharge
                if (blockEntity.healingCharge < maxCharge) {
                    blockEntity.healingCharge = (blockEntity.healingCharge + Cobblemon.config.chargeGainedPerTick).coerceAtMost(maxCharge)
                    blockEntity.updateBlockChargeLevel()
                    blockEntity.updateRedstoneSignal()
                    blockEntity.markUpdated()
                }
            }
        }
    }
}