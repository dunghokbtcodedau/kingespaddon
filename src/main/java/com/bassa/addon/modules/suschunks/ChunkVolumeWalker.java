package com.bassa.addon.modules.suschunks;

import net.minecraft.class_2818;
import net.minecraft.class_2826;

/* JADX INFO: loaded from: 1.jar:com/bassa/addon/modules/suschunks/ChunkVolumeWalker.class */
public final class ChunkVolumeWalker {

    /* JADX INFO: loaded from: 1.jar:com/bassa/addon/modules/suschunks/ChunkVolumeWalker$CellVisitor.class */
    @FunctionalInterface
    public interface CellVisitor {
        boolean visit(int i, int i2, int i3);
    }

    private ChunkVolumeWalker() {
    }

    public static void walk(class_2818 param0, int param1, int param2, int param3, int param4, CellVisitor param5) {
        class_2826 class_2826Var;
        class_2826[] class_2826VarArrMethod_12006 = param0.method_12006();
        int v7 = param3;
        while (v7 <= param4) {
            int v8 = param0.method_31602(v7);
            if (v8 < 0 || v8 >= class_2826VarArrMethod_12006.length || !((class_2826Var = class_2826VarArrMethod_12006[v8]) == null || class_2826Var.method_38292())) {
                for (int i = 0; i < 16; i++) {
                    for (int v10 = 0; v10 < 16; v10++) {
                        int v11 = param1 + i;
                        int v12 = param2 + v10;
                        if (!param5.visit(v11, v7, v12)) {
                            return;
                        }
                    }
                }
            } else {
                v7 = (v7 | 15) + 1;
            }
            v7++;
        }
    }
}
