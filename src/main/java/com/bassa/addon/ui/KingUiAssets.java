package com.bassa.addon.ui;

import com.bassa.addon.KingAddon;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.class_1011;
import net.minecraft.class_1049;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3298;

/* JADX INFO: loaded from: 1.jar:com/bassa/addon/ui/KingUiAssets.class */
public final class KingUiAssets {
    public static final class_2960 LOGO = class_2960.method_60655("bassa", "textures/icon.png");
    public static final class_2960 BACKGROUND = class_2960.method_60655("bassa", "textures/ui/background.png");
    private static final AtomicBoolean PRELOAD_STARTED = new AtomicBoolean(false);
    private static volatile boolean ready;
    private static volatile boolean hasBackgroundTexture;

    private KingUiAssets() {
    }

    public static boolean isReady() {
        return ready;
    }

    public static boolean hasBackgroundTexture() {
        return hasBackgroundTexture;
    }

    public static void ensureLoaded(class_310 param0) {
        if (!ready && param0 != null && PRELOAD_STARTED.compareAndSet(false, true)) {
            CompletableFuture.runAsync(() -> {
                warmCpuCache(param0);
            }).thenRun(() -> {
                param0.execute(() -> {
                    registerGpuTextures(param0);
                });
            }).exceptionally(param1 -> {
                KingAddon.LOG.warn("King Debug UI asset preload failed, falling back to procedural background.", param1);
                param0.execute(() -> {
                    registerGpuTextures(param0);
                });
                return null;
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void warmCpuCache(class_310 param0) {
        class_2960[] class_2960VarArr = {LOGO, BACKGROUND};
        for (int v3 = 0; v3 < 2; v3++) {
            try {
                class_3298 class_3298Var = (class_3298) param0.method_1478().method_14486(class_2960VarArr[v3]).orElse((class_3298) null);
                if (class_3298Var != null) {
                    InputStream inputStreamMethod_14482 = class_3298Var.method_14482();
                    try {
                        class_1011 class_1011VarMethod_4309 = class_1011.method_4309(inputStreamMethod_14482);
                        try {
                            class_1011VarMethod_4309.method_4307();
                            if (class_1011VarMethod_4309 != null) {
                                class_1011VarMethod_4309.close();
                            }
                            if (inputStreamMethod_14482 != null) {
                                inputStreamMethod_14482.close();
                            }
                        } catch (Throwable th) {
                            if (class_1011VarMethod_4309 != null) {
                                try {
                                    class_1011VarMethod_4309.close();
                                } catch (Throwable th2) {
                                    th.addSuppressed(th2);
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        if (inputStreamMethod_14482 != null) {
                            try {
                                inputStreamMethod_14482.close();
                            } catch (Throwable th4) {
                                th3.addSuppressed(th4);
                            }
                        }
                        throw th3;
                    }
                }
            } catch (Exception unused) {
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void registerGpuTextures(class_310 param0) {
        register(param0, LOGO);
        hasBackgroundTexture = register(param0, BACKGROUND);
        ready = true;
    }

    private static boolean register(class_310 param0, class_2960 param1) {
        if (param0.method_1478().method_14486(param1).isEmpty()) {
            return false;
        }
        try {
            param0.method_1531().method_65876(param1, new class_1049(param1));
            return true;
        } catch (Exception v2) {
            KingAddon.LOG.warn("Failed to register King Debug UI texture {}", param1, v2);
            return false;
        }
    }
}
