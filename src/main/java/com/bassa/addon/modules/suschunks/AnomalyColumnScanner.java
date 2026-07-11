package com.bassa.addon.modules.suschunks;

import net.minecraft.class_1923;
import net.minecraft.class_1944;
import net.minecraft.class_2338;
import net.minecraft.class_2818;

/* JADX INFO: loaded from: 1.jar:com/bassa/addon/modules/suschunks/AnomalyColumnScanner.class */
public final class AnomalyColumnScanner {
    private static final int Y_FLOOR = -64;
    private static final int Y_CEIL = 70;
    private static final int LIGHT_SIGNATURE = 5;
    private static final int HEIGHT_CUTOFF = 50;

    private AnomalyColumnScanner() {
    }

    public static void evaluate(class_2818 param0, class_1923 param1, DetectorContext param2) {
        if (param2.client.field_1687 == null || param2.registry.anomalySeeds.contains(param1)) {
            return;
        }
        int v3 = param1.method_8326();
        int v4 = param1.method_8328();
        class_2338.class_2339 class_2339Var = new class_2338.class_2339();
        class_2338.class_2339 class_2339Var2 = new class_2338.class_2339();
        int[] iArr = {0};
        ChunkVolumeWalker.walk(param0, v3, v4, Y_FLOOR, Y_CEIL, (param5, param6, param7) -> {
            if (param6 > HEIGHT_CUTOFF) {
                return true;
            }
            class_2339Var.method_10103(param5, param6, param7);
            if (param2.client.field_1687.method_8314(class_1944.field_9282, class_2339Var) != LIGHT_SIGNATURE || !CrystalNeighborhood.touchesCrystal(param0, class_2339Var, class_2339Var2)) {
                return true;
            }
            int i = iArr[0] + 1;
            iArr[0] = i;
            return i < ((Integer) param2.sensitivity.get()).intValue();
        });
        if (0 < ((Integer) param2.sensitivity.get()).intValue() || !param2.registry.anomalySeeds.add(param1)) {
            return;
        }
        param2.registry.mapOverlayColumns.add(param1);
        param2.registry.mapOverlayColumns.add(new class_1923(param1.field_9181 + 1, param1.field_9180));
        param2.registry.mapOverlayColumns.add(new class_1923(param1.field_9181, param1.field_9180 + 1));
        param2.registry.mapOverlayColumns.add(new class_1923(param1.field_9181 + 1, param1.field_9180 + 1));
        param2.alerts.susColumn(param1, 0, param2.notifyChat(), param2.notifySound());
    }
}
