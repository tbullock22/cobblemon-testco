/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.math

import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random

infix fun Int.pow(power: Int): Int {
    return toDouble().pow(power.toDouble()).toInt()
}

fun Double?.orMax() = this ?: Double.MAX_VALUE
fun Double?.orMin() = this ?: Int.MIN_VALUE.toDouble()
fun Float?.orMax() = this ?: Int.MAX_VALUE.toFloat()
fun Float?.orMin() = this ?: Int.MIN_VALUE.toFloat()
fun Int?.orMax() = this ?: Int.MAX_VALUE
fun Int?.orMin() = this ?: Int.MIN_VALUE

fun Int.toRGB(): Triple<Double, Double, Double> {
    val r = ((this shr 16) and 0b11111111) / 255.0
    val g = ((this shr 8) and 0b11111111) / 255.0
    val b = (this and 0b11111111) / 255.0
    return Triple(r, g, b)
}

fun IntRange.intersects(other: IntRange) = start in other || endInclusive in other || other.start in this
fun IntRange.intersection(other: IntRange): IntRange {
    val intersectionStart = max(other.start, start)
    val intersectionEnd = min(other.endInclusive, endInclusive)

    return intersectionStart..intersectionEnd
}

fun Pair<Float, Float>.random() = Random.nextFloat() * (second - first) + first

fun Float.remap(from: Pair<Float, Float>, to: Pair<Float, Float>): Float {
    val (fromMin, fromMax) = from
    val (toMin, toMax) = to
    return (this - fromMin) / (fromMax - fromMin) * (toMax - toMin) + toMin
}

fun Float.remap(start: FloatRange, end: FloatRange): Float{
    val (fromMin, fromMax) = start.start to start.endInclusive
    val (toMin, toMax) = end.start to end.endInclusive
    return (this - fromMin) / (fromMax - fromMin) * (toMax - toMin) + toMin
}

fun Int.remap(from: Pair<Int, Int>, to: Pair<Int, Int>): Int {
    val (fromMin, fromMax) = from
    val (toMin, toMax) = to
    return (this - fromMin) / (fromMax - fromMin) * (toMax - toMin) + toMin
}

fun Int.remap(start: IntRange, end: IntRange): Int{
    val (fromMin, fromMax) = start.first to start.last
    val (toMin, toMax) = end.first to end.last
    return (this - fromMin) / (fromMax - fromMin) * (toMax - toMin) + toMin
}

fun Double.remap(from: Pair<Double, Double>, to: Pair<Double, Double>): Double {
    val (fromMin, fromMax) = from
    val (toMin, toMax) = to
    return (this - fromMin) / (fromMax - fromMin) * (toMax - toMin) + toMin
}

fun Double.remap(start: DoubleRange, end: DoubleRange): Double{
    val (fromMin, fromMax) = start.start to start.endInclusive
    val (toMin, toMax) = end.start to end.endInclusive
    return (this - fromMin) / (fromMax - fromMin) * (toMax - toMin) + toMin
}



class FloatRange(override val start: Float, override val endInclusive: Float) : ClosedFloatingPointRange<Float>, Iterable<Float> {
    override fun contains(value: Float): Boolean = value in start..endInclusive
    override fun isEmpty(): Boolean = start > endInclusive
    override fun lessThanOrEquals(a: Float, b: Float): Boolean {
        TODO("Not yet implemented")
    }
    override fun equals(other: Any?): Boolean = other is FloatRange && start == other.start && endInclusive == other.endInclusive
    override fun hashCode(): Int = 31 * start.hashCode() + endInclusive.hashCode()
    override fun iterator(): Iterator<Float> {
        TODO("Not yet implemented")
    }

    override fun toString(): String = "$start..$endInclusive"
}

class DoubleRange(override val start: Double, override val endInclusive: Double) : ClosedFloatingPointRange<Double>, Iterable<Double> {
    override fun contains(value: Double): Boolean = value in start..endInclusive
    override fun isEmpty(): Boolean = start > endInclusive
    override fun lessThanOrEquals(a: Double, b: Double): Boolean {
        TODO("Not yet implemented")
    }
    override fun equals(other: Any?): Boolean = other is DoubleRange && start == other.start && endInclusive == other.endInclusive
    override fun hashCode(): Int = 31 * start.hashCode() + endInclusive.hashCode()
    override fun iterator(): Iterator<Double> {
        TODO("Not yet implemented")
    }

    override fun toString(): String = "$start..$endInclusive"
}