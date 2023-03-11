/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.advancement.criterion

import com.google.gson.JsonObject
import net.minecraft.advancement.criterion.AbstractCriterion
import net.minecraft.advancement.criterion.AbstractCriterionConditions
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer
import net.minecraft.predicate.entity.EntityPredicate.Extended
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier

/**
 * A wrapping around the AbstractCriterion system of Minecraft. This allows the same base class to be used
 * for all advancements by wrapping the advancement-specific contextual data in the generic T, the instance of
 * the advancement conditions in the generic C, and allowing the condition to do all serialization, deserialization,
 * and matching of context against conditions.
 *
 * @author Hiroku
 * @since November 4th, 2022
 */
open class SimpleCriterionTrigger<T, C : SimpleCriterionCondition<T>>(
    val _id: Identifier,
    val criterionClass: Class<C>
) : AbstractCriterion<C>() {
    override fun getId() = _id
    fun trigger(player: ServerPlayerEntity, context: T) = this.trigger(player) { it.matches(player, context) }
    override fun conditionsFromJson(
        obj: JsonObject,
        playerPredicate: Extended,
        predicateDeserializer: AdvancementEntityPredicateDeserializer
    ): C {
        val instance = criterionClass.getConstructor(Identifier::class.java, Extended::class.java).newInstance(id, playerPredicate)
        instance.fromJson(obj)
        return instance
    }
}

/**
 * The base of the conditions for Cobblemon's wrapping around Minecraft advancements. This is parameterized by whatever
 * contextual information this condition needs to be checked, and must handle serialization and deserialization from JSON.
 *
 * @author Hiroku
 * @since November 4th, 2022
 */
abstract class SimpleCriterionCondition<T>(
    id: Identifier,
    entity: Extended
): AbstractCriterionConditions(id, entity) {
    override fun toJson(predicateSerializer: AdvancementEntityPredicateSerializer): JsonObject {
        val json = super.toJson(predicateSerializer)
        toJson(json)
        return json
    }

    abstract fun toJson(json: JsonObject)
    abstract fun fromJson(json: JsonObject)

    abstract fun matches(player: ServerPlayerEntity, context: T): Boolean
}