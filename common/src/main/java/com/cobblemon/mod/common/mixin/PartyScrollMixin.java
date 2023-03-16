/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.keybind.keybinds.PartySendBinding;
import net.minecraft.client.Mouse;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class PartyScrollMixin {
    @Shadow
    private double eventDeltaWheel;

    @Inject(
            method = "onMouseScroll",
            at = @At(
                    value = "FIELD",
                    target="Lnet/minecraft/client/Mouse;eventDeltaWheel:D",
                    opcode = Opcodes.PUTFIELD,
                    ordinal = 2,
                    shift = At.Shift.BEFORE
            ),
            cancellable = true
    )
    public void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (PartySendBinding.INSTANCE.getWasDown()) {
            int i = (int)eventDeltaWheel;
            if (i > 0) {
                while (i-- > 0) CobblemonClient.INSTANCE.getStorage().shiftSelected(false);
                ci.cancel();
                eventDeltaWheel = 0;
                PartySendBinding.INSTANCE.actioned();
            } else if (i < 0) {
                while (i++ < 0) CobblemonClient.INSTANCE.getStorage().shiftSelected(true);
                ci.cancel();
                eventDeltaWheel = 0;
                PartySendBinding.INSTANCE.actioned();
            }
        }
    }
}
