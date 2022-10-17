package com.cablemc.pokemod.common.client.visual

import com.cablemc.pokemod.common.client.visual.patterns.OrbitPattern
import net.minecraft.entity.Entity
import net.minecraft.particle.DefaultParticleType
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec3d

class Visual(
    var particle: DefaultParticleType = ParticleTypes.FLAME,
    var world: ServerWorld? = null,
    var pos: Vec3d = Vec3d(0.0, 64.0, 0.0),
    var activeTicks: Int = 0,
    var duration: Int = 60,
    var pattern: VisualPattern = OrbitPattern()
) {

    constructor(
        particle: DefaultParticleType,
        attachedEntity: Entity,
        activeTicks: Int,
        duration: Int,
        pattern: VisualPattern
    ) : this(particle, attachedEntity.world as ServerWorld?, attachedEntity.pos, activeTicks, duration, pattern) {
        var attachedEntity: Entity? = attachedEntity
    }

    fun tick(): Boolean {
        pattern.execute(world, pos, particle, activeTicks)
        activeTicks++
        return activeTicks >= duration
    }
}