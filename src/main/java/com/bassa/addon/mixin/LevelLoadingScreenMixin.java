package com.bassa.addon.mixin;

import com.bassa.addon.ui.KingUiRenderer;
import net.minecraft.class_332;
import net.minecraft.class_3928;
import net.minecraft.class_437;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: 1.jar:com/bassa/addon/mixin/LevelLoadingScreenMixin.class */
@Mixin({class_3928.class})
public abstract class LevelLoadingScreenMixin {
    @Inject(method = {"method_25420(Lnet/minecraft/class_332;IIF)V"}, at = {@At("HEAD")}, cancellable = true)
    private void bassa$cancelPortalBackground(class_332 param1, int param2, int param3, float param4, CallbackInfo param5) {
        if (KingUiRenderer.appliesTo((class_437) (Object) this)) {
            param5.cancel();
        }
    }
}
