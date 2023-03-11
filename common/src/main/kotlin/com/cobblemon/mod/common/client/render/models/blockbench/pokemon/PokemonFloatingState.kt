/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon

import com.cobblemon.mod.common.client.render.models.blockbench.PoseableEntityState
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity

/**
 * A [PoseableEntityState] that exists even without an entity or other concrete state. This is
 * for when a Pokémon needs to be continuously animated and there isn't an entity to attach state
 * to.
 *
 * @author Hiroku
 * @since May 1st, 2022
 */
class PokemonFloatingState : PoseableEntityState<PokemonEntity>()