package com.bassa.addon.modules.suschunks;

import java.util.ArrayList;
import java.util.HashSet;
import net.minecraft.class_1923;
import net.minecraft.class_1944;
import net.minecraft.class_2338;
import net.minecraft.class_2818;

/* JADX INFO: loaded from: 1.jar:com/bassa/addon/modules/suschunks/CrystalDepositScanner.class */
public final class CrystalDepositScanner {
    private static final int Y_FLOOR = -64;
    private static final int Y_CEIL = 70;
    private static final int LIGHT_SIGNATURE = 5;
    private static final int HEIGHT_CUTOFF = 50;
    private static final int DEEP_Y_MAX = 5;

    private CrystalDepositScanner() {
    }

    public static void evaluate(class_2818 param0, class_1923 param1, DetectorContext param2) {
        if (param2.client.field_1687 != null) {
            int v3 = param1.method_8326();
            int v4 = param1.method_8328();
            class_2338.class_2339 class_2339Var = new class_2338.class_2339();
            class_2338.class_2339 class_2339Var2 = new class_2338.class_2339();
            ArrayList arrayList = new ArrayList();
            ChunkVolumeWalker.walk(param0, v3, v4, Y_FLOOR, Y_CEIL, (param5, param6, param7) -> {
                if (param6 > HEIGHT_CUTOFF) {
                    return true;
                }
                class_2339Var.method_10103(param5, param6, param7);
                if (param2.client.field_1687.method_8314(class_1944.field_9282, class_2339Var) != 5 || !CrystalNeighborhood.touchesCrystal(param0, class_2339Var, class_2339Var2)) {
                    return true;
                }
                arrayList.add(class_2339Var.method_10062());
                return true;
            });
            int v8 = ((Integer) param2.crystalMinHits.get()).intValue();
            if (arrayList.size() >= v8) {
                param2.registry.crystalDeposits.put(param1, new HashSet(arrayList));
                param2.registry.crystalLineAnchors.putIfAbsent(param1, anchorFor(arrayList));
                if (param2.notifyChat() && param2.registry.crystalAlerted.add(param1)) {
                    int v9 = arrayList.size();
                    param2.alerts.surfaceCrystal(param1, v9, true, param2.notifySound());
                }
            }
            if (((Boolean) param2.deepCrystalEnabled.get()).booleanValue()) {
                int v92 = 0;
                int v10 = 0;
                loop0: while (true) {
                    if (v10 > 5) {
                        break;
                    }
                    for (int v11 = 0; v11 < 16; v11++) {
                        for (int v12 = 0; v12 < 16; v12++) {
                            class_2339Var.method_10103(v3 + v11, v10, v4 + v12);
                            if (CrystalNeighborhood.touchesCrystal(param0, class_2339Var, class_2339Var2)) {
                                v92 = 1;
                                break loop0;
                            }
                        }
                    }
                    v10++;
                }
                if (v92 != 0 && param2.registry.deepCrystalColumns.add(param1) && param2.notifyChat() && param2.registry.deepCrystalAlerted.add(param1)) {
                    param2.alerts.deepCrystal(param1, true, param2.notifySound());
                }
            }
        }
    }

    public static class_2338 anchorFor(Iterable<class_2338> iterable) {
        double v1 = 0.0d;
        double v3 = 0.0d;
        double v5 = 0.0d;
        int v7 = 0;
        for (class_2338 class_2338Var : iterable) {
            v1 += ((double) class_2338Var.method_10263()) + 0.5d;
            v3 += ((double) class_2338Var.method_10264()) + 0.5d;
            v5 += ((double) class_2338Var.method_10260()) + 0.5d;
            v7++;
        }
        return v7 == 0 ? class_2338.field_10980 : class_2338.method_49637(v1 / ((double) v7), v3 / ((double) v7), v5 / ((double) v7));
    }
}
