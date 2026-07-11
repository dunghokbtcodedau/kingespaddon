package com.bassa.addon.mixin;

import com.bassa.addon.ui.KingUiAssets;
import com.bassa.addon.ui.loading.ForcedLoading;
import net.minecraft.class_310;
import net.minecraft.class_437;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: 1.jar:com/bassa/addon/mixin/MinecraftClientMixin.class */
@Mixin({class_310.class})
public abstract class MinecraftClientMixin {
    @Inject(method = {"method_1574()V"}, at = {@At("HEAD")})
    private void bassa$preloadUiAssets(CallbackInfo param1) {
        KingUiAssets.ensureLoaded((class_310) (Object) this);
    }

    @Inject(method = {"method_1507(Lnet/minecraft/class_437;)V"}, at = {@At("HEAD")}, cancellable = true)
    private void bassa$forceLoadingScreen(class_437 param1, CallbackInfo param2) {
        if (ForcedLoading.claim(param1)) {
            class_310 class_310Var = (class_310) (Object) this;
            class_310Var.method_1507(ForcedLoading.build(class_310Var, param1));
            param2.cancel();
        }
    }
}
