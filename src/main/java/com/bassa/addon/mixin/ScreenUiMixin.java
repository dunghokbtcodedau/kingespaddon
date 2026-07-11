package com.bassa.addon.mixin;

import com.bassa.addon.ui.KingUiAssets;
import com.bassa.addon.ui.KingUiRenderer;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: 1.jar:com/bassa/addon/mixin/ScreenUiMixin.class */
@Mixin({class_437.class})
public abstract class ScreenUiMixin {
    @Inject(method = {"method_25426()V"}, at = {@At("TAIL")})
    private void bassa$initUiPipeline(CallbackInfo param1) {
        KingUiAssets.ensureLoaded(class_310.method_1551());
    }

    @Inject(method = {"method_47413(Lnet/minecraft/class_332;IIF)V"}, at = {@At("HEAD")})
    private void bassa$drawUniversalBackground(class_332 param1, int param2, int param3, float param4, CallbackInfo param5) {
        class_437 class_437Var = (class_437) (Object) this;
        if (KingUiRenderer.appliesTo(class_437Var)) {
            KingUiRenderer.render(param1, class_437Var, param4);
        }
    }

    @Inject(method = {"method_25420(Lnet/minecraft/class_332;IIF)V"}, at = {@At("HEAD")}, cancellable = true)
    private void bassa$cancelVanillaBackground(class_332 param1, int param2, int param3, float param4, CallbackInfo param5) {
        if (KingUiRenderer.appliesTo((class_437) (Object) this)) {
            param5.cancel();
        }
    }

    @Inject(method = {"method_57728(Lnet/minecraft/class_332;F)V"}, at = {@At("HEAD")}, cancellable = true)
    private void bassa$cancelPanorama(class_332 param1, float param2, CallbackInfo param3) {
        if (KingUiRenderer.appliesTo((class_437) (Object) this)) {
            param3.cancel();
        }
    }

    @Inject(method = {"method_52752(Lnet/minecraft/class_332;)V"}, at = {@At("HEAD")}, cancellable = true)
    private void bassa$cancelInGameBackground(class_332 param1, CallbackInfo param2) {
        if (KingUiRenderer.appliesTo((class_437) (Object) this)) {
            param2.cancel();
        }
    }

    @Inject(method = {"method_57734(Lnet/minecraft/class_332;)V"}, at = {@At("HEAD")}, cancellable = true)
    private void bassa$cancelBlur(class_332 param1, CallbackInfo param2) {
        if (KingUiRenderer.appliesTo((class_437) (Object) this)) {
            param2.cancel();
        }
    }

    @Inject(method = {"method_57735(Lnet/minecraft/class_332;)V"}, at = {@At("HEAD")}, cancellable = true)
    private void bassa$cancelDarkening(class_332 param1, CallbackInfo param2) {
        if (KingUiRenderer.appliesTo((class_437) (Object) this)) {
            param2.cancel();
        }
    }
}
