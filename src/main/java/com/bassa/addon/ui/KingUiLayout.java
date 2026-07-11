package com.bassa.addon.ui;

import com.bassa.addon.mixin.HandledScreenAccessor;
import net.minecraft.class_3532;
import net.minecraft.class_437;
import net.minecraft.class_465;

/* JADX INFO: loaded from: 1.jar:com/bassa/addon/ui/KingUiLayout.class */
public final class KingUiLayout {
    public static final int TITLE_LOGO_BASE_Y = 30;
    public static final int TITLE_LOGO_WIDTH = 256;
    public static final int TITLE_LOGO_HEIGHT = 64;
    public static final int TITLE_EDITION_Y = 67;

    private KingUiLayout() {
    }

    public static int titleLogoCenterX(int param0) {
        return param0 / 2;
    }

    public static LogoFrame logo(class_437 param0, int param1, int param2, KingUiScreenProfile param3, float param4, float param5) {
        float v6 = param1 / Math.max(1, param2);
        int v7 = Math.min(param1, param2);
        int v8 = clamp((int) (v7 * (param3 == KingUiScreenProfile.CONTAINER ? 0.11f : 0.14f)), 28, 96);
        if (param3 == KingUiScreenProfile.FULL_MENU) {
            int v9 = (int) ((param4 * param1) - (v8 / 2.0f));
            int v10 = (int) ((param5 * param2) - (v8 / 2.0f));
            return new LogoFrame(v9, v10, v8, true);
        }
        if (param3 == KingUiScreenProfile.CONTAINER && (param0 instanceof class_465)) {
            HandledScreenAccessor handledScreenAccessor = (HandledScreenAccessor) (Object) param0;
            int v11 = handledScreenAccessor.bassa$getX();
            int v12 = handledScreenAccessor.bassa$getY();
            int v13 = handledScreenAccessor.bassa$getBackgroundWidth();
            handledScreenAccessor.bassa$getBackgroundHeight();
            int v15 = Math.max(12, v7 / 40);
            int v16 = v6 >= 1.65f ? 1 : 0;
            int v17 = v16 != 0 ? (param1 - v8) - v15 : v11 + v13 + v15;
            int v18 = v16 != 0 ? v15 : Math.max(v15, v12);
            if (v17 + v8 > param1 - v15) {
                v17 = (param1 - v8) - v15;
            }
            if (v18 + v8 > param2 - v15) {
                v18 = (param2 - v8) - v15;
            }
            if (v17 < v15) {
                v17 = v15;
            }
            if (v18 < v15) {
                v18 = v15;
            }
            return new LogoFrame(v17, v18, v8, false);
        }
        int v92 = Math.max(10, v7 / 36);
        return new LogoFrame((param1 - v8) - v92, v92, v8, false);
    }

    public static void backgroundCrop(int param0, int param1, int param2, int param3, int[] param4) {
        float v5 = param0 / Math.max(1, param1);
        float v6 = param2 / Math.max(1, param3);
        int v7 = param0;
        int v8 = param1;
        int v9 = 0;
        int v10 = 0;
        if (v5 > v6) {
            v8 = (int) (param0 / v6);
            v10 = (param1 - v8) / 2;
        } else {
            v7 = (int) (param1 * v6);
            v9 = (param0 - v7) / 2;
        }
        param4[0] = v9;
        param4[1] = v10;
        param4[2] = v7;
        param4[3] = v8;
    }

    public static int containerShadowPadding(int param0, int param1) {
        return Math.max(14, Math.min(param0, param1) / 28);
    }

    private static int clamp(int param0, int param1, int param2) {
        return class_3532.method_15340(param0, param1, param2);
    }

    public record LogoFrame(int x, int y, int size, boolean animated) {
    }
}
