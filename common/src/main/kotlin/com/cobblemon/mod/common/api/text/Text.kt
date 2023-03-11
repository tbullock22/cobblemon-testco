/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.text

import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.MutableText
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier

/**
 * Text utility shamelessly stolen from Cable Libs
 *
 * @author Hiroku, Craft (on Cable Libs)
 */
class Text internal constructor() {

    companion object {
        internal fun resolveComponent(text: Any): MutableText {
            return Text.translatable(text.toString().replace("&[A-Fa-f\\dk-oK-oRr]".toRegex()) { "§${it.value.substring(1)}" })
        }
    }

    private var style = Style.EMPTY
    private var head: MutableText? = null

    fun parse(vararg components: Any): MutableText {
        components.forEach {
            when (it) {
                is MutableText -> {
                    addComponent(it)
                    style = getBlankStyle()
                }
                is ClickEvent -> style = style.withClickEvent(it)
                is HoverEvent -> style = style.withHoverEvent(it)
                is Formatting -> {
                    when {
                        it.isColor -> style = style.withColor(it)
                        it == Formatting.UNDERLINE || it == UNDERLINED -> style = style.withUnderline(true)
                        it == Formatting.BOLD || it == BOLD -> style = style.withBold(true)
                        it == Formatting.ITALIC || it == ITALIC -> style = style.withItalic(true)
                        it == Formatting.OBFUSCATED || it == OBFUSCATED -> style = style.withObfuscated(true)
                        it == Formatting.RESET || it == RESET -> style = Style.EMPTY
                    }
                }
                else -> addComponent(resolveComponent(it).also { it.style = style.withParent(it.style) })
            }
        }

        return head ?: Text.literal("Empty!")
    }

    private fun addComponent(component: MutableText) {
        if (head == null) {
            head = component
            component.style = style.withParent(component.style)
            style = getBlankStyle()
        } else {
            head?.add(component.also { it.style = style.withParent(it.style) })
        }
    }

    private fun getBlankStyle() = Style.EMPTY.withBold(false).withItalic(false).withUnderline(false).withObfuscated(false).withColor(
        Formatting.WHITE).withClickEvent(null).withHoverEvent(null)
}

fun text(vararg components: Any) = Text().parse(*components)

val textClickHandlers = hashMapOf<UUID, (p: ServerPlayerEntity) -> Unit>()

fun click(consumed: AtomicBoolean, action: (p: ServerPlayerEntity) -> Unit): ClickEvent {
    val uuid = UUID.randomUUID()
    textClickHandlers[uuid] = {
        if (!consumed.get()) {
            action.invoke(it)
            consumed.set(true)
        }
        textClickHandlers.remove(uuid)
    }
    return ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cobblemonclicktext $uuid")
}

fun click(onlyOnce: Boolean = false, action: (p: ServerPlayerEntity) -> Unit): ClickEvent {
    val uuid = UUID.randomUUID()
    textClickHandlers[uuid] = if (onlyOnce) {
        {
            textClickHandlers.remove(uuid)
            action.invoke(it)
        }
    } else {
        { action.invoke(it) }
    }
    return ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cobblemonclicktext $uuid")
}

fun hover(text: Text) = HoverEvent(HoverEvent.Action.SHOW_TEXT, text)
fun hover(text: String) = hover(Text.of(text))
fun hover(item: ItemStack) = HoverEvent(HoverEvent.Action.SHOW_ITEM, HoverEvent.ItemStackContent(item))
fun hover(entity: LivingEntity) = HoverEvent(HoverEvent.Action.SHOW_ENTITY, HoverEvent.EntityContent(entity.type, entity.uuid, entity.displayName))

val BOLD = Object()
val ITALIC = Object()
val UNDERLINED = Object()
val OBFUSCATED = Object()
val RESET = Object()

fun String.red() = text(Formatting.RED, this)
fun String.black() = text(Formatting.BLACK, this)
fun String.darkBlue() = text(Formatting.DARK_BLUE, this)
fun String.darkGreen() = text(Formatting.DARK_GREEN, this)
fun String.darkAqua() = text(Formatting.DARK_AQUA, this)
fun String.darkRed() = text(Formatting.DARK_RED, this)
fun String.darkPurple() = text(Formatting.DARK_PURPLE, this)
fun String.gold() = text(Formatting.GOLD, this)
fun String.gray() = text(Formatting.GRAY, this)
fun String.darkGray() = text(Formatting.DARK_GRAY, this)
fun String.blue() = text(Formatting.BLUE, this)
fun String.green() = text(Formatting.GREEN, this)
fun String.aqua() = text(Formatting.AQUA, this)
fun String.lightPurple() = text(Formatting.LIGHT_PURPLE, this)
fun String.yellow() = text(Formatting.YELLOW, this)
fun String.white() = text(Formatting.WHITE, this)

fun MutableText.red() = also { it.style = it.style.withColor(Formatting.RED) }
fun MutableText.black() = also { it.style = it.style.withColor(Formatting.BLACK) }
fun MutableText.darkBlue() = also { it.style = it.style.withColor(Formatting.DARK_BLUE) }
fun MutableText.darkGreen() = also { it.style = it.style.withColor(Formatting.DARK_GREEN) }
fun MutableText.darkAqua() = also { it.style = it.style.withColor(Formatting.DARK_AQUA) }
fun MutableText.darkRed() = also { it.style = it.style.withColor(Formatting.DARK_RED) }
fun MutableText.darkPurple() = also { it.style = it.style.withColor(Formatting.DARK_PURPLE) }
fun MutableText.gold() = also { it.style = it.style.withColor(Formatting.GOLD) }
fun MutableText.gray() = also { it.style = it.style.withColor(Formatting.GRAY) }
fun MutableText.darkGray() = also { it.style = it.style.withColor(Formatting.DARK_GRAY) }
fun MutableText.blue() = also { it.style = it.style.withColor(Formatting.BLUE) }
fun MutableText.green() = also { it.style = it.style.withColor(Formatting.GREEN) }
fun MutableText.aqua() = also { it.style = it.style.withColor(Formatting.AQUA) }
fun MutableText.lightPurple() = also { it.style = it.style.withColor(Formatting.LIGHT_PURPLE) }
fun MutableText.yellow() = also { it.style = it.style.withColor(Formatting.YELLOW) }
fun MutableText.white() = also { it.style = it.style.withColor(Formatting.WHITE) }
fun MutableText.font(identifier: Identifier) = also { it.style = it.style.withFont(identifier) }

fun String.text() = text(this)
fun String.stripCodes(): String = this.replace("[&§][A-Ea-e0-9K-Ok-oRr]".toRegex(), "")

fun MutableText.onClick(consumed: AtomicBoolean, action: (p: ServerPlayerEntity) -> Unit) = also { it.style = it.style.withClickEvent(click(consumed, action)) }
fun MutableText.onClick(onlyOnce: Boolean = false, action: (p: ServerPlayerEntity) -> Unit) = also { it.style = it.style.withClickEvent(click(onlyOnce, action)) }
fun MutableText.onHover(string: String) = also { it.style = it.style.withHoverEvent(hover(string)) }
fun MutableText.onHover(text: MutableText) = also { it.style = it.style.withHoverEvent(hover(text)) }
fun MutableText.underline() = also { it.style = it.style.withUnderline(true) }
fun MutableText.bold() = also { it.style = it.style.withBold(true) }
fun MutableText.italicise() = also { it.style = it.style.withItalic(true) }
fun MutableText.strikethrough() = also { it.style = it.style.withStrikethrough(true) }
fun MutableText.obfuscate() = also { it.style = it.style.withObfuscated(true) }
fun MutableText.suggest(command: String) = also { it.style = it.style.withClickEvent(ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command)) }

fun MutableText.add(other: Text): MutableText {
    this.append(other)
    return this
}

fun MutableText.add(string: String): MutableText {
    this.add(text(string))
    return this
}

operator fun MutableText.plus(component: Text) = this.add(component)
operator fun MutableText.plus(string: String) = this.add(string)

fun Iterable<MutableText>.sum(separator: MutableText = ", ".text()) = if (any()) reduce { acc, next -> acc + separator + next } else "".text()