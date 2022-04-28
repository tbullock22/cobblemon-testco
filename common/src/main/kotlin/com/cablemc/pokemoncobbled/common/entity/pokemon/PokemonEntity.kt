package com.cablemc.pokemoncobbled.common.entity.pokemon

import com.cablemc.pokemoncobbled.common.CobbledEntities
import com.cablemc.pokemoncobbled.common.api.events.CobbledEvents
import com.cablemc.pokemoncobbled.common.api.events.pokemon.ShoulderMountEvent
import com.cablemc.pokemoncobbled.common.api.pokemon.PokemonSpecies
import com.cablemc.pokemoncobbled.common.api.scheduling.afterOnMain
import com.cablemc.pokemoncobbled.common.api.storage.party.PlayerPartyStore
import com.cablemc.pokemoncobbled.common.api.types.ElementalTypes
import com.cablemc.pokemoncobbled.common.client.entity.PokemonClientDelegate
import com.cablemc.pokemoncobbled.common.entity.EntityProperty
import com.cablemc.pokemoncobbled.common.item.interactive.PokemonInteractiveItem
import com.cablemc.pokemoncobbled.common.pokemon.Pokemon
import com.cablemc.pokemoncobbled.common.pokemon.activestate.ShoulderedState
import com.cablemc.pokemoncobbled.common.util.DataKeys
import com.cablemc.pokemoncobbled.common.util.getBitForByte
import com.cablemc.pokemoncobbled.common.util.setBitForByte
import dev.architectury.extensions.network.EntitySpawnExtension
import dev.architectury.networking.NetworkManager
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.Packet
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.AgeableMob
import net.minecraft.world.entity.EntityDimensions
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.Pose
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal
import net.minecraft.world.entity.ai.goal.Goal
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal
import net.minecraft.world.entity.animal.ShoulderRidingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import java.util.EnumSet
import java.util.Optional
import java.util.UUID

class PokemonEntity(
    level: Level,
    pokemon: Pokemon = Pokemon(),
    type: EntityType<out PokemonEntity> = CobbledEntities.POKEMON_TYPE,
) : ShoulderRidingEntity(type, level), EntitySpawnExtension {
    var pokemon: Pokemon = pokemon
        set(value) {
            field = value
            delegate.changePokemon(value)
        }
    var despawner: Despawner<PokemonEntity> = PokemonCobbled.defaultPokemonDespawner

    val delegate = if (level.isClientSide) {
        // Don't import because scanning for imports is a CI job we'll do later to detect errant access to client from server
        PokemonClientDelegate()
    } else {
        PokemonServerDelegate()
    }

    var ticksLived = 0
    val busyLocks = mutableListOf<Any>()
    val isBusy: Boolean
        get() = busyLocks.isNotEmpty()

    val entityProperties = mutableListOf<EntityProperty<*>>()

    val dexNumber = addEntityProperty(SPECIES_DEX, pokemon.species.nationalPokedexNumber)
    val shiny = addEntityProperty(SHINY, pokemon.shiny)
    val isMoving = addEntityProperty(MOVING, false)
    val behaviourFlags = addEntityProperty(BEHAVIOUR_FLAGS, 0)
    val phasingTargetId = addEntityProperty(PHASING_TARGET_ID, -1)
    val battleId = addEntityProperty(BATTLE_ID, Optional.empty())

    /**
     * 0 is do nothing,
     * 1 is appearing from a pokeball so needs to be small then grows,
     * 2 is being captured/recalling so starts large and shrinks.
     */
    val beamModeEmitter = addEntityProperty(BEAM_MODE, 0.toByte())
    // properties like the above are synced and can be subscribed to for changes on either side

    init {
        delegate.initialize(this)
        delegate.changePokemon(pokemon)
        refreshDimensions()

        battleId
            .subscribeIncludingCurrent {
                if (it.isPresent) {
                    busyLocks.add(BATTLE_LOCK)
                } else {
                    busyLocks.remove(BATTLE_LOCK)
                }
            }
            .unsubscribeWhen { isRemoved }
    }

    companion object {
        private val SPECIES_DEX = SynchedEntityData.defineId(PokemonEntity::class.java, EntityDataSerializers.INT)
        private val SHINY = SynchedEntityData.defineId(PokemonEntity::class.java, EntityDataSerializers.BOOLEAN)
        private val MOVING = SynchedEntityData.defineId(PokemonEntity::class.java, EntityDataSerializers.BOOLEAN)
        private val BEHAVIOUR_FLAGS = SynchedEntityData.defineId(PokemonEntity::class.java, EntityDataSerializers.BYTE)
        private val PHASING_TARGET_ID = SynchedEntityData.defineId(PokemonEntity::class.java, EntityDataSerializers.INT)
        private val BEAM_MODE = SynchedEntityData.defineId(PokemonEntity::class.java, EntityDataSerializers.BYTE)
        private val BATTLE_ID = SynchedEntityData.defineId(PokemonEntity::class.java, EntityDataSerializers.OPTIONAL_UUID)


        const val BATTLE_LOCK = "battle"
    }

    override fun tick() {
        super.tick()
        // We will be handling idle logic ourselves thank you
        this.noActionTime = 0
        entityProperties.forEach { it.checkForUpdate() }
        delegate.tick(this)
        ticksLived++
    }

    /**
     * Prevents water type Pokémon from taking drowning damage.
     */
    override fun canBreatheUnderwater(): Boolean {
        return ElementalTypes.WATER in pokemon.types
    }

    /**
     * Prevents fire type Pokémon from taking fire damage.
     */
    override fun fireImmune(): Boolean {
        return ElementalTypes.FIRE in pokemon.types
    }

    /**
     * Prevents flying type Pokémon from taking fall damage.
     */
    override fun checkFallDamage(pY: Double, pOnGround: Boolean, pState: BlockState, pPos: BlockPos) {
        if (ElementalTypes.FLYING in pokemon.types) {
            super.resetFallDistance()
        } else {
            super.checkFallDamage(pY, pOnGround, pState, pPos)
        }
    }

    override fun save(nbt: CompoundTag): Boolean {
        nbt.put(DataKeys.POKEMON, pokemon.saveToNBT(CompoundTag()))
        return super.save(nbt)
    }

    override fun saveWithoutId(nbt: CompoundTag): CompoundTag {
        nbt.put(DataKeys.POKEMON, pokemon.saveToNBT(CompoundTag()))
        return super.saveWithoutId(nbt)
    }

    override fun load(nbt: CompoundTag) {
        super.load(nbt)
        pokemon = Pokemon().loadFromNBT(nbt.getCompound(DataKeys.POKEMON))
        dexNumber.set(pokemon.species.nationalPokedexNumber)
        shiny.set(pokemon.shiny)
        speed = 0.35F
    }

    override fun getAddEntityPacket(): Packet<*> {
        return NetworkManager.createAddEntityPacket(this)
    }

    public override fun registerGoals() {
        goalSelector.removeAllGoals()
        goalSelector.addGoal(0, object : Goal() {
            override fun canUse() = this@PokemonEntity.phasingTargetId.get() != -1
            override fun getFlags() = EnumSet.allOf(Flag::class.java)
        })
        goalSelector.addGoal(3, FollowOwnerGoal(this, 0.6, 8F, 2F, false))
        goalSelector.addGoal(4, WaterAvoidingRandomStrollGoal(this, 0.33))
        goalSelector.addGoal(5, LookAtPlayerGoal(this, ServerPlayer::class.java, 5F))
    }

    fun <T> addEntityProperty(accessor: EntityDataAccessor<T>, initialValue: T): EntityProperty<T> {
        val property = EntityProperty(
            entityData = entityData,
            accessor = accessor,
            initialValue = initialValue
        )
        entityProperties.add(property)
        return property
    }

    override fun getBreedOffspring(level: ServerLevel, partner: AgeableMob) = null

    override fun canSitOnShoulder(): Boolean {
        return pokemon.form.shoulderMountable
    }

    override fun mobInteract(player: Player, hand: InteractionHand) : InteractionResult {
        this.attemptItemInteraction(player, player.getItemInHand(hand))
        if (player.isCrouching && hand == InteractionHand.MAIN_HAND) {
            if (canSitOnShoulder() && player is ServerPlayer && !isBusy) {
                val store = pokemon.storeCoordinates.get()?.store
                if (store is PlayerPartyStore && store.playerUUID == player.uuid) {
                    CobbledEvents.SHOULDER_MOUNT.postThen(ShoulderMountEvent(player, pokemon, isLeft = player.shoulderEntityLeft.isEmpty)) {
                        val dirToPlayer = player.eyePosition.subtract(position()).multiply(1.0, 0.0, 1.0).normalize()
                        deltaMovement = dirToPlayer.scale(0.8).add(0.0, 0.5, 0.0)
                        val lock = Any()
                        busyLocks.add(lock)
                        afterOnMain(seconds = 0.5F) {
                            busyLocks.remove(lock)
                            if (!isBusy && isAlive) {
                                val isLeft = player.shoulderEntityLeft.isEmpty
                                if (!isLeft || player.shoulderEntityRight.isEmpty) {
                                    pokemon.state = ShoulderedState(player.uuid, isLeft, pokemon.uuid)
                                    this.setEntityOnShoulder(player)
                                    this.pokemon.form.shoulderEffects.forEach { it.applyEffect(this.pokemon, player, isLeft) }
                                }
                            }
                        }
                    }
                }
            }
        }
        return super.mobInteract(player, hand)
    }

    override fun getDimensions(pose: Pose): EntityDimensions {
        val scale = pokemon.form.baseScale * pokemon.scaleModifier
        return pokemon.form.hitbox.scale(scale)
    }

    override fun saveAdditionalSpawnData(buffer: FriendlyByteBuf) {
        buffer.writeFloat(pokemon.scaleModifier)
        buffer.writeShort(pokemon.species.nationalPokedexNumber)
        buffer.writeUtf(pokemon.form.name)
        buffer.writeInt(phasingTargetId.get())
        buffer.writeByte(beamModeEmitter.get().toInt())
        buffer.writeBoolean(pokemon.shiny)
    }

    override fun loadAdditionalSpawnData(buffer: FriendlyByteBuf) {
        if (this.level.isClientSide) {
            pokemon.scaleModifier = buffer.readFloat()
            // TODO exception handling
            pokemon.species = PokemonSpecies.getByPokedexNumber(buffer.readUnsignedShort())!!
            // TODO exception handling
            pokemon.form = pokemon.species.forms.find { form -> form.name == buffer.readUtf() }!!
            phasingTargetId.set(buffer.readInt())
            beamModeEmitter.set(buffer.readByte())
            shiny.set(buffer.readBoolean())
        }
    }

    override fun shouldBeSaved(): Boolean {
        return false
    }

    override fun checkDespawn() {
        if (pokemon.getOwnerUUID() == null && despawner.shouldDespawn(this)) {
            discard()
        }
    }

    fun setBehaviourFlag(flag: PokemonBehaviourFlag, on: Boolean) {
        behaviourFlags.set(setBitForByte(behaviourFlags.get(), flag.bit, on))
    }

    fun getBehaviourFlag(flag: PokemonBehaviourFlag): Boolean = getBitForByte(behaviourFlags.get(), flag.bit)

    fun canBattle(player: Player): Boolean {
        if (isBusy) {
            return false
        }

        if (ownerUUID == player.uuid) {
            return false
        }

        return true
    }

    private fun attemptItemInteraction(playerIn: Player, stack: ItemStack) {
        if (playerIn !is ServerPlayer || stack.isEmpty) return
        (stack.item as? PokemonInteractiveItem)?.onInteraction(playerIn, this, stack)
    }

}