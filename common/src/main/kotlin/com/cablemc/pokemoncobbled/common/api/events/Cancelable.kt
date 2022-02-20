package com.cablemc.pokemoncobbled.common.api.events

/**
 * Something that can be canceled. This is a highly complex class.
 *
 * @author Hiroku
 * @since February 18th, 2022
 */
abstract class Cancelable {
    var isCanceled: Boolean = false
        private set

    fun cancel() {
        isCanceled = true
    }
}