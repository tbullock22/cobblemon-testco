/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonEntities
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.drop.DropTable
import com.cobblemon.mod.common.api.entity.Despawner
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.pokemon.ShoulderMountEvent
import com.cobblemon.mod.common.api.net.serializers.PoseTypeDataSerializer
import com.cobblemon.mod.common.api.net.serializers.StringSetDataSerializer
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.pokemon.status.Statuses
import com.cobblemon.mod.common.api.reactive.ObservableSubscription
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.api.scheduling.afterOnMain
import com.cobblemon.mod.common.api.types.ElementalTypes
import com.cobblemon.mod.common.api.types.ElementalTypes.FIRE
import com.cobblemon.mod.common.entity.EntityProperty
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.Poseable
import com.cobblemon.mod.common.entity.pokemon.ai.PokemonMoveControl
import com.cobblemon.mod.common.entity.pokemon.ai.PokemonNavigation
import com.cobblemon.mod.common.entity.pokemon.ai.goals.*
import com.cobblemon.mod.common.item.interactive.PokemonInteractiveItem
import com.cobblemon.mod.common.net.serverhandling.storage.SEND_OUT_DURATION
import com.cobblemon.mod.common.pokemon.FormData
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.activestate.ActivePokemonState
import com.cobblemon.mod.common.pokemon.activestate.InactivePokemonState
import com.cobblemon.mod.common.pokemon.activestate.ShoulderedState
import com.cobblemon.mod.common.pokemon.ai.FormPokemonBehaviour
import com.cobblemon.mod.common.pokemon.evolution.variants.ItemInteractionEvolution
import com.cobblemon.mod.common.util.*
import dev.architectury.extensions.network.EntitySpawnExtension
import dev.architectury.networking.NetworkManager
import java.util.*
import java.util.concurrent.CompletableFuture
import net.minecraft.block.BlockState
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityPose
import net.minecraft.entity.EntityType
import net.minecraft.entity.ai.control.MoveControl
import net.minecraft.entity.ai.goal.BreatheAirGoal
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.entity.ai.pathing.PathNodeType
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.entity.passive.PassiveEntity
import net.minecraft.entity.passive.TameableShoulderEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.FluidState
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.tag.FluidTags
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class PokemonEntity(
    world: World,
    pokemon: Pokemon = Pokemon(),
    type: EntityType<out PokemonEntity> = CobblemonEntities.POKEMON.get(),
) : TameableShoulderEntity(type, world), EntitySpawnExtension, Poseable {
    val removalObservable = SimpleObservable<RemovalReason?>()
    /** A list of observable subscriptions related to this entity that need to be cleaned up when the entity is removed. */
    val subscriptions = mutableListOf<ObservableSubscription<*>>()

    val form: FormData
        get() = pokemon.form
    val behaviour: FormPokemonBehaviour
        get() = form.behaviour

    var pokemon: Pokemon = pokemon
        set(value) {
            field = value
            delegate.changePokemon(value)
            // We need to update this value every time the Pokémon changes, other eye height related things will be dynamic.
            this.updateEyeHeight()
        }

    var despawner: Despawner<PokemonEntity> = Cobblemon.bestSpawner.defaultPokemonDespawner

    /** The player that caused this Pokémon to faint. */
    var killer: ServerPlayerEntity? = null

    var ticksLived = 0
    val busyLocks = mutableListOf<Any>()
    val isBusy: Boolean
        get() = busyLocks.isNotEmpty()
    val isBattling: Boolean
        get() = this.battleId.get().isPresent

    var drops: DropTable? = null
    val entityProperties = mutableListOf<EntityProperty<*>>()

    val species = addEntityProperty(SPECIES, pokemon.species.resourceIdentifier.toString())
    val isMoving = addEntityProperty(MOVING, false)
    val behaviourFlags = addEntityProperty(BEHAVIOUR_FLAGS, 0)
    val phasingTargetId = addEntityProperty(PHASING_TARGET_ID, -1)
    val battleId = addEntityProperty(BATTLE_ID, Optional.empty())
    val aspects = addEntityProperty(ASPECTS, pokemon.aspects)
    val deathEffectsStarted = addEntityProperty(DYING_EFFECTS_STARTED, false)
    val poseType = addEntityProperty(POSE_TYPE, PoseType.NONE)
    internal val labelLevel = addEntityProperty(LABEL_LEVEL, pokemon.level)

    /**
     * 0 is do nothing,
     * 1 is appearing from a pokeball so needs to be small then grows,
     * 2 is being captured/recalling so starts large and shrinks.
     */
    val beamModeEmitter = addEntityProperty(BEAM_MODE, 0.toByte())
    // properties like the above are synced and can be subscribed to for changes on either side

    val delegate = if (world.isClient) {
        // Don't import because scanning for imports is a CI job we'll do later to detect errant access to client from server
        com.cobblemon.mod.common.client.entity.PokemonClientDelegate()
    } else {
        PokemonServerDelegate()
    }

    init {
        delegate.initialize(this)
        delegate.changePokemon(pokemon)
        calculateDimensions()

        subscriptions.add(
            battleId
                .subscribeIncludingCurrent {
                    if (it.isPresent) {
                        busyLocks.add(BATTLE_LOCK)
                    } else {
                        busyLocks.remove(BATTLE_LOCK)
                    }
                }
        )

        subscriptions.add(
            poseType.subscribe {
                if (it == PoseType.FLY || it == PoseType.HOVER) {
                    setNoGravity(true)
                } else {
                    setNoGravity(false)
                }
            }
        )
    }

    companion object {
        val SPECIES = DataTracker.registerData(PokemonEntity::class.java, TrackedDataHandlerRegistry.STRING)
        val MOVING = DataTracker.registerData(PokemonEntity::class.java, TrackedDataHandlerRegistry.BOOLEAN)
        val BEHAVIOUR_FLAGS = DataTracker.registerData(PokemonEntity::class.java, TrackedDataHandlerRegistry.BYTE)
        val PHASING_TARGET_ID = DataTracker.registerData(PokemonEntity::class.java, TrackedDataHandlerRegistry.INTEGER)
        val BEAM_MODE = DataTracker.registerData(PokemonEntity::class.java, TrackedDataHandlerRegistry.BYTE)
        val BATTLE_ID = DataTracker.registerData(PokemonEntity::class.java, TrackedDataHandlerRegistry.OPTIONAL_UUID)
        val ASPECTS = DataTracker.registerData(PokemonEntity::class.java, StringSetDataSerializer)
        val DYING_EFFECTS_STARTED = DataTracker.registerData(PokemonEntity::class.java, TrackedDataHandlerRegistry.BOOLEAN)
        val POSE_TYPE = DataTracker.registerData(PokemonEntity::class.java, PoseTypeDataSerializer)
        val LABEL_LEVEL = DataTracker.registerData(PokemonEntity::class.java, TrackedDataHandlerRegistry.INTEGER)

        const val BATTLE_LOCK = "battle"
    }

    override fun canWalkOnFluid(state: FluidState): Boolean {
//        val node = navigation.currentPath?.currentNode
//        val targetPos = node?.blockPos
//        if (targetPos == null || world.getBlockState(targetPos.up()).isAir) {
        return if (state.isIn(FluidTags.WATER)) {
            behaviour.moving.swim.canWalkOnWater
        } else if (state.isIn(FluidTags.LAVA)) {
            behaviour.moving.swim.canWalkOnLava
        } else {
            super.canWalkOnFluid(state)
        }
//        }
//
//        return super.canWalkOnFluid(state)
    }

    override fun tick() {
        super.tick()
        // We will be handling idle logic ourselves thank you
        this.setDespawnCounter(0)
        entityProperties.forEach { it.checkForUpdate() }
        delegate.tick(this)
        ticksLived++
        if (this.ticksLived % 20 == 0) {
            this.updateEyeHeight()
        }
    }

    fun setMoveControl(moveControl: MoveControl) {
        this.moveControl = moveControl
    }

    /**
     * Prevents water type Pokémon from taking drowning damage.
     */
    override fun canBreatheInWater(): Boolean {
        return behaviour.moving.swim.canBreatheUnderwater
    }

    /**
     * Prevents fire type Pokémon from taking fire damage.
     */
    override fun isFireImmune(): Boolean {
        return FIRE in pokemon.types || behaviour.moving.swim.canSwimInLava
    }

    /**
     * Prevents flying type Pokémon from taking fall damage.
     */
    override fun fall(pY: Double, pOnGround: Boolean, pState: BlockState, pPos: BlockPos) {
        if (ElementalTypes.FLYING in pokemon.types) {
            fallDistance = 0F
        } else {
            super.fall(pY, pOnGround, pState, pPos)
        }
    }

    override fun saveNbt(nbt: NbtCompound): Boolean {
        nbt.put(DataKeys.POKEMON, pokemon.saveToNBT(NbtCompound()))
        return super.saveNbt(nbt)
    }

    override fun writeNbt(nbt: NbtCompound): NbtCompound {
        nbt.put(DataKeys.POKEMON, pokemon.saveToNBT(NbtCompound()))
        return super.writeNbt(nbt)
    }

    override fun isInvulnerableTo(damageSource: DamageSource): Boolean {
        // If the entity is busy, it cannot be hurt.
        if (busyLocks.isNotEmpty()) {
            return true
        }

        return super.isInvulnerableTo(damageSource)
    }

    fun recallWithAnimation(): CompletableFuture<Pokemon> {
        val owner = owner
        val future = CompletableFuture<Pokemon>()
        if (phasingTargetId.get() == -1 && owner != null) {
            owner.getWorld().playSoundServer(pos, CobblemonSounds.POKE_BALL_RECALL.get(), volume = 0.2F)
            phasingTargetId.set(owner.id)
            beamModeEmitter.set(2)
            afterOnMain(seconds = SEND_OUT_DURATION) {
                pokemon.recall()
                future.complete(pokemon)
            }
        } else {
            pokemon.recall()
            future.complete(pokemon)
        }

        return future
    }

    override fun readNbt(nbt: NbtCompound) {
        super.readNbt(nbt)
        pokemon = Pokemon().loadFromNBT(nbt.getCompound(DataKeys.POKEMON))
        species.set(pokemon.species.resourceIdentifier.toString())
        labelLevel.set(pokemon.level)
    }

    override fun createSpawnPacket() = NetworkManager.createAddEntityPacket(this)

    override fun getPathfindingPenalty(nodeType: PathNodeType): Float {
        return if (nodeType == PathNodeType.OPEN) 2F else super.getPathfindingPenalty(nodeType)
    }

    public override fun initGoals() {
        // It is capable of being null in specific cases, dw about it
        if (pokemon != null) {
            moveControl = PokemonMoveControl(this)
            navigation = PokemonNavigation(world, this)
            goalSelector.clear()
            goalSelector.add(0, object : Goal() {
                override fun canStart() = this@PokemonEntity.phasingTargetId.get() != -1 || pokemon.status?.status == Statuses.SLEEP || deathEffectsStarted.get()
                override fun getControls() = EnumSet.allOf(Control::class.java)
            })

            goalSelector.add(1, PokemonBreatheAirGoal(this))
            goalSelector.add(2, PokemonFloatToSurfaceGoal(this))
            goalSelector.add(3, PokemonFollowOwnerGoal(this, 1.0, 8F, 2F, false))
            goalSelector.add(4, PokemonMoveIntoFluidGoal(this))
            goalSelector.add(5, SleepOnTrainerGoal(this))
            goalSelector.add(5, WildRestGoal(this))
            goalSelector.add(6, PokemonWanderAroundGoal(this))
            goalSelector.add(7, PokemonLookAtEntityGoal(this, ServerPlayerEntity::class.java, 5F))
        }
    }

    fun <T> addEntityProperty(accessor: TrackedData<T>, initialValue: T): EntityProperty<T> {
        val property = EntityProperty(
            dataTracker = dataTracker,
            accessor = accessor,
            initialValue = initialValue
        )
        entityProperties.add(property)
        return property
    }

    override fun createChild(level: ServerWorld, partner: PassiveEntity) = null

    override fun isReadyToSitOnPlayer(): Boolean {
        return pokemon.form.shoulderMountable
    }

    override fun interactMob(player: PlayerEntity, hand: Hand) : ActionResult {
        // TODO: Move to proper pokemon interaction menu
        if (this.attemptItemInteraction(player, player.getStackInHand(hand))) {
            // TODO #105
            return ActionResult.SUCCESS
        }
        if (player.isSneaking && hand == Hand.MAIN_HAND) {
            if (isReadyToSitOnPlayer && player is ServerPlayerEntity && !isBusy) {
                this.tryMountingShoulder(player)
            }
        }
        return super.interactMob(player, hand)
    }

    override fun getDimensions(pose: EntityPose): EntityDimensions {
        val scale = pokemon.form.baseScale * pokemon.scaleModifier
        return pokemon.form.hitbox.scaled(scale)
    }

    override fun canTakeDamage() = super.canTakeDamage() && !isBusy
    override fun damage(source: DamageSource?, amount: Float): Boolean {
        return if (super.damage(source, amount)) {
            if (this.health == 0F) {
                pokemon.currentHealth = 0
            } else {
//                pokemon.currentHealth = this.health.toInt()
            }
            true
        } else false
    }

    override fun saveAdditionalSpawnData(buffer: PacketByteBuf) {
        buffer.writeFloat(pokemon.scaleModifier)
        buffer.writeIdentifier(pokemon.species.resourceIdentifier)
        buffer.writeString(pokemon.form.name)
        buffer.writeInt(phasingTargetId.get())
        buffer.writeByte(beamModeEmitter.get().toInt())
        buffer.writeCollection(pokemon.aspects, PacketByteBuf::writeString)
        buffer.writeInt(if (Cobblemon.config.displayEntityLevelLabel) this.labelLevel.get() else -1)
    }

    override fun loadAdditionalSpawnData(buffer: PacketByteBuf) {
        if (this.world.isClient) {
            pokemon.scaleModifier = buffer.readFloat()
            // TODO exception handling
            pokemon.species = PokemonSpecies.getByIdentifier(buffer.readIdentifier())!!
            // TODO exception handling
            val formName = buffer.readString()
            pokemon.form = pokemon.species.forms.find { form -> form.name == formName } ?: pokemon.species.standardForm
            phasingTargetId.set(buffer.readInt())
            beamModeEmitter.set(buffer.readByte())
            this.aspects.set(buffer.readList(PacketByteBuf::readString).toSet())
            labelLevel.set(buffer.readInt())
        }
    }

    override fun shouldSave(): Boolean {
        return false
    }

    override fun checkDespawn() {
        if (pokemon.getOwnerUUID() == null && despawner.shouldDespawn(this)) {
            discard()
        }
    }

    override fun getEyeHeight(pose: EntityPose): Float = this.pokemon.form.eyeHeight(this)

    override fun getActiveEyeHeight(pose: EntityPose?, dimensions: EntityDimensions?): Float {
        if (this.pokemon == null) {
            return super.getActiveEyeHeight(pose, dimensions)
        }
        return this.pokemon.form.eyeHeight(this)
    }

    fun setBehaviourFlag(flag: PokemonBehaviourFlag, on: Boolean) {
        behaviourFlags.set(setBitForByte(behaviourFlags.get(), flag.bit, on))
    }

    fun getBehaviourFlag(flag: PokemonBehaviourFlag): Boolean = getBitForByte(behaviourFlags.get(), flag.bit)

    fun canBattle(player: PlayerEntity): Boolean {
        if (isBusy) {
            return false
        } else if (battleId.get().isPresent) {
            return false
        } else if (ownerUuid != null) {
            return false
        } else if (health <= 0F || isDead) {
            return false
        }

        return true
    }

    /**
     * The level this entity should display.
     *
     * @return The level that should be displayed, if equal or lesser than 0 the level is not intended to be displayed.
     */
    fun labelLevel(): Int {
        return this.labelLevel.get()
    }

    private fun attemptItemInteraction(player: PlayerEntity, stack: ItemStack): Boolean {
        if (player !is ServerPlayerEntity || stack.isEmpty || this.isBusy) {
            return false
        }
        if (pokemon.getOwnerPlayer() == player) {
            val context = ItemInteractionEvolution.ItemInteractionContext(stack.item, player.world)
            pokemon.evolutions
                .filterIsInstance<ItemInteractionEvolution>()
                .forEach { evolution ->
                    if (evolution.attemptEvolution(pokemon, context)) {
                        if (!player.isCreative) {
                            stack.decrement(1)
                        }
                        this.world.playSoundServer(position = this.pos, sound = CobblemonSounds.ITEM_USE.get(), volume = 1F, pitch = 1F)
                        return true
                    }
                }
        }

        (stack.item as? PokemonInteractiveItem)?.let {
            if (it.onInteraction(player, this, stack)) {
                this.world.playSoundServer(position = this.pos, sound = CobblemonSounds.ITEM_USE.get(), volume = 1F, pitch = 1F)
                return true
            }
        }

        return false
    }

    private fun tryMountingShoulder(player: ServerPlayerEntity): Boolean {
        if (this.pokemon.belongsTo(player) && this.hasRoomToMount(player)) {
            CobblemonEvents.SHOULDER_MOUNT.postThen(ShoulderMountEvent(player, pokemon, isLeft = player.shoulderEntityLeft.isEmpty)) {
                val dirToPlayer = player.eyePos.subtract(pos).multiply(1.0, 0.0, 1.0).normalize()
                velocity = dirToPlayer.multiply(0.8).add(0.0, 0.5, 0.0)
                val lock = Any()
                busyLocks.add(lock)
                afterOnMain(seconds = 0.5F) {
                    busyLocks.remove(lock)
                    if (!isBusy && isAlive) {
                        val isLeft = player.shoulderEntityLeft.isEmpty
                        if (isLeft || player.shoulderEntityRight.isEmpty) {
                            pokemon.state = ShoulderedState(player.uuid, isLeft, pokemon.uuid)
                            this.mountOnto(player)
                            this.pokemon.form.shoulderEffects.forEach { it.applyEffect(this.pokemon, player, isLeft) }
                        }
                    }
                }
                return true
            }
        }
        return false
    }

    override fun remove(reason: RemovalReason?) {
        val stateEntity = (pokemon.state as? ActivePokemonState)?.entity
        super.remove(reason)
        if (stateEntity == this) {
            pokemon.state = InactivePokemonState()
        }
        subscriptions.forEach(ObservableSubscription<*>::unsubscribe)
        removalObservable.emit(reason)
    }

    // Copy and paste of how vanilla checks it, unfortunately no util method you can only add then wait for the result
    private fun hasRoomToMount(player: PlayerEntity): Boolean {
        return (player.shoulderEntityLeft.isEmpty || player.shoulderEntityRight.isEmpty)
                && !player.hasVehicle()
                && player.isOnGround
                && !player.isTouchingWater
                && !player.inPowderSnow
    }

    override fun drop(source: DamageSource?) {
        if (pokemon.isWild()) {
            super.drop(source)
            delegate.drop(source)
        }
    }

    override fun updatePostDeath() {
        super.updatePostDeath()
        delegate.updatePostDeath()
    }

    private fun updateEyeHeight() {
        @Suppress("CAST_NEVER_SUCCEEDS")
        (this as com.cobblemon.mod.common.mixin.accessor.AccessorEntity).standingEyeHeight(this.getActiveEyeHeight(EntityPose.STANDING, this.type.dimensions))
    }

    fun getIsSubmerged() = isInLava || isSubmergedInWater
    override fun getPoseType(): PoseType = this.poseType.get()
}