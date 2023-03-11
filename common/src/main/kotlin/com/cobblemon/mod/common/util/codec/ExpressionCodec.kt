/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.codec

import com.bedrockk.molang.Expression
import com.bedrockk.molang.MoLang
import com.cobblemon.mod.common.util.getString
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.codecs.PrimitiveCodec

val EXPRESSION_CODEC = object : PrimitiveCodec<Expression> {
    override fun <T> read(ops: DynamicOps<T>, input: T): DataResult<Expression> {
        return ops.getStringValue(input).map { MoLang.createParser(it).parseExpression() }
    }

    override fun <T> write(ops: DynamicOps<T>, value: Expression): T {
        return ops.createString(value.getString())
    }
}