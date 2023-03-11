/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.command.argument.*;
import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArgumentTypes.class)
public class ArgumentTypesMixin {

    @Shadow
    private static <A extends ArgumentType<?>, T extends ArgumentSerializer.ArgumentTypeProperties<A>> ArgumentSerializer<A, T> register(Registry<ArgumentSerializer<?, ?>> registry, String string, Class<? extends A> clazz, ArgumentSerializer<A, T> argumentSerializer) {
        throw new AssertionError("Should not be executed.");
    }

    @Inject(method = "register(Lnet/minecraft/util/registry/Registry;)Lnet/minecraft/command/argument/serialize/ArgumentSerializer;", at = @At("RETURN"))
    private static void register(Registry<ArgumentSerializer<?, ?>> registry, CallbackInfoReturnable<ArgumentSerializer<?, ?>> ci) {
        register(registry, "cobblemon:pokemon", PokemonArgumentType.class, ConstantArgumentSerializer.of(PokemonArgumentType.Companion::pokemon));
        register(registry, "cobblemon:pokemonproperties", PokemonPropertiesArgumentType.class, ConstantArgumentSerializer.of(PokemonPropertiesArgumentType.Companion::properties));
        register(registry, "cobblemon:spawnbucket", SpawnBucketArgumentType.class, ConstantArgumentSerializer.of(SpawnBucketArgumentType.Companion::spawnBucket));
        register(registry, "cobblemon:move", MoveArgumentType.class, ConstantArgumentSerializer.of(MoveArgumentType.Companion::move));
        register(registry, "cobblemon:partyslot", PartySlotArgumentType.class, ConstantArgumentSerializer.of(PartySlotArgumentType.Companion::partySlot));
    }

}