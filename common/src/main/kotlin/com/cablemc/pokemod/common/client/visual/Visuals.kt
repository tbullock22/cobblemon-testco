package com.cablemc.pokemod.common.client.visual

/**
 * Visuals are the "special effects" of Pokémod. These are most commonly used for move effects.
 * For example, when a Pokémon uses Flamethrower, the beam of flame particles is a Visual.
 *
 * Key Terms:
 * - Pattern (see: [VisualPattern] and [VisualPatterns])
 *   - A Pattern determines where particles or projectiles will spawn and how they will move.
 *   - Patterns each have unique properties. Examples include count, radius, width, spread, etc.
 * - Visual (see: [Visual] and [Visuals])
 *   - A Visual is a completed instance of a Pattern; when a Pattern's properties are all set, it can be registered as a Visual.
 *   - Common variables that all Patterns share (Duration, Position, etc.) are stored in [Visual]
 * - Visual Presets (WIP)
 *   - Commonly-used Visuals may be saved as Presets. This is useful for moves, since there are many moves that will use the same Visual to represent them.
 *   - For example, SpecialAttackFire is a Visual that uses the ParticleBeam Pattern with Flame particles
 *
 * @author AnneOminous
 * @since October 17th, 2022
 */

object Visuals {
    private val activeVisuals = mutableListOf<Visual>()

    fun registerVisual(visual: Visual) {
        activeVisuals.add(visual)
    }

    fun unregisterVisual(visual: Visual) {
        activeVisuals.remove(visual)
    }

    fun onServerStarted() {
        activeVisuals.clear()
    }

    fun tick() {
        activeVisuals.removeIf{ it.tick() }
    }
}