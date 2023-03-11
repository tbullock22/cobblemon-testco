/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.detail

import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.api.spawning.context.SpawningContext
import com.cobblemon.mod.common.util.toVec3d
import net.minecraft.entity.Entity


/**
 * A scheduled spawning action.
 *
 * @author Hiroku
 * @since February 4th, 2022
 */
abstract class SpawnAction<T : Entity>(
    val ctx: SpawningContext,
    open val detail: SpawnDetail
) {
    abstract fun createEntity(): T?

    fun run() {
        ctx.influences.forEach { it.affectAction(this) }
        val e = createEntity() ?: return
        e.setPosition(ctx.position.toVec3d().add(0.5, 1.0, 0.5))
        entity.emit(e)
        ctx.world.spawnEntity(e)
    }

    /**
     * An observable for the entity that will eventually be spawned by this action.
     *
     * You can safely register subscriptions here for anything you want to do to the
     * entity after it's spawned, whenever that may be. This is tolerant of situations
     * where the entity spawn has been canceled, so that your action will not occur.
     */
    val entity: SimpleObservable<T> = SimpleObservable<T>().apply {
        subscribe { entity -> ctx.afterSpawn(entity) }
    }
}