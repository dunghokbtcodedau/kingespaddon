package com.bassa.addon.ui.loading;

import com.bassa.addon.ui.KingUiRenderer;
import com.bassa.addon.ui.loading.Easing;
import net.minecraft.class_10799;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_3532;

/* JADX INFO: loaded from: 1.jar:com/bassa/addon/ui/loading/LoadingRenderer.class */
public final class LoadingRenderer {
    private static final int ACCENT = 11156735;
    private static final int ACCENT_HI = 15244543;
    private final TextureCarousel carousel;
    private final Easing.Mode progressEasing;
    private final float[] progressBezier;
    private long lastNanos;
    private float animTime;
    private float renderedProgress;

    public LoadingRenderer(TextureCarousel param1, Easing.Mode param2, float[] param3) {
        this.carousel = param1;
        this.progressEasing = param2 == null ? Easing.Mode.EASE_OUT_EXPO : param2;
        this.progressBezier = (param3 == null || param3.length != 4) ? new float[]{0.16f, 1.0f, 0.3f, 1.0f} : param3;
        this.lastNanos = System.nanoTime();
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: java.lang.MatchException */
    public void render(class_332 param1, LoadingProgress param2, int param3, int param4) throws MatchException {
        if (param3 > 0 && param4 > 0) {
            long v5 = System.nanoTime();
            long v7 = Math.max(0L, (v5 - this.lastNanos) / 1000000);
            this.lastNanos = v5;
            this.animTime += v7 / 1000.0f;
            if (this.carousel != null) {
                this.carousel.update(v7);
            }
            advanceProgress(param2.progress(), v7);
            if (this.carousel != null && !this.carousel.isEmpty()) {
                drawHero(param1, param3, param4);
                drawVignette(param1, param3, param4);
            } else {
                KingUiRenderer.renderAmbient(param1, param3, param4, v7 / 16.0f);
            }
            drawProgressBar(param1, param2, param3, param4);
            drawStatusText(param1, param2, param3, param4);
        }
    }

    private void drawHero(class_332 param1, int param2, int param3) {
        class_2960 next;
        if (this.carousel != null && !this.carousel.isEmpty()) {
            float v4 = this.carousel.blend();
            float v5 = (class_3532.method_15374(this.animTime * 0.18f) * 0.5f) + 0.5f;
            float v6 = (class_3532.method_15362(this.animTime * 0.13f) * 0.5f) + 0.5f;
            class_2960 class_2960VarCurrent = this.carousel.current();
            if (class_2960VarCurrent != null) {
                drawHeroLayer(param1, class_2960VarCurrent, param2, param3, v5, v6, alpha(1.0f - v4));
            }
            if (this.carousel.isFading() && (next = this.carousel.next()) != null) {
                drawHeroLayer(param1, next, param2, param3, v5, v6, alpha(v4));
            }
            param1.method_25294(0, 0, param2, param3, 1711276032);
            return;
        }
        drawGradientFallback(param1, param2, param3);
    }

    private void drawHeroLayer(class_332 param1, class_2960 param2, int param3, int param4, float param5, float param6, int param7) {
        int v8 = Math.max(24, Math.min(param3, param4) / 10);
        int v9 = (int) ((param5 - 0.5f) * v8);
        int v10 = (int) ((param6 - 0.5f) * v8);
        int v11 = (-v8) + v9;
        int v12 = (-v8) + v10;
        int v13 = param3 + (v8 * 2);
        int v14 = param4 + (v8 * 2);
        param1.method_25291(class_10799.field_56883, param2, v11, v12, 0.0f, 0.0f, v13, v14, v13, v14, param7);
    }

    private void drawGradientFallback(class_332 param1, int param2, int param3) {
        param1.method_25296(0, 0, param2, param3, -16121836, -15073229);
    }

    private void drawVignette(class_332 param1, int param2, int param3) {
        int v4 = Math.max(48, param3 / 7);
        param1.method_25296(0, 0, param2, v4, -872415232, 0);
        param1.method_25296(0, param3 - v4, param2, param3, 0, -872415232);
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: java.lang.MatchException */
    private void advanceProgress(float param1, long param2) throws MatchException {
        float v5 = 1.0f - ((float) Math.exp((-param2) / 140.0f));
        float v6 = this.renderedProgress + ((param1 - this.renderedProgress) * v5);
        float v7 = Easing.apply(this.progressEasing, v6, this.progressBezier[0], this.progressBezier[1], this.progressBezier[2], this.progressBezier[3]);
        this.renderedProgress = class_3532.method_15363(class_3532.method_16439(0.5f, v6, v7), 0.0f, 1.0f);
    }

    private void drawProgressBar(class_332 param1, LoadingProgress param2, int param3, int param4) {
        int v5 = Math.min(440, param3 - 80);
        int v7 = (param3 - v5) / 2;
        int v8 = param4 - Math.max(70, param4 / 6);
        param1.method_25294(v7 - 2, v8 - 2, v7 + v5 + 2, v8 + 6 + 2, -1442840576);
        param1.method_25294(v7, v8, v7 + v5, v8 + 6, -14939602);
        int v9 = Math.max(0, (int) (v5 * this.renderedProgress));
        if (v9 > 0) {
            param1.method_25296(v7, v8, v7 + v9, v8 + 6, -5620481, -1532673);
            int v10 = Math.min(v7 + v9, v7 + v5);
            param1.method_25294(v10 - 2, v8 - 1, v10 + 1, v8 + 6 + 1, -1);
        }
        class_327 class_327VarTextRenderer = textRenderer();
        if (class_327VarTextRenderer != null) {
            float v11 = param2.progress();
            String str = Math.round(v11 * 100.0f) + "%";
            int v13 = class_327VarTextRenderer.method_1727(str);
            param1.method_51433(class_327VarTextRenderer, str, (v7 + v5) - v13, v8 - 12, -1520385, true);
        }
    }

    private void drawStatusText(class_332 param1, LoadingProgress param2, int param3, int param4) {
        class_327 class_327VarTextRenderer = textRenderer();
        if (class_327VarTextRenderer != null) {
            int v6 = param4 - Math.max(70, param4 / 6);
            String strStage = param2.stage();
            if (strStage != null && !strStage.isEmpty()) {
                int v8 = class_327VarTextRenderer.method_1727(strStage);
                param1.method_51433(class_327VarTextRenderer, strStage, (param3 - v8) / 2, v6 - 14, -1, true);
            }
            String strDetail = param2.detail();
            if (strDetail != null && !strDetail.isEmpty()) {
                int v9 = class_327VarTextRenderer.method_1727(strDetail);
                param1.method_51433(class_327VarTextRenderer, strDetail, (param3 - v9) / 2, v6 + 14, -6519632, true);
            }
        }
    }

    private static int alpha(float param0) {
        int v1 = class_3532.method_15340((int) (param0 * 255.0f), 0, 255);
        return (v1 << 24) | 16777215;
    }

    private static int argb(int param0) {
        return param0;
    }

    private static int withAlpha(int param0, int param1) {
        return (class_3532.method_15340(param1, 0, 255) << 24) | (param0 & 16777215);
    }

    private static class_327 textRenderer() {
        class_310 class_310VarMethod_1551 = class_310.method_1551();
        if (class_310VarMethod_1551 == null) {
            return null;
        }
        return class_310VarMethod_1551.field_1772;
    }
}
