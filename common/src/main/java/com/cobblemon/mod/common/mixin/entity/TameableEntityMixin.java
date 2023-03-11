/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin.entity;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TameableEntity.class)
public class TameableEntityMixin {

    @Redirect(method = "onDeath", at = @At(value = "FIELD", target = "Lnet/minecraft/world/World;isClient:Z"))
    public boolean cobblemon$checkIfPokemonBeforeSendingMessage(World world) {
        return world.isClient || (this.getClass().isAssignableFrom(PokemonEntity.class));
    }

}
