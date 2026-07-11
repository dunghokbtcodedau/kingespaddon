package com.bassa.addon.ui.loading;

import com.bassa.addon.ui.KingUiAssets;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_437;
import net.minecraft.class_442;

/* JADX INFO: loaded from: 1.jar:com/bassa/addon/ui/loading/ForcedLoading.class */
public final class ForcedLoading {
    private static final class_2960 CONFIG = class_2960.method_60655("kingdebug", "loading/loading_config.json");
    private static final AtomicBoolean CONSUMED = new AtomicBoolean(false);

    private ForcedLoading() {
    }

    public static boolean claim(class_437 param0) {
        if (param0 instanceof class_442) {
            return CONSUMED.compareAndSet(false, true);
        }
        return false;
    }

    public static KingLoadingScreen build(class_310 param0, class_437 param1) {
        LoadingConfig loadingConfigLoadOrDefault = LoadingConfig.loadOrDefault(CONFIG);
        return new KingLoadingScreen(loadingConfigLoadOrDefault, new LoadingController().stage(loadingConfigLoadOrDefault.stageText("assets", "Loading assets…"), 3.0f, () -> {
            KingUiAssets.ensureLoaded(param0);
            long v1 = System.currentTimeMillis() + 4000;
            while (!KingUiAssets.isReady() && System.currentTimeMillis() < v1) {
                Thread.sleep(40L);
            }
        }).stage(loadingConfigLoadOrDefault.stageText("modules", "Registering modules…"), 2.0f, () -> {
            Thread.sleep(550L);
        }).stage(loadingConfigLoadOrDefault.stageText("config", "Applying configuration…"), 2.0f, () -> {
            Thread.sleep(550L);
        }).stage(loadingConfigLoadOrDefault.stageText("finalize", "Finalizing…"), 1.0f, () -> {
            Thread.sleep(450L);
        }), () -> {
            param0.method_1507(param1 != null ? param1 : new class_442());
        });
    }
}
