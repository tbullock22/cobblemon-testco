/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util

const val QUOTE = '"'

fun String.splitMap(delimiter: String, assigner: String) : MutableList<Pair<String, String?>>
{
    val result = mutableListOf<Pair<String, String?>>()
    val split = this.split(delimiter)

    var joiner: String? = null
    for (argument in split) {
        if (joiner != null && argument.endsWith(QUOTE)) {
            joiner += "$delimiter${argument.substring(0, argument.length - 1)}"

            val components = joiner.split(assigner)
            val key = components[0].lowercase()
            val value = if (joiner.contains(assigner)) {
                components[1]
            } else {
                null
            }

            joiner = null
            result.add(key to value)
        } else if (joiner == null) {
            if (argument.contains(assigner)) {
                val equalsIndex = argument.indexOf(assigner)
                val key = argument.substring(0, equalsIndex).lowercase()
                val value = argument.substring(equalsIndex + 1)

                if (value.startsWith(QUOTE)) {
                    if(value.endsWith(QUOTE)) {
                        result.add(key to value.substring(1, value.length - 1))
                    } else {
                        joiner = "$key$assigner${value.substring(1)}"
                    }
                } else {
                    result.add(key to value)
                }
            } else {
                if (argument.startsWith(QUOTE) && argument.endsWith(QUOTE)) {
                    result.add(argument.lowercase().substring(1, argument.length - 1) to null)
                } else if(!argument.contains(QUOTE)) {
                    result.add(argument.lowercase() to null)
                }
            }
        } else {
            joiner += "$delimiter$argument"
        }
    }

    return result
}