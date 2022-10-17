package com.cablemc.pokemod.common.client.visual

import net.minecraft.entity.Entity
import net.minecraft.particle.DefaultParticleType
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec3d

abstract class VisualPattern {

    open fun execute(world: ServerWorld?, pos: Vec3d, particle: DefaultParticleType, activeTicks: Int) {}
    open fun execute(attachedEntity: Entity, particle: DefaultParticleType, activeTicks: Int) {}
    open fun parse(string: String, delimiter: String = " ", delimitStopper: String = "\"", assignmentOperator: String = "=") {}

    companion object {
        //TODO: This duplicates a private function in PokemonProperties. Consider separating the keypair functions from PokemonProperties into a separate class
        fun divideIntoKeyPairs(
            string: String,
            delimiter: String,
            delimitCounter: String,
            assignmentOperator: String
        ): MutableList<Pair<String, String?>> {
            val keyPairs = mutableListOf<Pair<String, String?>>()
            val delimited = string.split(delimiter)
            var aggregated: String? = null

            for (sub in delimited) {
                if (aggregated != null && sub.endsWith(delimitCounter)) {
                    aggregated += delimiter + sub.substring(0, sub.length - 1)
                    val key = aggregated.split(assignmentOperator)[0].lowercase()
                    val value = if (aggregated.contains(assignmentOperator)) {
                        aggregated.split(assignmentOperator)[1]
                    } else {
                        null
                    }
                    aggregated = null
                    keyPairs.add(key to value)
                } else if (aggregated == null) {
                    if (sub.contains(assignmentOperator)) {
                        val equalsIndex = sub.indexOf(assignmentOperator)
                        val key = sub.substring(0, equalsIndex).lowercase()
                        var valueComponent = sub.substring(equalsIndex + 1)
                        if (valueComponent.startsWith(delimitCounter)) {
                            valueComponent = valueComponent.substring(1)
                            if (valueComponent.endsWith(delimitCounter)) {
                                valueComponent = valueComponent.substring(0, valueComponent.length - 1)
                                keyPairs.add(key to valueComponent)
                            } else {
                                aggregated = key + assignmentOperator + valueComponent
                            }
                        } else {
                            keyPairs.add(key to valueComponent)
                        }
                    } else {
                        keyPairs.add(sub.lowercase() to null)
                    }
                } else {
                    aggregated += delimiter + sub
                }
            }

            return keyPairs
        }
    }
}