package com.cobblemon.mod.common.api.spawning

/**
 * A simple utility for combining multiple integer ranges.
 *
 * @author Hiroku
 * @since December 16th, 2022
 */
open class IntRanges() {
    constructor(vararg ranges: IntRange) : this() {
        this.ranges = ranges.toMutableList()
    }

    var ranges = mutableListOf<IntRange>()

    operator fun contains(value: Int) = ranges.any { value in it }
}