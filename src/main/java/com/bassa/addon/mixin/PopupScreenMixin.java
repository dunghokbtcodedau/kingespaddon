package com.bassa.addon.mixin;

import com.bassa.addon.ui.KingUiRenderer;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_8816;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: 1.jar:com/bassa/addon/mixin/PopupScreenMixin.class */
@Mixin({class_8816.class})
public abstract class PopupScreenMixin {
    @Inject(method = {"method_25420(Lnet/minecraft/class_332;IIF)V"}, at = {@At("HEAD")}, cancellable = true)
    private void bassa$cancelPopupBackground(class_332 param1, int param2, int param3, float param4, CallbackInfo param5) {
        if (KingUiRenderer.appliesTo((class_437) (Object) this)) {
            param5.cancel();
        }
    }
}
