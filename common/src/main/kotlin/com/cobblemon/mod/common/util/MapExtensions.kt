package com.cobblemon.mod.common.util

fun <A, B> MutableMap<A, B>.removeIf(predicate: (Map.Entry<A, B>) -> Boolean) {
    val toRemove = mutableListOf<A>()
    for (entry in this) {
        if (predicate(entry)) {
            toRemove.add(entry.key)
        }
    }
    for (key in toRemove) {
        this.remove(key)
    }
}