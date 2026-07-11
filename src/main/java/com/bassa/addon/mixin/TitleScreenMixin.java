package com.bassa.addon.mixin;

import com.bassa.addon.ui.KingUiRenderer;
import net.minecraft.class_332;
import net.minecraft.class_442;
import net.minecraft.class_8020;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/* JADX INFO: loaded from: 1.jar:com/bassa/addon/mixin/TitleScreenMixin.class */
@Mixin({class_442.class})
public abstract class TitleScreenMixin {
    @Redirect(method = {"method_25394(Lnet/minecraft/class_332;IIF)V"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/class_8020;method_48209(Lnet/minecraft/class_332;IF)V"))
    private void bassa$replaceVanillaLogo(class_8020 param1, class_332 param2, int param3, float param4) {
        KingUiRenderer.renderTitleBrand(param2, param3, param4);
    }
}
