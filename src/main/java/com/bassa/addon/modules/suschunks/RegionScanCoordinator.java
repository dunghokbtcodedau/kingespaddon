package com.bassa.addon.modules.suschunks;

import com.bassa.addon.KingAddon;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.minecraft.class_1923;
import net.minecraft.class_2818;

/* JADX INFO: loaded from: 1.jar:com/bassa/addon/modules/suschunks/RegionScanCoordinator.class */
public final class RegionScanCoordinator {
    private static final int HORIZON_RADIUS = 16;
    private static final int TICKS_PER_DISPATCH = 20;
    private final ScanRegistry registry = new ScanRegistry();
    private ExecutorService workers;
    private DetectorContext ctx;
    private int pulse;

    public ScanRegistry registry() {
        return this.registry;
    }

    public void boot(DetectorContext param1) {
        shutdownWorkers();
        this.registry.reset();
        this.ctx = param1;
        this.pulse = 0;
        this.workers = Executors.newFixedThreadPool(2);
    }

    public void halt() {
        shutdownWorkers();
        this.registry.reset();
        this.ctx = null;
    }

    private void shutdownWorkers() {
        if (this.workers != null) {
            this.workers.shutdownNow();
            this.workers = null;
        }
    }

    public void pulse() {
        if (this.ctx == null || this.ctx.client.field_1687 == null || this.ctx.client.field_1724 == null) {
            return;
        }
        if (((Boolean) this.ctx.beehiveEnabled.get()).booleanValue()) {
            ApiaryProximityScanner.sweep(this.ctx);
        }
        int i = this.pulse + 1;
        this.pulse = i;
        if (i % TICKS_PER_DISPATCH == 0) {
            class_1923 class_1923VarMethod_31476 = this.ctx.client.field_1724.method_31476();
            ExecutorService executorService = this.workers;
            if (executorService != null) {
                this.registry.scheduledColumns.clear();
                for (int v1 = -16; v1 <= HORIZON_RADIUS; v1++) {
                    for (int v2 = -16; v2 <= HORIZON_RADIUS; v2++) {
                        class_1923 class_1923Var = new class_1923(class_1923VarMethod_31476.field_9181 + v1, class_1923VarMethod_31476.field_9180 + v2);
                        this.registry.scheduledColumns.add(class_1923Var);
                        if (this.ctx.client.field_1687.method_8393(class_1923Var.field_9181, class_1923Var.field_9180)) {
                            Object v8 = this.ctx.client.field_1687.method_8497(class_1923Var.field_9181, class_1923Var.field_9180);
                            if (v8 instanceof class_2818) {
                                class_2818 class_2818Var = (class_2818) v8;
                                DetectorContext detectorContext = this.ctx;
                                executorService.submit(() -> {
                                    runColumnPass(detectorContext, class_2818Var, class_1923Var);
                                });
                            }
                        }
                    }
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void runColumnPass(DetectorContext param0, class_2818 param1, class_1923 param2) {
        try {
            AnomalyColumnScanner.evaluate(param1, param2, param0);
            if (((Boolean) param0.overgrowthEnabled.get()).booleanValue()) {
                OvergrowthScanner.evaluate(param1, param2, param0);
            }
            if (((Boolean) param0.crystalEnabled.get()).booleanValue()) {
                CrystalDepositScanner.evaluate(param1, param2, param0);
            }
        } catch (Throwable v3) {
            KingAddon.LOG.warn("SusChunks scan failed at chunk {}", param2, v3);
        } finally {
            param0.registry.scheduledColumns.remove(param2);
        }
    }
}
