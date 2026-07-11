package com.bassa.addon.ui.loading;

import com.bassa.addon.KingAddon;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.class_310;

/* JADX INFO: loaded from: 1.jar:com/bassa/addon/ui/loading/LoadingController.class */
public final class LoadingController {
    private final List<Stage> stages = new ArrayList();
    private final AtomicBoolean started = new AtomicBoolean(false);
    private Thread worker;

    /* JADX INFO: loaded from: 1.jar:com/bassa/addon/ui/loading/LoadingController$Task.class */
    public interface Task {
        void run() throws Exception;
    }

    public LoadingController stage(String param1, float param2, Task param3) {
        this.stages.add(new Stage(param1, param2, param3));
        return this;
    }

    public LoadingController stage(String param1, Task param2) {
        return stage(param1, 1.0f, param2);
    }

    public void start(LoadingProgress param1, Runnable param2) {
        if (this.started.compareAndSet(false, true)) {
            float v3 = 0.0f;
            Iterator<Stage> it = this.stages.iterator();
            while (it.hasNext()) {
                v3 += it.next().weight();
            }
            float f = v3 <= 0.0f ? 1.0f : v3;
            this.worker = new Thread(() -> {
                runStages(param1, param2, f);
            }, "King-Loading-Worker");
            this.worker.setDaemon(true);
            this.worker.start();
        }
    }

    private void runStages(LoadingProgress param1, Runnable param2, float param3) {
        float v4 = 0.0f;
        for (Stage stage : this.stages) {
            float v7 = v4 / param3;
            float fWeight = stage.weight() / param3;
            param1.report(v7, stage.label());
            param1.detail("");
            try {
                stage.task().run();
            } catch (Exception v9) {
                KingAddon.LOG.warn("King loading stage '{}' failed; continuing.", stage.label(), v9);
                param1.detail("recovered: " + stage.label());
            }
            v4 += stage.weight();
            param1.report(v4 / param3, stage.label());
        }
        param1.complete();
        class_310 class_310VarMethod_1551 = class_310.method_1551();
        if (param2 != null && class_310VarMethod_1551 != null) {
            class_310VarMethod_1551.execute(param2);
        } else if (param2 != null) {
            param2.run();
        }
    }

    public record Stage(String label, float weight, Task task) {
        public Stage {
            weight = weight <= 0.0f ? 1.0f : weight;
            label = label == null ? "Working…" : label;
        }
    }
}
