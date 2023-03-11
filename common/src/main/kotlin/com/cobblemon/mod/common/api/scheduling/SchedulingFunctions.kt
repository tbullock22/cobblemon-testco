/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.scheduling

import com.cobblemon.mod.common.util.runOnServer

fun after(ticks: Int = 0, seconds: Float = 0F, serverThread: Boolean = false, action: () -> Unit) {
    ScheduledTaskTracker.addTask(
        ScheduledTask(
            action = { if (serverThread) runOnServer(action) else action() },
            delaySeconds = ticks / 20F + seconds
        )
    )
}

/**
 * Same as [after] but the task is made to run on the main thread. This is for when the task
 * being completed after the delay does things like entity removal or other thread-unsafe actions.
 */
fun afterOnMain(ticks: Int = 0, seconds: Float = 0F, action: () -> Unit) = after(ticks, seconds, true, action)


fun lerp(seconds: Float = 0F, serverThread: Boolean = false, action: (Float) -> Unit) {
    val startedTime = System.currentTimeMillis()
    var passed = 0F
    if (seconds == 0F) {
        if (serverThread) runOnServer { action(1F) } else action(1F)
        return
    }
    action(passed / seconds)
    if (passed / seconds != 1F) {
        taskBuilder().interval(0F).iterations(-1).execute { task ->
            passed = (System.currentTimeMillis() - startedTime)/1000F
            if (passed > seconds) {
                passed = seconds
            }
            val ratio = passed / seconds
            if (serverThread) runOnServer { action(ratio) } else action(ratio)
            if (passed >= seconds) {
                task.expire()
            }
        }.build()
    }
}

fun taskBuilder() = ScheduledTask.Builder()