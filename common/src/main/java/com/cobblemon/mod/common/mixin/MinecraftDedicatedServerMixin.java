/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerWorld.class)
public abstract class MinecraftDedicatedServerMixin implements DedicatedServer {

    @Inject(method = "shouldCancelSpawn", at = @At(value = "HEAD"), cancellable = true)
    public void cobblemon$allowPokemonSpawns(Entity entity, CallbackInfoReturnable<Boolean> callback) {
        if(entity instanceof PokemonEntity) {
            callback.setReturnValue(false);
        }
    }

}
