package com.bassa.addon.modules.suschunks;

import net.minecraft.class_1923;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_2741;
import net.minecraft.class_2818;
import net.minecraft.class_2826;

/* JADX INFO: loaded from: 1.jar:com/bassa/addon/modules/suschunks/OvergrowthScanner.class */
public final class OvergrowthScanner {
    private static final int CLUSTER_QUOTA = 45;
    private static final int KELP_SCAN_Y = 50;
    private static final int VINE_TOP_Y = 64;
    private static final int VINE_BOTTOM_Y = -64;

    private OvergrowthScanner() {
    }

    public static void evaluate(class_2818 param0, class_1923 param1, DetectorContext param2) {
        if (param2.registry.overgrowthByColumn.containsKey(param1)) {
            return;
        }
        int v3 = param1.method_8326();
        int v4 = param1.method_8328();
        class_2338.class_2339 class_2339Var = new class_2338.class_2339();
        class_2826[] class_2826VarArrMethod_12006 = param0.method_12006();
        if (countMatureClusters(param0, v3, v4, class_2339Var, class_2826VarArrMethod_12006) >= CLUSTER_QUOTA) {
            register(param1, OvergrowthKind.CLUSTERS, "grown clusters", param2);
            return;
        }
        for (int v7 = 0; v7 < 16; v7++) {
            for (int v8 = 0; v8 < 16; v8++) {
                class_2339Var.method_10103(v3 + v7, KELP_SCAN_Y, v4 + v8);
                class_2680 class_2680VarMethod_8320 = param0.method_8320(class_2339Var);
                if (class_2680VarMethod_8320.method_27852(class_2246.field_9993) && class_2680VarMethod_8320.method_28498(class_2741.field_12517) && ((Integer) class_2680VarMethod_8320.method_11654(class_2741.field_12517)).intValue() == 25) {
                    register(param1, OvergrowthKind.KELP, "grown kelp", param2);
                    return;
                } else {
                    if (longestVineRun(param0, v3 + v7, v4 + v8, class_2339Var, class_2826VarArrMethod_12006) >= ((Integer) param2.vineChainMin.get()).intValue()) {
                        register(param1, OvergrowthKind.VINES, "grown vines", param2);
                        return;
                    }
                }
            }
        }
    }

    private static int countMatureClusters(class_2818 param0, int param1, int param2, class_2338.class_2339 param3, class_2826[] param4) {
        class_2826 class_2826Var;
        int v5 = 0;
        int v6 = VINE_BOTTOM_Y;
        while (v6 <= KELP_SCAN_Y) {
            int v7 = param0.method_31602(v6);
            if (v7 < 0 || v7 >= param4.length || !((class_2826Var = param4[v7]) == null || class_2826Var.method_38292())) {
                for (int i = 0; i < 16; i++) {
                    for (int v9 = 0; v9 < 16; v9++) {
                        param3.method_10103(param1 + i, v6, param2 + v9);
                        if (param0.method_8320(param3).method_27852(class_2246.field_27161)) {
                            v5++;
                            if (v5 >= CLUSTER_QUOTA) {
                                return v5;
                            }
                        }
                    }
                }
            } else {
                v6 = ((v6 >> 4) << 4) + 15;
            }
            v6++;
        }
        return v5;
    }

    private static int longestVineRun(class_2818 param0, int param1, int param2, class_2338.class_2339 param3, class_2826[] param4) {
        class_2826 class_2826Var;
        int v5 = 0;
        int v6 = 0;
        int v7 = 64;
        while (v7 >= VINE_BOTTOM_Y) {
            int v8 = param0.method_31602(v7);
            if (v8 < 0 || v8 >= param4.length || !((class_2826Var = param4[v8]) == null || class_2826Var.method_38292())) {
                param3.method_10103(param1, v7, param2);
                if (isClimbingPlant(param0.method_8320(param3))) {
                    v5++;
                    v6 = Math.max(v6, v5);
                } else {
                    v5 = 0;
                }
            } else {
                v5 = 0;
                v7 = (v7 >> 4) << 4;
            }
            v7--;
        }
        return v6;
    }

    private static boolean isClimbingPlant(class_2680 param0) {
        return param0.method_27852(class_2246.field_10597) || param0.method_27852(class_2246.field_22123) || param0.method_27852(class_2246.field_22124) || param0.method_27852(class_2246.field_23078) || param0.method_27852(class_2246.field_23079) || param0.method_27852(class_2246.field_28675) || param0.method_27852(class_2246.field_28676);
    }

    private static void register(class_1923 param0, OvergrowthKind param1, String param2, DetectorContext param3) {
        if (param3.registry.overgrowthByColumn.putIfAbsent(param0, param1) == null) {
            param3.alerts.overgrowth(param0, param2, param3.notifyChat(), param3.notifySound());
        }
    }
}
