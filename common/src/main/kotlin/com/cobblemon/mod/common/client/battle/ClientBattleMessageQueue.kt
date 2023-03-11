/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.battle

import net.minecraft.text.OrderedText

/**
 * A wrapper around the client battle message information. This is essentially an appending list of all
 * the battle messages the client knows about. It keeps track of where it is up to, so they must actively
 * progress the queue to see newer messages.
 *
 * @author Hiroku
 * @since June 24th, 2022
 */
class ClientBattleMessageQueue {
    var listeners = mutableListOf<(OrderedText) -> Unit>()
    private val messages = mutableListOf<OrderedText>()

    fun add(messages: Iterable<OrderedText>) {
        this.messages.addAll(messages)
        listeners.forEach { listener -> messages.forEach(listener) }
    }

    fun subscribe(listener: (OrderedText) -> Unit) {
        this.listeners.add(listener)
        messages.forEach(listener)
    }
}