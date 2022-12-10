/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.data

import com.cobblemon.mod.common.util.endsWith
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.nio.file.Path
import java.util.concurrent.ExecutionException
import kotlin.io.path.pathString
import net.minecraft.resource.ResourceManager
import net.minecraft.util.Identifier

/**
 * A [DataRegistry] that consumes JSON files.
 * Every deserialized instance is attached to an [Identifier].
 * For example a file under data/mymod/[resourcePath]/entry.json would be backed by the identifier modid:entry.
 *
 * @param T The type of the data consumed by this registry.
 *
 * @author Licious
 * @since August 5th, 2022
 */
interface JsonDataRegistry<T> : DataRegistry {

    /**
     * The [Gson] used to deserialize the data this registry will consume.
     */
    val gson: Gson

    /**
     * The [TypeToken] of type [T].
     */
    val typeToken: TypeToken<T>

    /**
     * The folder location for the data this registry will consume.
     */
    val resourcePath: String

    override fun reload(manager: ResourceManager) {
        val data = hashMapOf<Identifier, T>()
        manager.findResources(this.resourcePath) { path -> path.endsWith(JSON_EXTENSION) }.forEach { (identifier, resource) ->
            resource.inputStream.use { stream ->
                stream.bufferedReader().use { reader ->
                    val resolvedIdentifier = Identifier(identifier.namespace, File(identifier.path).nameWithoutExtension)
                    try {
                        data[resolvedIdentifier] = this.gson.fromJson(reader, this.typeToken.type)
                    } catch (exception: Exception) {
                        throw ExecutionException("Error loading JSON for data: $identifier", exception)
                    }
                }
            }
        }
        this.reload(data)
    }

    /**
     * Reloads this registry from the deserialized data.
     *
     * @param data A map of the data associating an instance to the respective identifier from the [ResourceManager].
     */
    fun reload(data: Map<Identifier, T>)

    companion object {
        private const val JSON_EXTENSION = ".json"
    }
}