package com.bassa.addon.ui;

import com.bassa.addon.mixin.HandledScreenAccessor;
import java.util.Objects;
import java.util.Random;
import net.minecraft.class_10799;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_412;
import net.minecraft.class_419;
import net.minecraft.class_424;
import net.minecraft.class_435;
import net.minecraft.class_437;
import net.minecraft.class_442;
import net.minecraft.class_465;
import net.minecraft.class_500;
import net.minecraft.class_5250;
import net.minecraft.class_8671;
import org.joml.Matrix3x2fStack;

/* JADX INFO: loaded from: 1.jar:com/bassa/addon/ui/KingUiRenderer.class */
public final class KingUiRenderer {
    private static final int ACCENT = -5620481;
    private static final int ACCENT_2 = -8635667;
    private static final int BG_TOP = -16054760;
    private static final int BG_BOTTOM = -15267024;
    private static final int PARTICLE_COUNT = 60;
    private static final Particle[] PARTICLES = new Particle[PARTICLE_COUNT];
    private static final int[] CROP = new int[4];
    private static float animTime;
    private static boolean initialized;

    private KingUiRenderer() {
    }

    public static boolean appliesTo(class_437 param0) {
        return (param0 instanceof class_442) || (param0 instanceof class_500) || (param0 instanceof class_419) || (param0 instanceof class_412) || (param0 instanceof class_435) || (param0 instanceof class_8671) || (param0 instanceof class_424);
    }

    public static void tick(float param0) {
        if (!initialized) {
            initParticles();
        }
        animTime += param0 * 0.035f;
    }

    public static void renderAmbient(class_332 param0, int param1, int param2, float param3) {
        if (param1 > 0 && param2 > 0) {
            tick(param3);
            float v4 = (class_3532.method_15374(animTime * 1.2f) + 1.0f) * 0.5f;
            drawAmbient(param0, param1, param2, v4);
        }
    }

    public static void render(class_332 param0, class_437 param1, float param2) {
        if (param1.field_22789 > 0 && param1.field_22790 > 0) {
            class_310 class_310VarMethod_1551 = class_310.method_1551();
            if (class_310VarMethod_1551 != null) {
                KingUiAssets.ensureLoaded(class_310VarMethod_1551);
            }
            tick(param2);
            int v4 = param1.field_22789;
            int v5 = param1.field_22790;
            Object v6 = KingUiScreenProfile.classify(param1);
            float v7 = (class_3532.method_15374(animTime * 1.2f) + 1.0f) * 0.5f;
            drawAmbient(param0, v4, v5, v7);
            if (v6 == KingUiScreenProfile.CONTAINER && (param1 instanceof class_465)) {
                drawContainerDepth(param0, (class_465) param1, v4, v5);
            }
        }
    }

    public static void renderTitleBrand(class_332 param0, int param1, float param2) {
        if (param1 > 0) {
            tick(0.0f);
            drawTitleWordmark(param0, param1, param2);
        }
    }

    private static void drawAmbient(class_332 param0, int param1, int param2, float param3) {
        param0.method_25296(0, 0, param1, param2, BG_TOP, BG_BOTTOM);
        if (KingUiAssets.isReady() && KingUiAssets.hasBackgroundTexture()) {
            KingUiLayout.backgroundCrop(param1, param2, 1920, 1080, CROP);
            param0.method_25291(class_10799.field_56883, KingUiAssets.BACKGROUND, CROP[0], CROP[1], 0.0f, 0.0f, CROP[2], CROP[3], 1920, 1080, withAlpha(-1, 70));
        }
        param0.method_25296(0, 0, param1, (int) (param2 * 0.62f), withAlpha(ACCENT, (int) (34.0f + (param3 * 16.0f))), withAlpha(ACCENT, 0));
        param0.method_25296(0, (int) (param2 * 0.45f), param1, param2, withAlpha(ACCENT_2, 0), withAlpha(ACCENT_2, (int) (26.0f + (param3 * 14.0f))));
        drawSheen(param0, param1, param2);
        drawParticles(param0, param1, param2);
        drawVignette(param0, param1, param2);
    }

    private static void drawSheen(class_332 param0, int param1, int param2) {
        int v3 = Math.max(26, param1 / 14);
        float v4 = ((animTime * 0.1f) % 1.6f) - 0.3f;
        int v5 = (int) (v4 * param1);
        Matrix3x2fStack matrix3x2fStackMethod_51448 = param0.method_51448();
        matrix3x2fStackMethod_51448.pushMatrix();
        matrix3x2fStackMethod_51448.translate(param1 / 2.0f, param2 / 2.0f);
        matrix3x2fStackMethod_51448.rotate(-0.31415927f);
        matrix3x2fStackMethod_51448.translate((-param1) / 2.0f, (-param2) / 2.0f);
        for (int v7 = -v3; v7 <= v3; v7++) {
            float v8 = 1.0f - (Math.abs(v7) / v3);
            int v9 = (int) (22.0f * v8 * v8);
            if (v9 > 0) {
                int v10 = v5 + v7;
                param0.method_25294(v10, -param2, v10 + 1, param2 * 2, withAlpha(-1, v9));
            }
        }
        matrix3x2fStackMethod_51448.popMatrix();
    }

    private static void drawParticles(class_332 param0, int param1, int param2) {
        for (Particle particle : PARTICLES) {
            particle.update(animTime, param1, param2);
            int v7 = (int) particle.x;
            int v8 = (int) particle.y;
            param0.method_25294(v7 - 1, v8 - 1, v7 + 2, v8 + 2, withAlpha(ACCENT, particle.alpha / 5));
            param0.method_25294(v7, v8, v7 + particle.size, v8 + particle.size, withAlpha(-1, particle.alpha));
        }
    }

    private static void drawVignette(class_332 param0, int param1, int param2) {
        int v3 = Math.max(46, param2 / 8);
        int v4 = withAlpha(-16777216, 200);
        int v5 = withAlpha(-16777216, 0);
        param0.method_25296(0, 0, param1, v3, v4, v5);
        param0.method_25296(0, param2 - v3, param1, param2, v5, v4);
        int v32 = Math.max(PARTICLE_COUNT, param1 / 7);
        param0.method_25296(0, 0, v32, param2, v4, v5);
        param0.method_25296(param1 - v32, 0, param1, param2, v5, v4);
    }

    private static void drawContainerDepth(class_332 param0, class_465<?> class_465Var, int param2, int param3) {
        HandledScreenAccessor handledScreenAccessor = (HandledScreenAccessor) class_465Var;
        int v5 = handledScreenAccessor.bassa$getX();
        int v6 = handledScreenAccessor.bassa$getY();
        int v7 = handledScreenAccessor.bassa$getBackgroundWidth();
        int v8 = handledScreenAccessor.bassa$getBackgroundHeight();
        int v9 = KingUiLayout.containerShadowPadding(param2, param3);
        param0.method_25294((v5 - v9) - 3, (v6 - v9) - 2, v5 + v7 + v9 + 3, v6 + v8 + v9 + 3, withAlpha(-16777216, 95));
        param0.method_25294(v5 - v9, v6 - v9, v5 + v7 + v9, v6 + v8 + v9, withAlpha(ACCENT, 28));
        param0.method_25294((v5 - v9) + 2, (v6 - v9) + 2, ((v5 + v7) + v9) - 2, ((v6 + v8) + v9) - 2, withAlpha(-15466456, 120));
    }

    private static void drawTitleWordmark(class_332 param0, int param1, float param2) {
        class_310 class_310VarMethod_1551 = class_310.method_1551();
        if (class_310VarMethod_1551 != null) {
            class_327 class_327Var = class_310VarMethod_1551.field_1772;
            class_5250 class_5250VarMethod_43470 = class_2561.method_43470("King");
            int v6 = class_327Var.method_27525(class_5250VarMethod_43470);
            Objects.requireNonNull(class_327Var);
            float v10 = Math.min(240.64f / v6, 6.2577777f);
            int v11 = KingUiLayout.titleLogoCenterX(param1);
            float v13 = v6 * v10;
            float v14 = 9.0f * v10;
            float v15 = v11 - (v13 / 2.0f);
            float v16 = 30.0f + ((64.0f - v14) / 2.0f);
            int v17 = class_3532.method_15340((int) (param2 * 255.0f), 0, 255);
            int v18 = withAlpha(-1, v17);
            int v19 = withAlpha(-12632257, v17);
            Matrix3x2fStack matrix3x2fStackMethod_51448 = param0.method_51448();
            matrix3x2fStackMethod_51448.pushMatrix();
            matrix3x2fStackMethod_51448.translate(v15, v16);
            matrix3x2fStackMethod_51448.scale(v10, v10);
            param0.method_51439(class_327Var, class_5250VarMethod_43470, 2, 2, v19, false);
            param0.method_51439(class_327Var, class_5250VarMethod_43470, 1, 1, v19, false);
            param0.method_51439(class_327Var, class_5250VarMethod_43470, 0, 0, v18, true);
            matrix3x2fStackMethod_51448.popMatrix();
        }
    }

    private static void initParticles() {
        Random random = new Random(195385857L);
        for (int v1 = 0; v1 < PARTICLE_COUNT; v1++) {
            PARTICLES[v1] = new Particle(random);
        }
        initialized = true;
    }

    private static int blend(int param0, int param1, float param2) {
        float param22 = class_3532.method_15363(param2, 0.0f, 1.0f);
        int v3 = (param0 >> 24) & 255;
        int v4 = (param0 >> 16) & 255;
        int v5 = (param0 >> 8) & 255;
        int v6 = param0 & 255;
        int v7 = (param1 >> 24) & 255;
        int v8 = (param1 >> 16) & 255;
        int v9 = (param1 >> 8) & 255;
        int v10 = param1 & 255;
        return (((int) (v3 + ((v7 - v3) * param22))) << 24) | (((int) (v4 + ((v8 - v4) * param22))) << 16) | (((int) (v5 + ((v9 - v5) * param22))) << 8) | ((int) (v6 + ((v10 - v6) * param22)));
    }

    private static int withAlpha(int param0, int param1) {
        return (class_3532.method_15340(param1, 0, 255) << 24) | (param0 & 16777215);
    }

    /* JADX INFO: loaded from: 1.jar:com/bassa/addon/ui/KingUiRenderer$Particle.class */
    private static final class Particle {
        private final float baseX;
        private final float baseY;
        private final float speed;
        private final float radius;
        private final int size;
        private final int alpha;
        private float x;
        private float y;

        private Particle(Random param1) {
            this.baseX = param1.nextFloat();
            this.baseY = param1.nextFloat();
            this.speed = 0.25f + (param1.nextFloat() * 0.8f);
            this.radius = 0.012f + (param1.nextFloat() * 0.04f);
            this.size = param1.nextBoolean() ? 1 : 2;
            this.alpha = 30 + param1.nextInt(80);
        }

        private void update(float param1, int param2, int param3) {
            float v4 = class_3532.method_15374((param1 * this.speed) + (this.baseX * 11.0f)) * this.radius;
            float v5 = class_3532.method_15362((param1 * this.speed * 0.85f) + (this.baseY * 9.0f)) * this.radius;
            this.x = (this.baseX + v4) * param2;
            this.y = (this.baseY + v5) * param3;
        }
    }
}
