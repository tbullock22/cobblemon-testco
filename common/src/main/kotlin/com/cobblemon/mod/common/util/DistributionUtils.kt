/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util

import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.api.reactive.Observable
import dev.architectury.utils.Env
import dev.architectury.utils.EnvExecutor
import dev.architectury.utils.GameInstance
import java.util.concurrent.CompletableFuture
import net.fabricmc.api.EnvType
import net.minecraft.world.World

/** Runs the given [Runnable] if the caller is on the CLIENT side. */
fun ifClient(runnable: Runnable) {
    EnvExecutor.runInEnv(Env.CLIENT) { runnable }
}

/** Runs the given [Runnable] if the caller is on the SERVER side. */
fun ifServer(runnable: Runnable) {
    EnvExecutor.runInEnv(Env.SERVER) { runnable }
}

/** Runs the given [Runnable] if the caller is a dedicated server. */
fun ifDedicatedServer(action: Runnable) {
    EnvExecutor.runInEnv(EnvType.SERVER) { action }
}

/*
 * Schedules the given block of code to run on the main thread and returns a [CompletableFuture] that completes with the result of the block when the code has executed.
 */
fun <T> runOnServer(block: () -> T): CompletableFuture<T> {
    val future = CompletableFuture<T>()
    val server = getServer()
    if (server == null) {
        future.completeExceptionally(IllegalStateException("There is no server to schedule it on."))
    } else {
        server.execute { future.complete(block()) }
    }
    return future
}
fun <T> Observable<T>.subscribeOnServer(priority: Priority = Priority.NORMAL, block: () -> Unit) = subscribe(priority) { runOnServer(block) }

fun getServer() = GameInstance.getServer()

///** If you are not Cobblemon, don't touch this. If you end up doing client side stuff, you'll probably break stuff. */
//internal fun <T> runOnSide(side: LogicalSide, block: () -> T): CompletableFuture<T> {
//    val future = CompletableFuture<T>()
//    LogicalSidedProvider.WORKQUEUE.get(side).submit { future.complete(block()) }
//    return future
//}

fun World.isServerSide() = !isClient