/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util

import com.cobblemon.mod.common.net.IntSize
import io.netty.buffer.ByteBuf

fun ByteBuf.writeConditional(condition: () -> Boolean, writer: () -> Unit) {
    writeConditional(condition(), writer)
}

fun ByteBuf.writeConditional(shouldWrite: Boolean, writer: () -> Unit) {
    writeBoolean(shouldWrite)
    if (shouldWrite) {
        writer()
    }
}

fun ByteBuf.writeSizedInt(size: IntSize, value: Int) {
    when (size) {
        IntSize.INT -> writeInt(value)
        IntSize.SHORT, IntSize.U_SHORT -> writeShort(value)
        IntSize.BYTE, IntSize.U_BYTE -> writeByte(value)
    }
}

fun ByteBuf.readConditional(reader: () -> Unit) {
    val shouldRead = readBoolean()
    if (shouldRead) {
        reader()
    }
}

fun ByteBuf.readSizedInt(size: IntSize): Int {
    return when (size) {
        IntSize.INT -> readInt()
        IntSize.SHORT -> readShort().toInt()
        IntSize.U_SHORT -> readUnsignedShort()
        IntSize.BYTE -> readByte().toInt()
        IntSize.U_BYTE -> readUnsignedByte().toInt()
    }
}

fun ByteBuf.readTimes(size: IntSize = IntSize.U_BYTE, readEntry: () -> Unit) {
    val times = readSizedInt(size)
    repeat(times) { readEntry() }
}

fun <K, V> ByteBuf.writeMapK(size: IntSize = IntSize.U_BYTE, map: Map<K, V>, entryWriter: (Map.Entry<K, V>) -> Unit) {
    writeSizedInt(size, map.size)
    map.entries.forEach(entryWriter)
}

fun <K, V> ByteBuf.readMapK(size: IntSize = IntSize.U_BYTE, map: MutableMap<K, V>, entryReader: () -> Pair<K, V>) {
    val times = readSizedInt(size)
    repeat(times) {
        val (key, value) = entryReader()
        map[key] = value
    }
}