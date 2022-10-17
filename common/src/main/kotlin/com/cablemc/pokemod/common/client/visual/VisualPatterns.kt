package com.cablemc.pokemod.common.client.visual

import com.cablemc.pokemod.common.client.visual.patterns.OrbitPattern

object VisualPatterns {
    private val allPatterns = mutableMapOf<String, VisualPattern>()

    init {
        allPatterns += mutableMapOf(
            "orbit" to OrbitPattern(),
            // "particle_beam" to ParticleBeamPattern(),
            // "throw_projectile" to ThrowProjectilePattern()
        )
    }

    fun getByName(name: String) = allPatterns[name.lowercase()]
    fun getPatterns(): Collection<String> = allPatterns.keys.toSet()
}