/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util

import com.bedrockk.molang.Expression
import com.bedrockk.molang.MoLang
import com.bedrockk.molang.ast.NumberExpression
import com.bedrockk.molang.runtime.MoLangRuntime
import com.bedrockk.molang.runtime.MoScope
import com.bedrockk.molang.runtime.value.MoValue
import net.minecraft.util.math.Vec3d

fun MoLangRuntime.resolve(expression: Expression): MoValue = expression.evaluate(MoScope(), environment)
fun MoLangRuntime.resolveDouble(expression: Expression): Double = resolve(expression).asDouble()
fun MoLangRuntime.resolveInt(expression: Expression): Int = resolveDouble(expression).toInt()

fun MoLangRuntime.resolveBoolean(expression: Expression): Boolean = resolve(expression).asDouble() != 0.0
fun MoLangRuntime.resolveVec3d(triple: Triple<Expression, Expression, Expression>) = Vec3d(
    resolveDouble(triple.first),
    resolveDouble(triple.second),
    resolveDouble(triple.third)
)

fun Expression.getString() = originalString ?: "0"
fun Double.asExpression() = NumberExpression(this)
fun String.asExpression() = MoLang.createParser(if (this == "") "0.0" else this).parseExpression()