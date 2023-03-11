/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage.adapter.flatifle

import com.cobblemon.mod.common.Cobblemon.LOGGER
import com.cobblemon.mod.common.api.storage.PokemonStore
import com.cobblemon.mod.common.api.storage.StorePosition
import java.io.EOFException
import java.io.File
import java.util.UUID
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtIo

/**
 * A [OneToOneFileStoreAdapter] that can arbitrarily save a single [PokemonStore] into an [NbtCompound] with the
 * help of Minecraft's [NbtIo]. This is arguably the best persistence method for [PokemonStore]s and is absolutely
 * the most efficient [FileStoreAdapter].
 *
 * @author Hiroku
 * @since November 30th, 2021
 */
open class NBTStoreAdapter(
    rootFolder: String,
    useNestedFolders: Boolean,
    folderPerClass: Boolean,
) : OneToOneFileStoreAdapter<NbtCompound>(rootFolder, useNestedFolders, folderPerClass, "dat") {
    override fun <E : StorePosition, T : PokemonStore<E>> serialize(store: T) = store.saveToNBT(NbtCompound())
    override fun save(file: File, serialized: NbtCompound) = NbtIo.writeCompressed(serialized, file)
    override fun <E, T : PokemonStore<E>> load(file: File, storeClass: Class<out T>, uuid: UUID): T? {
        val store = storeClass.getConstructor(UUID::class.java).newInstance(uuid)
        return try {
            val nbt = NbtIo.readCompressed(file)
            store.loadFromNBT(nbt)
            store
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}