/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench

import com.cobblemon.mod.common.client.render.MatrixWrapper
import net.minecraft.client.model.ModelPart
import net.minecraft.client.util.math.MatrixStack

/**
 * A storage of locator information for a model, attached to its [joint].
 * This is like a sister to ModelPart itself in its nesting structure, but
 * used specifically for Locators.
 *
 * @author Hiroku
 * @since February 10th, 2023
 */
class LocatorAccess(
    val joint: ModelPart,
    val locators: Map<String, ModelPart> = mapOf(),
    val children: List<LocatorAccess> = listOf()
) {
    companion object {
        const val PREFIX = "locator_"

        fun resolve(part: ModelPart): LocatorAccess? {
            val (
                locatorChildren,
                nonLocatorChildren
            ) = part.children.entries.partition { it.key.startsWith(PREFIX) }

            val locators = locatorChildren.associate { (namePrefixed, part) ->
                namePrefixed.substringAfter(PREFIX) to part
            }

            val children = mutableListOf<LocatorAccess>()

            if (nonLocatorChildren.isEmpty()) {
                return if (locators.isEmpty()) {
                    null
                } else {
                    LocatorAccess(
                        joint = part,
                        locators = locators
                    )
                }
            } else {
                children.addAll(nonLocatorChildren.mapNotNull { (_, part) -> resolve(part) })
                return if (children.isEmpty() && locators.isEmpty()) {
                    null
                } else {
                    LocatorAccess(
                        joint = part,
                        locators = locators,
                        children = children
                    )
                }
            }
        }
    }

    /**
     * Updates all of the locator states with the position at this current frame.
     * This is the same logic as ModelPart uses, that's why we reuse ModelPart#rotate.
     */
    fun update(matrixStack: MatrixStack, state: MutableMap<String, MatrixWrapper>) {
        matrixStack.push()
        joint.rotate(matrixStack)

        for ((name, locator) in locators) {
            matrixStack.push()
            locator.rotate(matrixStack)
            state.getOrPut(name) { MatrixWrapper() }.update(matrixStack.peek().positionMatrix)
            matrixStack.pop()
        }

        children.forEach {
            it.update(matrixStack, state)
        }

        matrixStack.pop()
    }
}