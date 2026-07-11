package com.bassa.addon.ui.loading;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/* JADX INFO: loaded from: 1.jar:com/bassa/addon/ui/loading/LoadingProgress.class */
public final class LoadingProgress {
    private final AtomicInteger progressBits = new AtomicInteger(0);
    private final AtomicReference<String> stage = new AtomicReference<>("Preparing…");
    private final AtomicReference<String> detail = new AtomicReference<>("");
    private final AtomicInteger completed = new AtomicInteger(0);
    private final long startNanos = System.nanoTime();

    public void report(float param1, String param2) {
        this.progressBits.set(Float.floatToRawIntBits(clamp01(param1)));
        if (param2 != null) {
            this.stage.set(param2);
        }
    }

    public void report(float param1) {
        this.progressBits.set(Float.floatToRawIntBits(clamp01(param1)));
    }

    public void detail(String param1) {
        this.detail.set(param1 == null ? "" : param1);
    }

    public void complete() {
        this.progressBits.set(1065353216);
        this.completed.set(1);
    }

    public float progress() {
        return Float.intBitsToFloat(this.progressBits.get());
    }

    public String stage() {
        return this.stage.get();
    }

    public String detail() {
        return this.detail.get();
    }

    public boolean isComplete() {
        return this.completed.get() == 1;
    }

    public long elapsedMillis() {
        return (System.nanoTime() - this.startNanos) / 1000000;
    }

    private static float clamp01(float param0) {
        if (Float.isNaN(param0) || param0 < 0.0f) {
            return 0.0f;
        }
        if (param0 > 1.0f) {
            return 1.0f;
        }
        return param0;
    }
}
