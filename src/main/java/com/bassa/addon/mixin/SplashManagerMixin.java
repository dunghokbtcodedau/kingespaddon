package com.bassa.addon.mixin;

import com.bassa.addon.ui.KingSplashTexts;
import net.minecraft.class_4008;
import net.minecraft.class_8519;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* JADX INFO: loaded from: 1.jar:com/bassa/addon/mixin/SplashManagerMixin.class */
@Mixin({class_4008.class})
public abstract class SplashManagerMixin {
    @Inject(method = {"method_18174()Lnet/minecraft/class_8519;"}, at = {@At("HEAD")}, cancellable = true)
    private void bassa$customSplash(CallbackInfoReturnable<class_8519> callbackInfoReturnable) {
        callbackInfoReturnable.setReturnValue(KingSplashTexts.randomRenderer());
    }
}
