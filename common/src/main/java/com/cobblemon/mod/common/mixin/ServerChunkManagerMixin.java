/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.api.spawning.mixins.CachedOnlyChunkAccessor;
import com.mojang.datafixers.util.Either;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.concurrent.CompletableFuture;

@Mixin(ServerChunkManager.class)
public abstract class ServerChunkManagerMixin extends ChunkManager implements CachedOnlyChunkAccessor {
    @Shadow @Nullable protected abstract ChunkHolder getChunkHolder(long pos);

    @Override
    public Chunk cobblemon$request(int x, int z, @NotNull ChunkStatus status) {
        long l = ChunkPos.toLong(x, z);

        ChunkHolder holder = getChunkHolder(l);
        if (holder != null) {
            CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> future = holder.getFutureFor(status);
            if (future.isDone()) {
                return future.join().left().orElse(null);
            }
        }

        return null;
    }
}
