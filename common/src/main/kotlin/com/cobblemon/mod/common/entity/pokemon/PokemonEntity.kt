/*
 * Copyright (C) 2023 Cobblemon Contributors
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
import com.cobblemon.mod.common.api.events.entity.PokemonEntityLoadEvent
import com.cobblemon.mod.common.api.events.entity.PokemonEntitySaveEvent
import com.cobblemon.mod.common.api.events.entity.PokemonEntitySaveToWorldEvent
import com.cobblemon.mod.common.api.events.pokemon.ShoulderMountEvent
import com.cobblemon.mod.common.api.net.serializers.PoseTypeDataSerializer
import com.cobblemon.mod.common.api.net.serializers.StringSetDataSerializer
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.pokemon.feature.FlagSpeciesFeature
import com.cobblemon.mod.common.api.pokemon.status.Statuses
import com.cobblemon.mod.common.api.reactive.ObservableSubscription
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.api.scheduling.afterOnMain
import com.cobblemon.mod.common.api.types.ElementalTypes
import com.cobblemon.mod.common.api.types.ElementalTypes.FIRE
import com.cobblemon.mod.common.battles.BattleRegistry
import com.cobblemon.mod.common.entity.EntityProperty
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.Poseable
import com.cobblemon.mod.common.entity.data.CobblemonTrackedDataHandlerRegistry
import com.cobblemon.mod.common.entity.pokemon.ai.PokemonMoveControl
import com.cobblemon.mod.common.entity.pokemon.ai.PokemonNavigation
import com.cobblemon.mod.common.entity.pokemon.ai.goals.*
import com.cobblemon.mod.common.entity.pokemon.data.PokemonDisplayNameState
import com.cobblemon.mod.common.item.interactive.PokemonInteractiveItem
import com.cobblemon.mod.common.net.messages.client.sound.UnvalidatedPlaySoundS2CPacket
import com.cobblemon.mod.common.net.messages.client.ui.InteractPokemonUIPacket
import com.cobblemon.mod.common.net.serverhandling.storage.SEND_OUT_DURATION
import com.cobblemon.mod.common.pokemon.FormData
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.pokemon.activestate.ActivePokemonState
import com.cobblemon.mod.common.pokemon.activestate.InactivePokemonState
import com.cobblemon.mod.common.pokemon.activestate.ShoulderedState
import com.cobblemon.mod.common.pokemon.ai.FormPokemonBehaviour
import com.cobblemon.mod.common.pokemon.evolution.variants.ItemInteractionEvolution
import com.cobblemon.mod.common.util.*
import dev.architectury.extensions.network.EntitySpawnExtension
import dev.architectury.networking.NetworkManager
import net.minecraft.block.BlockState
import net.minecraft.entity.*
import net.minecraft.entity.ai.control.MoveControl
import net.minecraft.entity.ai.goal.EatGrassGoal
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.entity.ai.pathing.PathNodeType
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.entity.passive.AnimalEntity
import net.minecraft.entity.passive.PassiveEntity
import net.minecraft.entity.passive.TameableShoulderEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.FluidState
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsage
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents
import net.minecraft.tag.FluidTags
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraft.world.event.GameEvent
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.math.round
import kotlin.math.roundToInt
import kotlin.math.sqrt

class PokemonEntity(
    world: World,
    pokemon: Pokemon = Pokemon(),
    type: EntityType<out PokemonEntity> = CobblemonEntities.POKEMON.get(),
) : TameableShoulderEntity(type, world), EntitySpawnExtension, Poseable, Shearable {
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
            // ToDo enable once nickname is implemented
            /*
            if (this.dataTracker.get(POKEMON_NAME_STATE).shouldRender) {
                this.updateNameState(if (value.nickame != null) PokemonNameState.NICKNAME else PokemonNameState.SPECIES)
            }
             */
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

    /**
     * The amount of steps this entity has taken.
     */
    var steps: Int = 0

    val entityProperties = mutableListOf<EntityProperty<*>>()

    val species = addEntityProperty(SPECIES, pokemon.species.resourceIdentifier.toString())
    val isMoving = addEntityProperty(MOVING, false)
    val behaviourFlags = addEntityProperty(BEHAVIOUR_FLAGS, 0)
    val phasingTargetId = addEntityProperty(PHASING_TARGET_ID, -1)
    val battleId = addEntityProperty(BATTLE_ID, Optional.empty())
    val aspects = addEntityProperty(ASPECTS, pokemon.aspects)
    val deathEffectsStarted = addEntityProperty(DYING_EFFECTS_STARTED, false)
    val poseType = addEntityProperty(POSE_TYPE, PoseType.NONE)
    // ToDo uncomment and remove fixed PokemonNameState.SPECIES start once nicknames are implemented.
    // val nameDisplayState = addEntityProperty(POKEMON_NAME_STATE, if (pokemon.nickame != null) PokemonNameState.NICKNAME else PokemonNameState.SPECIES)
    val displayNameState = addEntityProperty(POKEMON_NAME_STATE, PokemonDisplayNameState.SPECIES)
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

        subscriptions.add(
            species.subscribe {
                calculateDimensions()
            }
        )
    }

    companion object {
        val POKEMON_NAME_STATE = DataTracker.registerData(PokemonEntity::class.java, CobblemonTrackedDataHandlerRegistry.POKEMON_DISPLAY_NAME_STATE)
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

    override fun handleStatus(status: Byte) {
        delegate.handleStatus(status)
        super.handleStatus(status)
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
        return FIRE in pokemon.types || !behaviour.moving.swim.hurtByLava
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

    override fun isInvulnerableTo(damageSource: DamageSource): Boolean {
        // If the entity is busy, it cannot be hurt.
        if (busyLocks.isNotEmpty()) {
            return true
        }

        // Owned Pokémon cannot be hurt by players or suffocation
        if (ownerUuid != null && (damageSource.attacker is PlayerEntity || damageSource == DamageSource.IN_WALL)) {
            return true
        }

        if (!Cobblemon.config.playerDamagePokemon && damageSource.attacker is PlayerEntity) {
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

    override fun writeNbt(nbt: NbtCompound): NbtCompound {
        val ownerId = ownerUuid
        if (ownerId != null) {
            nbt.putUuid(DataKeys.POKEMON_OWNER_ID, ownerId)
        }

        nbt.put(DataKeys.POKEMON, pokemon.saveToNBT(NbtCompound()))
        val battleIdToSave = battleId.get().orElse(null)
        if (battleIdToSave != null) {
            nbt.putUuid(DataKeys.POKEMON_BATTLE_ID, battleIdToSave)
        }
        nbt.putString(DataKeys.POKEMON_POSE_TYPE, poseType.get().name)
        nbt.putByte(DataKeys.POKEMON_BEHAVIOUR_FLAGS, behaviourFlags.get())

        CobblemonEvents.POKEMON_ENTITY_SAVE.post(PokemonEntitySaveEvent(this, nbt))

        return super.writeNbt(nbt)
    }

    override fun readNbt(nbt: NbtCompound) {
        super.readNbt(nbt)
        if (nbt.containsUuid(DataKeys.POKEMON_OWNER_ID)) {
            ownerUuid = nbt.getUuid(DataKeys.POKEMON_OWNER_ID)
        }
        pokemon = Pokemon().loadFromNBT(nbt.getCompound(DataKeys.POKEMON))
        species.set(pokemon.species.resourceIdentifier.toString())
        labelLevel.set(pokemon.level)
        val savedBattleId = if (nbt.containsUuid(DataKeys.POKEMON_BATTLE_ID)) nbt.getUuid(DataKeys.POKEMON_BATTLE_ID) else null
        if (savedBattleId != null) {
            val battle = BattleRegistry.getBattle(savedBattleId)
            if (battle != null) {
                battleId.set(Optional.of(savedBattleId))
            }
        }
        poseType.set(PoseType.valueOf(nbt.getString(DataKeys.POKEMON_POSE_TYPE)))
        behaviourFlags.set(nbt.getByte(DataKeys.POKEMON_BEHAVIOUR_FLAGS))
        this.setBehaviourFlag(PokemonBehaviourFlag.EXCITED, true)

        CobblemonEvents.POKEMON_ENTITY_LOAD.postThen(
            event = PokemonEntityLoadEvent(this, nbt),
            ifSucceeded = {},
            ifCanceled = { this.discard() }
        )
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
            goalSelector.add(0, PokemonInBattleMovementGoal(this, 10))
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

            if (pokemon.getFeature<FlagSpeciesFeature>(DataKeys.HAS_BEEN_SHEARED) != null) {
                goalSelector.add(5, EatGrassGoal(this))
            }

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
        val itemStack = player.getStackInHand(hand)
        if (player is ServerPlayerEntity) {
            if (itemStack.isOf(Items.SHEARS) && this.isShearable) {
                this.sheared(SoundCategory.PLAYERS)
                this.emitGameEvent(GameEvent.SHEAR, player)
                itemStack.damage(1, player) { it.sendToolBreakStatus(hand) }
                return ActionResult.SUCCESS
            }
            else if (itemStack.isOf(Items.BUCKET)) {
                if (pokemon.getFeature<FlagSpeciesFeature>(DataKeys.CAN_BE_MILKED) != null) {
                    world.playSoundFromEntity(
                        null,
                        this,
                        SoundEvents.ENTITY_GOAT_MILK,
                        SoundCategory.PLAYERS,
                        1.0F,
                        1.0F
                    )
                    val milkBucket = ItemUsage.exchangeStack(itemStack, player, ItemStack(Items.MILK_BUCKET))
                    player.setStackInHand(hand, milkBucket)
                    return ActionResult.SUCCESS
                }
            }
        }


        if (hand == Hand.MAIN_HAND && player is ServerPlayerEntity && pokemon.getOwnerPlayer() == player) {
            if (player.isSneaking) {
                InteractPokemonUIPacket(this.getUuid(), isReadyToSitOnPlayer).sendToPlayer(player)
            } else {
                // TODO #105
                if (this.attemptItemInteraction(player, player.getStackInHand(hand))) return ActionResult.SUCCESS
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
        buffer.writeString(pokemon.form.formOnlyShowdownId())
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
            val formId = buffer.readString()
            pokemon.form = pokemon.species.forms.find { form -> form.formOnlyShowdownId() == formId } ?: pokemon.species.standardForm
            phasingTargetId.set(buffer.readInt())
            beamModeEmitter.set(buffer.readByte())
            this.aspects.set(buffer.readList(PacketByteBuf::readString).toSet())
            labelLevel.set(buffer.readInt())
        }
    }

    override fun shouldSave(): Boolean {
        if (ownerUuid == null && Cobblemon.config.savePokemonToWorld) {
            CobblemonEvents.POKEMON_ENTITY_SAVE_TO_WORLD.postThen(PokemonEntitySaveToWorldEvent(this)) {
                return true
            }
        }
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

    override fun playAmbientSound() {
        if (!this.isSilent) {
            val sound = Identifier(this.pokemon.species.resourceIdentifier.namespace, "pokemon.${this.pokemon.showdownId()}.ambient")
            // ToDo distance to travel is currently hardcoded to default we can maybe find a way to work around this down the line
            UnvalidatedPlaySoundS2CPacket(sound, this.soundCategory, this.x, this.y, this.z, this.soundVolume, this.soundPitch)
                .sendToPlayersAround(this.x, this.y, this.z, 16.0, this.world.registryKey)
        }
    }

    // We never want to allow an actual sound event here, we do not register our sounds to the sound registry as species are loaded by the time the registry is frozen.
    // Super call would do the same but might as well future-proof.
    override fun getAmbientSound(): SoundEvent? {
        return null
    }

    override fun getMinAmbientSoundDelay(): Int {
        return Cobblemon.config.ambientPokemonCryTicks
    }

    private fun attemptItemInteraction(player: PlayerEntity, stack: ItemStack): Boolean {
        if (player !is ServerPlayerEntity || this.isBusy) {
            return false
        }
        if (!stack.isEmpty) {
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
            shouldSave()
            (stack.item as? PokemonInteractiveItem)?.let {
                if (it.onInteraction(player, this, stack)) {
                    this.world.playSoundServer(position = this.pos, sound = CobblemonSounds.ITEM_USE.get(), volume = 1F, pitch = 1F)
                    return true
                }
            }
        }
        return false
    }

    fun offerHeldItem(player: PlayerEntity, stack: ItemStack): Boolean {
        if (player !is ServerPlayerEntity || this.isBusy || this.pokemon.getOwnerPlayer() != player) {
            return false
        }
        // We want the count of 1 in order to match the ItemStack#areEqual
        val giving = stack.copy().apply { count = 1 }
        val possibleReturn = this.pokemon.heldItemNoCopy()
        if (stack.isEmpty && possibleReturn.isEmpty) {
            return false
        }
        if (ItemStack.areEqual(giving, possibleReturn)) {
            player.sendMessage(lang("held_item.already_holding", this.pokemon.displayName, stack.name))
            return true
        }
        val returned = this.pokemon.swapHeldItem(stack, !player.isCreative)
        val text = when {
            giving.isEmpty -> lang("held_item.take", returned.name, this.pokemon.displayName)
            returned.isEmpty -> lang("held_item.give", this.pokemon.displayName, giving.name)
            else -> lang("held_item.replace", returned.name, this.pokemon.displayName, giving.name)
        }
        player.giveOrDropItemStack(returned)
        player.sendMessage(text)
        return true
    }

    fun tryMountingShoulder(player: ServerPlayerEntity): Boolean {
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
                            this.world.playSoundServer(position = this.pos, sound = SoundEvents.ENTITY_ITEM_PICKUP, volume = 0.7F, pitch = 1.4F)
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
    fun hasRoomToMount(player: PlayerEntity): Boolean {
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
        // Do not invoke super we need to keep a tight lid on this due to the Thorium mod forcing the ticks to a max of 20 on server side if we invoke a field update here
        // Client delegate is mimicking expected behavior on client end.
        delegate.updatePostDeath()
    }

    override fun onEatingGrass() {
        super.onEatingGrass()

        val feature = pokemon.getFeature<FlagSpeciesFeature>(DataKeys.HAS_BEEN_SHEARED)
        if (feature != null) {
            feature.enabled = false
            pokemon.markFeatureDirty(feature)
            pokemon.updateAspects()
        }
    }

    override fun travel(movementInput: Vec3d) {
        val previousX = this.x
        val previousY = this.y
        val previousZ = this.z
        super.travel(movementInput)
        val xDiff = this.x - previousX
        val yDiff = this.y - previousY
        val zDiff = this.z - previousZ
        this.updateWalkedSteps(xDiff, yDiff, zDiff)
    }

    private fun updateWalkedSteps(xDiff: Double, yDiff: Double, zDiff: Double) {
        // Riding or falling shouldn't count, other movement sources are fine
        if (!this.hasVehicle() || !this.isFallFlying) {
            return
        }
        val stepsTaken = when {
            this.isSwimming || this.isSubmergedIn(FluidTags.WATER) -> round(sqrt(xDiff * xDiff + yDiff * yDiff + zDiff * zDiff) * 100F)
            this.isClimbing -> round(yDiff * 100F)
            // Walking, flying or touching water
            else -> round(sqrt(xDiff * xDiff + zDiff * zDiff) * 100F)
        }
        if (stepsTaken > 0) {
            this.steps += stepsTaken.roundToInt()
        }
    }

    private fun updateEyeHeight() {
        @Suppress("CAST_NEVER_SUCCEEDS")
        (this as com.cobblemon.mod.common.mixin.accessor.AccessorEntity).standingEyeHeight(this.getActiveEyeHeight(EntityPose.STANDING, this.type.dimensions))
    }

    fun getIsSubmerged() = isInLava || isSubmergedInWater
    override fun getPoseType(): PoseType = this.poseType.get()


    // ToDo START - Review when implementing nicknames

    /**
     * Returns the [Species.translatedName] of the backing [pokemon].
     *
     * @return The [Species.translatedName] of the backing [pokemon].
     */
    override fun getDefaultName(): Text = this.pokemon.species.translatedName

    /**
     * Returns the name of this entity based on their [displayNameState].
     * If the result of [PokemonDisplayNameState.nameResolver] is null returns the [getDefaultName].
     *
     * @return The current name of this entity.
     */
    override fun getName(): Text = this.displayNameState.get().nameResolver(this.pokemon) ?: this.defaultName

    /**
     * Returns the custom name of this entity, in the context of Cobblemon it is the [Pokemon.nickname].
     * At this time Cobblemon does not have the nickname feature as such this function always returns null.
     *
     * @return The nickname of the backing [pokemon].
     */
    override fun getCustomName(): Text? = null//PokemonNameState.NICKNAME.nameResolver(this.pokemon)

    /**
     * Sets the custom name of this entity.
     * In the context of a Pokémon entity this affects the [Pokemon.nickname].
     * At this time Cobblemon does not have the nickname feature as such this function does not do anything.
     *
     * @param name The new name being set, if null the [Pokemon.nickname] is removed.
     */
    override fun setCustomName(name: Text?) {
        // We do this as a compromise to keep as much compatibility as possible with other mods expecting this entity to act like a vanilla one
        // this.pokemon.nickname = name
        // this.dataTracker.set(DISPLAYED_NAME, DisplayNameMode.NICKNAME)
    }

    /**
     * Checks if the backing [pokemon] has a non-null [Pokemon.nickname].
     * At this time Cobblemon does not have the nickname feature as such this function always returns false.
     *
     * @return If the backing [pokemon] has a non-null [Pokemon.nickname].
     */
    override fun hasCustomName(): Boolean = false /* pokemon.nickname != null */

    /**
     * This method toggles the visibility of the entity name,
     * Unlike the vanilla implementation in our context it sets the [displayNameState] to [PokemonDisplayNameState.NICKNAME] or [PokemonDisplayNameState.SPECIES].
     *
     * @param visible The state of custom name visibility.
     */
    override fun setCustomNameVisible(visible: Boolean) {
        // We do this as a compromise to keep as much compatibility as possible with other mods expecting this entity to act like a vanilla one
        this.displayNameState.set(if (visible) PokemonDisplayNameState.NICKNAME else PokemonDisplayNameState.SPECIES)
    }

    /**
     * In the context of a Pokémon entity this checks if the backing [displayNameState] is [PokemonDisplayNameState.NICKNAME].
     *
     * @return If the custom name of this entity should display, in this case the [getCustomName] is the nickname but if null the [getDefaultName] will be used.
     */
    override fun isCustomNameVisible(): Boolean = this.displayNameState.get() == PokemonDisplayNameState.NICKNAME

    /**
     * Checks if the [displayNameState] has the property [PokemonDisplayNameState.shouldRender] as true.
     *
     * @return If this entity should render the name label.
     */
    override fun shouldRenderName(): Boolean = this.displayNameState.get().shouldRender

    /**
     * Updates the underlying [displayNameState] with the given [state].
     * If the new state is [PokemonDisplayNameState.NICKNAME] [setCustomNameVisible] will be invoked with true.
     *
     * @param state The new [PokemonDisplayNameState].
     */
    fun updateNameState(state: PokemonDisplayNameState) {
        val current = this.displayNameState.get()
        if (state != current) {
            if (state == PokemonDisplayNameState.NICKNAME) {
                this.isCustomNameVisible = true
            }
            else {
                this.displayNameState.set(state)
            }
        }
    }

    /**
     * A shortcut for [updateNameState].
     * Sets the state as [PokemonDisplayNameState.NONE].
     */
    fun hideNameRendering() {
        this.updateNameState(PokemonDisplayNameState.NONE)
    }

    // ToDo END - Review when implementing nicknames

    override fun isBreedingItem(stack: ItemStack): Boolean = false

    override fun canBreedWith(other: AnimalEntity): Boolean = false

    override fun breed(world: ServerWorld, other: AnimalEntity) {}

    override fun sheared(shearedSoundCategory: SoundCategory) {
        this.world.playSoundFromEntity(null, this,SoundEvents.ENTITY_SHEEP_SHEAR, shearedSoundCategory, 1.0F, 1.0F)
        val feature = this.pokemon.getFeature<FlagSpeciesFeature>(DataKeys.HAS_BEEN_SHEARED) ?: return
        feature.enabled = true
        this.pokemon.markFeatureDirty(feature)
        this.pokemon.updateAspects()
        val i = this.random.nextInt(3)
        for (j in 0 until i) {
            val itemEntity = this.dropItem(Items.WHITE_WOOL, 1) ?: return
            itemEntity.velocity = itemEntity.velocity.add(
                ((this.random.nextFloat() - this.random.nextFloat()) * 0.1f).toDouble(),
                (this.random.nextFloat() * 0.05f).toDouble(),
                ((this.random.nextFloat() - this.random.nextFloat()) * 0.1f).toDouble()
            )
        }
    }

    override fun isShearable(): Boolean {
        val feature = this.pokemon.getFeature<FlagSpeciesFeature>(DataKeys.HAS_BEEN_SHEARED) ?: return false
        return !this.isBusy && !this.pokemon.isFainted() && !feature.enabled
    }

    override fun canUsePortals() = false
}