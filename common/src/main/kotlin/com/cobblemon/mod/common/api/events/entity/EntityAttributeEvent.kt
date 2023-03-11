/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events.entity

import net.minecraft.entity.EntityType
import net.minecraft.entity.attribute.DefaultAttributeContainer

/**
 * Event fired when we're registering entity attributes. This is really just a hack to get
 * attributes working on the Forge side because I'm a dumbass and have no idea how to fix
 * it properly.
 *
 * @author Hiroku
 * @since February 20th, 2022
 */
class EntityAttributeEvent(val entityType: EntityType<*>, val attributeSupplier: DefaultAttributeContainer.Builder)