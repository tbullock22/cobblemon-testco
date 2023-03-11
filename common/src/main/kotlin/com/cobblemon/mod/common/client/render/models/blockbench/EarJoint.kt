/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench

import net.minecraft.client.model.ModelPart

/**
 * An ear joint which focuses on a specific part and can only be rotated along a specific axis and range of motion.
 *
 * @author Hiroku
 * @since December 5th, 2021
 */
class EarJoint(val modelPart: ModelPart, val axis: Int, val rangeOfMotion: RangeOfMotion)