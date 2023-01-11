/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokeball

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonEntities.EMPTY_POKEBALL
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent
import com.cobblemon.mod.common.api.net.serializers.Vec3DataSerializer
import com.cobblemon.mod.common.api.pokeball.PokeBalls
import com.cobblemon.mod.common.api.pokemon.status.Statuses
import com.cobblemon.mod.common.api.scheduling.afterOnMain
import com.cobblemon.mod.common.api.scheduling.taskBuilder
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.api.text.yellow
import com.cobblemon.mod.common.battles.BattleCaptureAction
import com.cobblemon.mod.common.battles.BattleRegistry
import com.cobblemon.mod.common.battles.BattleTypes
import com.cobblemon.mod.common.entity.EntityProperty
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.client.battle.BattleApplyCaptureResponsePacket
import com.cobblemon.mod.common.net.messages.client.battle.BattleCaptureStartPacket
import com.cobblemon.mod.common.pokeball.PokeBall
import com.cobblemon.mod.common.util.asResource
import com.cobblemon.mod.common.util.isServerSide
import com.cobblemon.mod.common.util.lang
import com.cobblemon.mod.common.util.playSoundServer
import com.cobblemon.mod.common.util.sendParticlesServer
import com.cobblemon.mod.common.util.setPositionSafely
import dev.architectury.extensions.network.EntitySpawnExtension
import dev.architectury.networking.NetworkManager
import java.util.concurrent.CompletableFuture
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityPose
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.entity.projectile.thrown.ThrownItemEntity
import net.minecraft.item.Item
import net.minecraft.network.Packet
import net.minecraft.network.PacketByteBuf
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.math.MathHelper.PI
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

class EmptyPokeBallEntity(
    var pokeBall: PokeBall,
    world: World,
    entityType: EntityType<out EmptyPokeBallEntity> = EMPTY_POKEBALL.get()
) : ThrownItemEntity(entityType, world), EntitySpawnExtension {
    enum class CaptureState {
        NOT,
        HIT,
        FALL,
        SHAKE
    }

    companion object {
        private val CAPTURE_STATE = DataTracker.registerData(EmptyPokeBallEntity::class.java, TrackedDataHandlerRegistry.BYTE)
        private val HIT_TARGET_POSITION = DataTracker.registerData(EmptyPokeBallEntity::class.java, Vec3DataSerializer)
        private val HIT_VELOCITY = DataTracker.registerData(EmptyPokeBallEntity::class.java, Vec3DataSerializer)
        private val SHAKE = DataTracker.registerData(EmptyPokeBallEntity::class.java, TrackedDataHandlerRegistry.BOOLEAN)

        const val SECONDS_BETWEEN_SHAKES = 1.25F
        const val SECONDS_BEFORE_SHAKE = 0.3F
    }

    val DIMENSIONS = EntityDimensions(0.4F, 0.4F, true)
    val entityProperties = mutableListOf<EntityProperty<*>>()

    var capturingPokemon: PokemonEntity? = null
    val captureState = addEntityProperty(CAPTURE_STATE, CaptureState.NOT.ordinal.toByte())
    val hitTargetPosition = addEntityProperty(HIT_TARGET_POSITION, Vec3d.ZERO)
    val hitVelocity = addEntityProperty(HIT_VELOCITY, Vec3d.ZERO)
    val shakeEmitter = addEntityProperty(SHAKE, false)
    val captureFuture = CompletableFuture<Boolean>()

    val delegate = if (world.isClient) {
        com.cobblemon.mod.common.client.entity.EmptyPokeBallClientDelegate()
    } else {
        EmptyPokeBallServerDelegate()
    }

    init {
        delegate.initialize(this)
        captureState.subscribe { state ->
            when (CaptureState.values()[state.toInt()]) {
                CaptureState.NOT -> setNoGravity(false)
                CaptureState.HIT -> {}
                CaptureState.FALL -> setNoGravity(false)
                CaptureState.SHAKE -> setNoGravity(false)
            }
        }
    }

    constructor(world: World) : this(pokeBall = PokeBalls.POKE_BALL, world = world)

    override fun onBlockHit(hitResult: BlockHitResult) {
        if (captureState.get() == CaptureState.NOT.ordinal.toByte()) {
            if (world.isServerSide()) {
                super.onBlockHit(hitResult)
                discard()
                val player = this.owner as? ServerPlayerEntity
                if (player?.isCreative == false) {
                    dropItem(defaultItem)
                }
            }
        } else {
            setNoGravity(false)
            velocity = Vec3d.ZERO
            isOnGround = true
        }
    }

    override fun onEntityHit(hitResult: EntityHitResult) {
        if (captureState.get() == CaptureState.NOT.ordinal.toByte()) {
            if (hitResult.entity is PokemonEntity && world.isServerSide()) {
                val pokemonEntity = hitResult.entity as PokemonEntity

                val battle = pokemonEntity.battleId.get().orElse(null)?.let { BattleRegistry.getBattle(it) }
                val owner = owner

                if (!pokemonEntity.pokemon.isWild()) {
                    owner?.sendMessage(lang("capture.not_wild", pokemonEntity.pokemon.species.translatedName).red())
                    return drop()
                }

                if (battle != null && owner != null && owner is LivingEntity) {
                    val throwerActor = battle.getActor(owner.uuid)
                    val hitActor = battle.actors.find { it.isForPokemon(pokemonEntity) }
                    val hitBattlePokemon = hitActor?.activePokemon?.find { it.battlePokemon?.effectedPokemon?.entity == pokemonEntity }

                    if (throwerActor == null) {
                        owner.sendMessage(lang("capture.in_battle", pokemonEntity.pokemon.species.translatedName).red())
                        return drop()
                    }

                    if (hitActor == null || hitBattlePokemon == null) {
                        return drop() // Weird, shouldn't be possible
                    }

                    if (battle.format.battleType != BattleTypes.SINGLES || hitActor.pokemonList.count { it.health > 0 } > 1) {
                        owner.sendMessage(lang("capture.not_single").red())
                        return drop()
                    }

                    val request = throwerActor.request?.takeIf { throwerActor.mustChoose } ?: run {
                        owner.sendMessage(lang("capture.not_your_turn").red())
                        return drop()
                    }

                    val countMovable = (request.active?.count() ?: 0) - request.forceSwitch.count { it }
                    if (countMovable > throwerActor.expectingCaptureActions) {
                        throwerActor.expectingCaptureActions++
                        battle.captureActions.add(BattleCaptureAction(battle, hitBattlePokemon, this).also { it.attach() })
                        battle.broadcastChatMessage(
                            lang(
                                "capture.attempted_capture",
                                throwerActor.getName(),
                                pokeBall.item().name,
                                pokemonEntity.pokemon.species.translatedName
                            ).yellow()
                        )
                        battle.sendUpdate(BattleCaptureStartPacket(pokeBall.name, hitBattlePokemon.getPNX()))
                        throwerActor.sendUpdate(BattleApplyCaptureResponsePacket())
                    } else {
                        owner.sendMessage(lang("capture.not_your_turn").red())
                        return drop()
                    }
                } else if (pokemonEntity.isBusy) {
                    owner?.sendMessage(lang("capture.busy", pokemonEntity.pokemon.species.translatedName).red())
                    return drop()
                } else if (owner is ServerPlayerEntity && BattleRegistry.getBattleByParticipatingPlayer(owner) != null) {
                    owner.sendMessage(lang("you_in_battle").red())
                    return drop()
                }
                capturingPokemon = pokemonEntity
                hitVelocity.set(velocity.normalize())
                hitTargetPosition.set(hitResult.pos)
                attemptCatch(pokemonEntity)
                return
            }
        }
        super.onEntityHit(hitResult)
    }

    private fun drop() {
        val owner = owner
        discard()
        val player = owner?.takeIf { it is ServerPlayerEntity } as? ServerPlayerEntity
        if (player?.isCreative != true) dropItem(defaultItem)
        return
    }

    override fun shouldSave(): Boolean {
        return false
    }
    override fun tick() {
        delegate.tick(this)
        entityProperties.forEach { it.checkForUpdate() }
        super.tick()
        if (world.isServerSide()) {
            if (owner == null || !owner!!.isAlive || (captureState.get() != CaptureState.NOT.ordinal.toByte() && capturingPokemon?.isAlive != true)) {
                breakFree()
                discard()
                return
            }

            if (isOnGround && captureState.get() == CaptureState.FALL.ordinal.toByte()) {
                capturingPokemon?.setPositionSafely(pos)
                captureState.set(CaptureState.SHAKE.ordinal.toByte())

                val captureResult = Cobblemon.captureCalculator.processCapture(owner as ServerPlayerEntity, pokeBall, capturingPokemon!!.pokemon, null)

                var rollsRemaining = captureResult.numberOfShakes
                if (rollsRemaining == 4) {
                    rollsRemaining--
                }

                taskBuilder()
                    .iterations(captureResult.numberOfShakes + 1)
                    .delay(SECONDS_BEFORE_SHAKE)
                    .interval(SECONDS_BETWEEN_SHAKES)
                    .execute {
                        if (capturingPokemon?.isAlive != true) {
                            discard()
                        }

                        if (!isAlive) {
                            it.expire()
                            return@execute
                        }

                        if (rollsRemaining <= 0) {
                            if (captureResult.isSuccessfulCapture) {
                                // Do a capture
                                world.sendParticlesServer(ParticleTypes.CRIT, pos, 10, Vec3d(0.1, -0.5, 0.1), 0.2)
                                world.playSoundServer(pos, CobblemonSounds.POKE_BALL_CAPTURE_SUCCEEDED.get(), volume = 0.3F, pitch = 1F)
                                val pokemon = capturingPokemon ?: return@execute
                                val player = this.owner as? ServerPlayerEntity ?: return@execute

                                afterOnMain(seconds = 1F) {
                                    pokemon.discard()
                                    discard()
                                    captureFuture.complete(true)
                                    val party = Cobblemon.storage.getParty(player.uuid)
                                    pokemon.pokemon.caughtBall = pokeBall
                                    pokeBall.effects.forEach { effect -> effect.apply(player, pokemon.pokemon) }
                                    party.add(pokemon.pokemon)
                                    CobblemonEvents.POKEMON_CAPTURED.post(PokemonCapturedEvent(pokemon.pokemon, player))
                                }

                                return@execute
                            } else {
                                breakFree()
                            }
                            return@execute
                        }

                        rollsRemaining--
                        world.playSoundServer(pos, CobblemonSounds.POKE_BALL_SHAKE.get())
                        shakeEmitter.set(!shakeEmitter.get())
                    }
                    .build()
            }
        }
    }

    private fun breakFree() {
        val pokemon = capturingPokemon ?: return
        pokemon.setPosition(pos)
        pokemon.beamModeEmitter.set(1)
        pokemon.isInvisible = false

        if (pokemon.battleId.get().isEmpty) {
            pokemon.pokemon.status?.takeIf { it.status == Statuses.SLEEP }?.let { pokemon.pokemon.status = null }
        }

        afterOnMain(seconds = 0.25F) {
            pokemon.busyLocks.remove(this)
            captureFuture.complete(false)
            world.sendParticlesServer(ParticleTypes.CLOUD, pos, 20, Vec3d(0.0, 0.2, 0.0), 0.05)
            world.playSoundServer(pos, CobblemonSounds.POKE_BALL_OPEN.get())
            discard()
        }
    }

    override fun getDefaultItem(): Item = pokeBall.item()

    override fun createSpawnPacket(): Packet<*> {
        return NetworkManager.createAddEntityPacket(this)
    }

    override fun getDimensions(pPose: EntityPose) = DIMENSIONS
    fun <T> addEntityProperty(accessor: TrackedData<T>, initialValue: T): EntityProperty<T> {
        val property = EntityProperty(
            dataTracker = dataTracker,
            accessor = accessor,
            initialValue = initialValue
        )
        entityProperties.add(property)
        return property
    }

    private fun attemptCatch(pokemonEntity: PokemonEntity) {
        pokemonEntity.busyLocks.add(this)
        val displace = velocity
        captureState.set(CaptureState.HIT.ordinal.toByte())
        val mul = if (random.nextBoolean()) 1 else -1
        world.playSoundServer(pos, CobblemonSounds.POKE_BALL_HIT.get())
        velocity = displace.multiply(-1.0, 0.0, -1.0).normalize().rotateY(mul * PI/3).multiply(0.1, 0.0, 0.1).add(0.0, 1.0 / 3, 0.0)
        pokemonEntity.phasingTargetId.set(this.id)
        afterOnMain(seconds = 0.7F) {
            velocity = Vec3d.ZERO
            setNoGravity(true)
            world.playSoundServer(pos, CobblemonSounds.POKE_BALL_CAPTURE_STARTED.get(), volume = 0.2F)
            pokemonEntity.beamModeEmitter.set(2.toByte())
        }

        afterOnMain(seconds = 2.2F) {
            pokemonEntity.phasingTargetId.set(-1)
            pokemonEntity.beamModeEmitter.set(0.toByte())
            pokemonEntity.isInvisible = true
            captureState.set(CaptureState.FALL.ordinal.toByte())
        }
    }

    override fun saveAdditionalSpawnData(buf: PacketByteBuf) {
        buf.writeString(pokeBall.name.toString())
    }

    override fun loadAdditionalSpawnData(buf: PacketByteBuf) {
        pokeBall = PokeBalls.getPokeBall(buf.readString().asResource()) ?: PokeBalls.POKE_BALL
    }
}