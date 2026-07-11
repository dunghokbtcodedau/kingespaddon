package com.bassa.addon.modules.suschunks;

import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_2818;

/* JADX INFO: loaded from: 1.jar:com/bassa/addon/modules/suschunks/CrystalNeighborhood.class */
public final class CrystalNeighborhood {
    private static final int RADIUS = 1;

    private CrystalNeighborhood() {
    }

    public static boolean touchesCrystal(class_2818 param0, class_2338.class_2339 param1, class_2338.class_2339 param2) {
        int v3 = param1.method_10263();
        int v4 = param1.method_10264();
        int v5 = param1.method_10260();
        for (int v6 = -1; v6 <= RADIUS; v6 += RADIUS) {
            for (int v7 = -1; v7 <= RADIUS; v7 += RADIUS) {
                for (int v8 = -1; v8 <= RADIUS; v8 += RADIUS) {
                    param2.method_10103(v3 + v6, v4 + v7, v5 + v8);
                    if (isCrystalFamily(param0.method_8320(param2))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean isCrystalFamily(class_2680 param0) {
        return param0.method_27852(class_2246.field_27161) || param0.method_27852(class_2246.field_27162) || param0.method_27852(class_2246.field_27163) || param0.method_27852(class_2246.field_27164) || param0.method_27852(class_2246.field_27160) || param0.method_27852(class_2246.field_27159);
    }
}
