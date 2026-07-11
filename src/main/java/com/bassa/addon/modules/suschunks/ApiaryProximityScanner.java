package com.bassa.addon.modules.suschunks;

import java.util.HashSet;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2586;
import net.minecraft.class_2680;
import net.minecraft.class_2741;
import net.minecraft.class_2818;
import net.minecraft.class_4482;
import net.minecraft.class_638;
import net.minecraft.class_746;

/* JADX INFO: loaded from: 1.jar:com/bassa/addon/modules/suschunks/ApiaryProximityScanner.class */
public final class ApiaryProximityScanner {
    private static final int HONEY_CAP = 5;

    private ApiaryProximityScanner() {
    }

    public static void sweep(DetectorContext param0) {
        class_746 class_746Var = param0.client.field_1724;
        class_638 class_638Var = param0.client.field_1687;
        if (class_746Var == null || class_638Var == null) {
            return;
        }
        int v3 = ((Integer) param0.beehiveRange.get()).intValue();
        int v4 = v3 * v3;
        class_2338 class_2338VarMethod_24515 = class_746Var.method_24515();
        int v6 = class_2338VarMethod_24515.method_10263();
        int v7 = class_2338VarMethod_24515.method_10260();
        int v8 = (v6 - v3) >> 4;
        int v9 = (v6 + v3) >> 4;
        int v10 = (v7 - v3) >> 4;
        int v11 = (v7 + v3) >> 4;
        HashSet hashSet = new HashSet();
        for (int v13 = v8; v13 <= v9; v13++) {
            for (int v14 = v10; v14 <= v11; v14++) {
                if (class_638Var.method_8393(v13, v14)) {
                    Object v15 = class_638Var.method_8497(v13, v14);
                    if (v15 instanceof class_2818) {
                        for (class_2586 class_2586Var : ((class_2818) v15).method_12214().values()) {
                            if (class_2586Var instanceof class_4482) {
                                class_2680 class_2680VarMethod_11010 = class_2586Var.method_11010();
                                boolean zMethod_27852 = class_2680VarMethod_11010.method_27852(class_2246.field_20421);
                                boolean zMethod_278522 = class_2680VarMethod_11010.method_27852(class_2246.field_20422);
                                if (zMethod_27852 || zMethod_278522) {
                                    if (class_2680VarMethod_11010.method_28498(class_2741.field_20432) && ((Integer) class_2680VarMethod_11010.method_11654(class_2741.field_20432)).intValue() == HONEY_CAP) {
                                        class_2338 class_2338VarMethod_11016 = class_2586Var.method_11016();
                                        double v23 = (((double) class_2338VarMethod_11016.method_10263()) + 0.5d) - (((double) v6) + 0.5d);
                                        double v25 = (((double) class_2338VarMethod_11016.method_10260()) + 0.5d) - (((double) v7) + 0.5d);
                                        if ((v23 * v23) + (v25 * v25) <= v4) {
                                            String str = zMethod_27852 ? "Beenest" : "Beehive";
                                            hashSet.add(class_2338VarMethod_11016);
                                            param0.registry.apiarySites.put(class_2338VarMethod_11016, str);
                                            if (param0.registry.apiaryAnnounced.add(class_2338VarMethod_11016)) {
                                                param0.alerts.apiary(class_2338VarMethod_11016, str, param0.notifyChat(), param0.notifySound());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        param0.registry.apiarySites.keySet().removeIf(param1 -> {
            return !hashSet.contains(param1);
        });
        param0.registry.apiaryAnnounced.removeIf(param12 -> {
            return !hashSet.contains(param12);
        });
    }
}
