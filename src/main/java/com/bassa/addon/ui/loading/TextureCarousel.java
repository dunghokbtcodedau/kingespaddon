package com.bassa.addon.ui.loading;

import com.bassa.addon.KingAddon;
import com.bassa.addon.ui.loading.Easing;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1049;
import net.minecraft.class_2960;
import net.minecraft.class_310;

/* JADX INFO: loaded from: 1.jar:com/bassa/addon/ui/loading/TextureCarousel.class */
public final class TextureCarousel {
    private final List<class_2960> frames = new ArrayList();
    private final long dwellMillis;
    private final long fadeMillis;
    private final Easing.Mode easing;
    private final float[] bezier;
    private int currentIndex;
    private long frameClock;
    private boolean fading;
    private long fadeClock;

    public TextureCarousel(List<class_2960> list, long param2, long param4, Easing.Mode param6, float[] param7) {
        this.dwellMillis = Math.max(250L, param2);
        this.fadeMillis = Math.max(0L, param4);
        this.easing = param6 == null ? Easing.Mode.EASE_IN_OUT_CUBIC : param6;
        this.bezier = (param7 == null || param7.length != 4) ? new float[]{0.4f, 0.0f, 0.2f, 1.0f} : param7;
        class_310 class_310VarMethod_1551 = class_310.method_1551();
        if (list != null && class_310VarMethod_1551 != null) {
            for (class_2960 class_2960Var : list) {
                if (class_2960Var != null && register(class_310VarMethod_1551, class_2960Var)) {
                    this.frames.add(class_2960Var);
                }
            }
        }
    }

    public boolean isEmpty() {
        return this.frames.isEmpty();
    }

    public int size() {
        return this.frames.size();
    }

    public void update(long param1) {
        if (this.frames.size() > 1) {
            if (param1 < 0) {
                param1 = 0;
            }
            if (this.fading) {
                this.fadeClock += param1;
                if (this.fadeClock >= this.fadeMillis) {
                    this.fading = false;
                    this.fadeClock = 0L;
                    this.currentIndex = (this.currentIndex + 1) % this.frames.size();
                    this.frameClock = 0L;
                    return;
                }
                return;
            }
            this.frameClock += param1;
            if (this.frameClock >= this.dwellMillis) {
                if (this.fadeMillis == 0) {
                    this.currentIndex = (this.currentIndex + 1) % this.frames.size();
                    this.frameClock = 0L;
                } else {
                    this.fading = true;
                    this.fadeClock = 0L;
                }
            }
        }
    }

    public class_2960 current() {
        if (this.frames.isEmpty()) {
            return null;
        }
        return this.frames.get(this.currentIndex);
    }

    public class_2960 next() {
        if (this.frames.isEmpty()) {
            return null;
        }
        return this.frames.get((this.currentIndex + 1) % this.frames.size());
    }

    public float blend() {
        if (this.fading && this.fadeMillis != 0) {
            float v1 = this.fadeClock / this.fadeMillis;
            return Easing.apply(this.easing, v1, this.bezier[0], this.bezier[1], this.bezier[2], this.bezier[3]);
        }
        return 0.0f;
    }

    public boolean isFading() {
        return this.fading;
    }

    private static boolean register(class_310 param0, class_2960 param1) {
        try {
            if (param0.method_1478().method_14486(param1).isEmpty()) {
                return false;
            }
            param0.method_1531().method_65876(param1, new class_1049(param1));
            return true;
        } catch (Exception v2) {
            KingAddon.LOG.warn("King loading carousel: skipping unusable texture {}", param1, v2);
            return false;
        }
    }
}
