package com.cablemc.pokemod.common.client.visual.patterns

import com.cablemc.pokemod.common.client.visual.VisualPattern
import net.minecraft.particle.DefaultParticleType
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec3d
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class OrbitPattern(
    var count: Int = 5,
    var radius: Double = 2.0,
    var speed: Double = 2.0 // represents rotation speed in degrees per tick
) : VisualPattern() {
    override fun execute(world: ServerWorld?, pos: Vec3d, particle: DefaultParticleType, activeTicks: Int) {
        for (i in 0 until count) {
            val baseAngle = PI * (activeTicks * speed) / 180
            val theta = baseAngle + (i * (2 * PI / count))
            val offsetX = radius * cos(theta)
            val offsetZ = radius * sin(theta)
            world?.spawnParticles(
                particle,
                pos.x + offsetX,
                pos.y,
                pos.z + offsetZ,
                1,
                0.0,
                0.0,
                0.0,
                0.0
            )
        }
    }

    override fun parse(string: String, delimiter: String, delimitStopper: String, assignmentOperator: String) {
        val keyPairs = divideIntoKeyPairs(string, delimiter, delimitStopper, assignmentOperator)
        //TODO: Consider using parse___Properties from PokemonProperties (move those functions into a public class)
        keyPairs.forEach {
            when (it.first.lowercase()) {
                "count" -> with(it.second?.toIntOrNull()) { if (this != null) count = this }
                "radius" -> with(it.second?.toDoubleOrNull()) { if (this != null) radius = this }
                "r" -> with(it.second?.toDoubleOrNull()) { if (this != null) radius = this }
                "speed" -> with(it.second?.toDoubleOrNull()) { if (this != null) speed = this }
                "spd" -> with(it.second?.toDoubleOrNull()) { if (this != null) speed = this }
            }
        }
    }
}